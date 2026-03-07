# PRD — Desacoplação de Código

| Campo            | Valor                                    |
|------------------|-----------------------------------------|
| **Título**       | Desacoplação de Código                  |
| **Versão**       | 0.5                                      |
| **Data**         | 2026-03-01                               |
| **Autor**        | Equipe CallbackFX                        |
| **Status**       | Proposta                                 |

---

## 1. Resumo Executivo

O CallbackFX depende fortemente de estado estático global para orquestrar
composição de telas, navegação, eventos e validação. Sete classes mantêm
campos `static` mutáveis que representam o estado ativo da aplicação inteira.
Isso funciona no cenário atual (desktop single-window), porém gera:

- **Acoplamento invisível** entre subsistemas (eventos, validação, navegação)
- **Impossibilidade de testes isolados** (um teste altera estado que afeta outro)
- **Risco de colisão** ao operar múltiplas janelas ou modals simultâneos
- **Debugging penoso** (não se sabe quem alterou o estado global)

Este PRD propõe migrar o estado global para **objetos de contexto explícitos**,
usando o padrão _Context Object + Injeção de Dependência manual_, sem
introduzir frameworks externos como Spring. A estratégia é **incremental** —
migra-se uma classe por vez, mantendo compatibilidade retroativa durante a
transição.

---

## 2. Diagnóstico Atual — Mapa Completo de Estado Estático

### 2.1 Inventário de campos estáticos mutáveis

| Classe | Campo | Tipo | Escopo Real | Risco |
|--------|-------|------|-------------|-------|
| `Status` | `VALIDA` | `boolean` | Global por processo | **Crítico** |
| `Status` | `EVENT` | `EventType` | Global por processo | **Crítico** |
| `Status` | `EXIT_REASON` | `FocusExitReason` | Global por processo | **Crítico** |
| `Status` | `VAL_ERRSIM` | `boolean` | Global por processo | **Crítico** |
| `Status` | `NODE` | `Object` | Global por processo | **Crítico** |
| `Status` | `CONFIRMED_SELECTION` | `boolean` | Global por processo | **Crítico** |
| `Status` | `SKIP_VALIDATION` | `boolean` | Global por processo | **Crítico** |
| `ScreenManager` | `mainStage` | `Stage` | Singleton global | **Alto** |
| `ScreenManager` | `screenInstance` | `Object` | Singleton global | **Alto** |
| `ScreenManager` | `currentMetadata` | `ScreenMetadata` | Singleton global | **Alto** |
| `ScreenManager` | `changeInProgress` | `boolean` | Singleton global | **Alto** |
| `ScreenManager` | `deferredScreenClass` | `Class<?>` | Singleton global | **Alto** |
| `ScreenManager` | `screenHistory` | `Stack<Class<?>>` | Singleton global | **Alto** |
| `ScreenManager` | `childStages` | `Map<Object, Stage>` | Per-screen keys, mapa global | **Alto** |
| `ScreenManager` | `composingMetadata` | `ThreadLocal<Deque<...>>` | Per-thread, global | **Médio** |
| `ElementManager` | `literal` | `String` | Global temporário | **Alto** |
| `EventBinder` | `EVENT_MAP` | `Map<Object, Map<...>>` | Per-screen keys, mapa global | **Médio** |
| `ScreenControllerRegistry` | `CONTEXTS` | `ConcurrentHashMap` | Per-screen keys, mapa global | **Médio** |
| `ScreenControllerRegistry` | `INSTANCE_TO_CLASS` | `WeakHashMap` | Per-screen keys, mapa global | **Médio** |
| `ScreenManagerSharedData` | `CACHE` | `WeakHashMap` | Per-screen keys, mapa global | **Médio** |
| `ScreenHierarchyRegistry` | `CHILDREN_BY_PARENT` | `IdentityHashMap` | Per-screen keys, mapa global | **Baixo** |
| `ValidationBinderGeneric` | `BUTTON_TRIGGERS` | `WeakHashMap` | Per-button, mapa global | **Médio** |
| `ValidationBinderGeneric.FocusSuppression` | `SUPPRESSED` | `Set<TextInputControl>` | Per-control, mapa global | **Médio** |

**Total**: 22 campos estáticos mutáveis em 8 classes.

### 2.2 Classes estáticas imutáveis (não precisam mudar)

| Classe | Tipo de estado | Avaliação |
|--------|---------------|-----------|
| `CallbackInvoker` | Nenhum (stateless) | Pode permanecer `static` |
| `RegionManager` | Registro write-once (imutável após init) | Pode permanecer `static` |
| `ScreenAssembler` | 4 binders finais (stateless instances) | Migra junto com `ScreenManager` |
| `PropertiesBinderGeneric` | 1 constante `String` | Já é instance-based |
| `SizeBinderGeneric` | Nenhum | Já é instance-based |
| `PositionBinderGeneric` | Nenhum | Já é instance-based |
| `StringUtils`, `NumberUtils`, `DateTimeUtils` | Nenhum | Permanecem `static` |

### 2.3 Grafo de dependências atual

```
StartApplication
  └─→ ScreenManager [STATIC]
        ├─→ ScreenAssembler [STATIC]
        │     ├─→ ScreenManagerSharedData [STATIC]
        │     ├─→ EventBinder [STATIC]
        │     │     ├─→ CallbackInvoker [STATIC, stateless]
        │     │     └─→ EventBinder* subclasses [instance]
        │     │           └─→ EventBinderEvents [abstract]
        │     │                 └─→ Status [STATIC, mutable]
        │     ├─→ ScreenHierarchyRegistry [STATIC]
        │     ├─→ ElementManager [STATIC, mutable literal]
        │     ├─→ ValidationBinderGeneric [instance + STATIC maps]
        │     │     └─→ Status.VALIDA [STATIC]
        │     └─→ ScreenManager.push/popCompositionMetadata()
        ├─→ ScreenControllerRegistry [STATIC]
        ├─→ ScreenManagerSharedData [STATIC]
        ├─→ ScreenHierarchyRegistry [STATIC]
        └─→ EventBinder.deleteEvents() [STATIC]

Controllers do usuário
  ├─→ ScreenManager [STATIC] (openChildWindow, getScreenReference, etc.)
  ├─→ Status [STATIC] (markError, clearError)
  └─→ MessageUtil [STATIC] → Status [STATIC]
```

---

## 3. Problemas Concretos

### 3.1 Impossibilidade de testes isolados

O `Status` é escrito por `EventBinderEvents`, `ValidationBinderGeneric` e
`MessageUtil`, e lido por controllers. Se o teste A monta uma tela e
dispara um evento que seta `Status.VALIDA = true`, o teste B que roda em
seguida pode receber esse valor residual. Não há mecanismo de cleanup
automático — cada teste precisaria limpar manualmente todos os 7 campos do
`Status`, todos os mapas de `ScreenManagerSharedData`, `EventBinder`, etc.

### 3.2 Colisão entre janelas

`ScreenManager.openChildWindow()` abre uma sub-janela, mas o `Status`
permanece global. Se o usuário interage com a janela-filha e a janela-pai
ao mesmo tempo (e.g. modal + tela principal), o `Status.EVENT` e
`Status.NODE` podem ser sobrescritos por qualquer uma das janelas,
causando comportamento imprevisível.

### 3.3 Passagem de parâmetro via campo estático

`ElementManager.literal` é um campo estático mutável usado para passar um
parâmetro temporário entre `ScreenAssembler.createNodes()` →
`ElementManager.setLiteral()` → `ElementManager.createElement()`. Esse
padrão é equivalente a uma variável global usada como argumento de
função — frágil, não thread-safe, e impossível de testar em paralelo.

### 3.4 Ordem de chamada obrigatória

O `ScreenManager.init(Stage)` deve ser chamado antes de qualquer
`changeTo()`. Se não for, `mainStage` é `null` e o framework quebra
silenciosamente. Não há validação nem mensagem explicativa.

### 3.5 Debugging difícil

Quando `Status.VAL_ERRSIM` aparece como `true` inesperadamente, não há
como saber se foi setado por `EventBinderTextInputControl`,
`ValidationBinderGeneric`, `ExampleController.callbackAltcamSaveButton`,
ou qualquer outra das 14 classes que acessam `Status`. O estado global
não carrega metadados sobre quem o alterou.

### 3.6 Escalabilidade limitada

Se amanhã o framework precisar suportar múltiplas "aplicações" dentro de
um mesmo processo (e.g. plugin system, testes paralelos, multi-tenant),
o design atual impede completamente — existe um único `mainStage`,
um único `screenInstance`, um único `Status`.

---

## 4. Objetivos

| #  | Objetivo | Resultado esperado |
|----|----------|-------------------|
| O1 | Eliminar estado global do `Status` | Cada pipeline de evento opera sobre seu próprio contexto |
| O2 | Transformar `ScreenManager` em instância | Permitir múltiplos contextos de aplicação no mesmo processo |
| O3 | Eliminar passagem de parâmetro via `static` no `ElementManager` | Parâmetro `literal` passado explicitamente |
| O4 | Consolidar registros globais em um objeto de contexto | `EventBinder`, `ScreenControllerRegistry`, `ScreenManagerSharedData`, `ScreenHierarchyRegistry` pertencem a um dono explícito |
| O5 | Manter retrocompatibilidade durante a transição | Fachada estática delega para instância padrão |
| O6 | Permitir que controllers acessem estado de evento sem `static` | `EventContext` passado como parâmetro de callback |

---

## 5. Fora do Escopo

- Migração para framework de DI (Spring, Guice, Dagger)
- Suporte a multi-threading real no pipeline de composição
- Alteração da API pública dos controllers existentes (retrocompat via overload)
- Refatoração dos binders (já são instance-based e stateless)

---

## 6. Arquitetura Proposta

### 6.1 Conceito Central: `ScreenRuntimeContext`

Todas as classes que hoje guardam estado em `static` passam a ler/escrever
em uma instância de `ScreenRuntimeContext`. Essa instância é criada uma
vez por "aplicação" (normalmente em `StartApplication`) e propagada
explicitamente para `ScreenManager`, `ScreenAssembler`, binders e
controllers.

```
┌──────────────────────────────────────────────────────┐
│                 ScreenRuntimeContext                  │
│                                                      │
│  ┌──────────────────┐  ┌──────────────────────────┐  │
│  │  NavigationState  │  │  ScreenRegistry          │  │
│  │  - mainStage      │  │  - controllerContexts    │  │
│  │  - screenInstance  │  │  - instanceToClass       │  │
│  │  - history         │  │  - sharedDataCache       │  │
│  │  - currentMetadata │  │  - childrenByParent      │  │
│  │  - childStages     │  │  - eventMap              │  │
│  └──────────────────┘  └──────────────────────────┘  │
│                                                      │
│  ┌──────────────────────────────────────────────────┐│
│  │  ValidationState                                 ││
│  │  - buttonTriggers: Map<ButtonBase, List<...>>    ││
│  │  - focusSuppression: Set<TextInputControl>       ││
│  └──────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────┘
```

### 6.2 `EventContext` — Substituto do `Status`

Em vez de 7 campos estáticos globais, cada processamento de evento recebe
uma instância de `EventContext`:

```java
/**
 * Contexto de execução de um evento, substituindo o estado global de Status.
 * Cada pipeline de evento cria e consome sua própria instância.
 */
public class EventContext {

    private boolean valida;
    private EventType event;
    private FocusExitReason exitReason;
    private boolean validationError;
    private Object errorNode;
    private boolean confirmedSelection;
    private boolean skipValidation;

    // Métodos equivalentes aos de Status

    public void markError() {
        this.validationError = true;
        this.errorNode = null;
    }

    public void markError(Object node) {
        this.validationError = true;
        this.errorNode = node;
    }

    public void clearError() {
        this.validationError = false;
        this.errorNode = null;
    }

    public boolean shouldValidateOnExit() {
        return exitReason != FocusExitReason.SHIFT_TAB
            && exitReason != FocusExitReason.ESC;
    }

    public void markValidationSkip() {
        this.skipValidation = true;
    }

    public boolean consumeValidationSkip() {
        boolean value = this.skipValidation;
        this.skipValidation = false;
        return value;
    }

    // Getters e setters para todos os campos
}
```

### 6.3 Impacto no `CallbackInvoker` e nos Controllers

**Hoje**, o `CallbackInvoker` chama o callback assim:

```java
// Assinatura no controller hoje
public void callbackAltcamSaveButton(Example screen) {
    Status.clearError();                // lê/escreve global
    if (condição) Status.markError(node);
}
```

**Proposta**: o `CallbackInvoker` passa o `EventContext` como segundo argumento:

```java
// Assinatura nova (preferida)
public void callbackAltcamSaveButton(Example screen, EventContext context) {
    context.clearError();
    if (condição) context.markError(node);
}

// Assinatura antiga continua funcionando (retrocompatibilidade)
public void callbackAltcamSaveButton(Example screen) {
    // Neste caso, CallbackInvoker usa o contexto padrão internamente
}
```

O `CallbackInvoker.call()` tenta primeiro encontrar o método com parâmetro
`EventContext`; se não existir, faz fallback para a assinatura sem ele.
Isso garante que controllers existentes **não quebram**.

### 6.4 Impacto no `EventBinderEvents` e subclasses

A classe abstrata `EventBinderEvents` hoje acessa `Status` diretamente em
6 métodos protegidos (`publishEvent`, `changeValida`, `resetErrorTracking`,
`focusIfError`, `shouldValidateOnExit`, `clearExitReason`).

**Proposta**: `EventBinderEvents` recebe um `EventContext` no construtor (ou
via campo injetado pelo `EventBinder.attach()`). Os métodos protegidos
operam sobre essa instância:

```java
public abstract class EventBinderEvents {

    // Contexto de execução do evento atual
    protected final EventContext eventContext;

    protected EventBinderEvents(EventContext eventContext) {
        this.eventContext = eventContext;
    }

    protected void publishEvent(EventType eventType) {
        eventContext.setEvent(eventType);
    }

    protected void changeValida(boolean valida) {
        eventContext.setValida(valida);
    }

    protected void resetErrorTracking() {
        eventContext.clearError();
    }

    protected void focusIfError(Object defaultTarget) {
        if (!eventContext.isValidationError()) return;
        // mesma lógica, usando eventContext em vez de Status
    }

    protected boolean shouldValidateOnExit() {
        return eventContext.shouldValidateOnExit();
    }

    protected void clearExitReason() {
        eventContext.setExitReason(null);
    }
}
```

Cada `EventBinder*` (Button, TextInputControl, CheckBox, etc.) recebe o
`EventContext` e repassa ao super. **Nenhuma subclasse precisa acessar
`Status` diretamente.**

### 6.5 Impacto no `ScreenManager`

**Hoje** — 26 métodos estáticos, 9 campos estáticos:

```java
public class ScreenManager {
    private static Stage mainStage;
    private static Object screenInstance;
    // ...
    public static void init(Stage stage) { ... }
    public static void changeTo(Class<?> cls) { ... }
}
```

**Proposta** — instâncias com fachada estática transitória:

```java
/**
 * Contexto completo de navegação e estado da aplicação.
 */
public class ScreenRuntimeContext {

    private Stage mainStage;
    private Object screenInstance;
    private ScreenMetadata currentMetadata;
    private boolean changeInProgress;
    private Class<?> deferredScreenClass;
    private final Deque<Class<?>> history = new ArrayDeque<>();
    private final Map<Object, Stage> childStages = new WeakHashMap<>();
    private final ThreadLocal<Deque<ScreenCompositionMetadata>> composingMetadata
        = ThreadLocal.withInitial(ArrayDeque::new);

    // Registries (antes eram classes separadas com static)
    private final Map<Class<?>, ScreenContext> controllerContexts = new ConcurrentHashMap<>();
    private final Map<Object, Class<?>> instanceToClass = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Object, Map<String, Object>> sharedDataCache = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Object, List<Object>> childrenByParent = new IdentityHashMap<>();
    private final Map<Object, Map<Object, List<Runnable>>> eventMap = Collections.synchronizedMap(new WeakHashMap<>());

    // Getters e métodos de ciclo de vida
}
```

```java
/**
 * Gerenciador de telas, agora baseado em instância.
 */
public class ScreenManager {

    private final ScreenRuntimeContext context;

    public ScreenManager(ScreenRuntimeContext context) {
        this.context = context;
    }

    // Métodos de instância (mesmas assinaturas sem 'static')
    public void init(Stage stage) { ... }
    public void changeTo(Class<?> cls) { ... }

    // ─── Fachada estática (transição) ───

    private static ScreenManager defaultInstance;

    /**
     * Retorna a instância padrão. Mantida durante o período de transição
     * para não quebrar controllers existentes que chamam ScreenManager.changeTo().
     */
    public static ScreenManager getDefault() {
        return defaultInstance;
    }

    /** @deprecated Use new ScreenManager(context).init(stage) */
    @Deprecated
    public static void initStatic(Stage stage) {
        // ...
    }
}
```

### 6.6 Impacto no `ElementManager.literal`

**Hoje**: `setLiteral()` → `createElement()` → `applyLiteral()` → limpa.

**Proposta**: passar `literal` como parâmetro explícito:

```java
// Antes
ElementManager.setLiteral(definition.literal());
Object element = ElementManager.createElement(type);

// Depois
Object element = ElementManager.createElement(type, definition.literal());
```

O campo `static String literal` é removido completamente.

### 6.7 Impacto no `MessageUtil.confirm()`

**Hoje**: `confirm()` escreve `Status.CONFIRMED_SELECTION` e manipula
`Status.EXIT_REASON`.

**Proposta**: `confirm()` recebe `EventContext` como parâmetro e escreve nele:

```java
// Antes
public static boolean confirm(String title, String message) {
    // ... escreve Status.CONFIRMED_SELECTION, Status.EXIT_REASON
}

// Depois
public static boolean confirm(String title, String message, EventContext context) {
    // ... escreve context.setConfirmedSelection(), context.setExitReason()
}

// Overload retrocompatível (usa contexto padrão)
public static boolean confirm(String title, String message) {
    return confirm(title, message, EventContext.getDefault());
}
```

---

## 7. Plano de Migração em 6 Fases

A migração é **incremental**. Cada fase produz código compilável e funcional.
Classes migradas mantêm fachada estática que delega para a instância padrão,
de forma que controllers existentes não quebram.

### Fase 1 — `EventContext` (substituir `Status`)

**Prioridade**: Máxima (é o ponto mais acoplado do framework)

| Tarefa | Descrição |
|--------|-----------|
| 1.1 | Criar classe `EventContext` com os 7 campos + métodos de `Status` |
| 1.2 | Adicionar `EventContext` como campo em `EventBinderEvents` (recebido no construtor) |
| 1.3 | Migrar os 6 métodos protegidos de `EventBinderEvents` para usar `eventContext` |
| 1.4 | Atualizar os 11 `EventBinder*` para receberem e repassarem `EventContext` |
| 1.5 | Atualizar `EventBinder.attach()` para criar `EventContext` e repassar |
| 1.6 | Atualizar `ValidationBinderGeneric` para receber `EventContext` em `applyAll()` |
| 1.7 | Atualizar `MessageUtil.confirm()` para receber `EventContext` (com overload retrocompat) |
| 1.8 | Atualizar `CallbackInvoker.call()` para tentar assinatura com `EventContext` (fallback sem) |
| 1.9 | Manter `Status` como fachada que delega para `EventContext.getDefault()` |
| 1.10 | Marcar campos públicos de `Status` como `@Deprecated` |

**Classes modificadas**: `EventContext` (nova), `Status`, `EventBinderEvents`,
11× `EventBinder*`, `EventBinder`, `ValidationBinderGeneric`,
`MessageUtil`, `CallbackInvoker`

**Impacto em controllers existentes**: **Nenhum** (fachada `Status` continua
funcionando, assinatura sem `EventContext` continua aceita).

**Estimativa**: 3 a 4 dias

---

### Fase 2 — `ElementManager.literal` (eliminar parâmetro estático)

| Tarefa | Descrição |
|--------|-----------|
| 2.1 | Adicionar overload `createElement(Class<?>, String literal)` em `ElementManager` |
| 2.2 | Atualizar `ScreenAssembler.createNodes()` para chamar `createElement(type, literal)` |
| 2.3 | Remover `setLiteral()`, `applyLiteral()` e o campo `static String literal` |

**Classes modificadas**: `ElementManager`, `ScreenAssembler`

**Impacto em controllers existentes**: **Nenhum** (`createElement` não é API pública de controller)

**Estimativa**: meio dia

---

### Fase 3 — `ScreenRuntimeContext` (criar objeto de contexto)

| Tarefa | Descrição |
|--------|-----------|
| 3.1 | Criar classe `ScreenRuntimeContext` com os campos de navegação (Stage, history, metadata, etc.) |
| 3.2 | Mover campos estáticos de `ScreenManager` para dentro de `ScreenRuntimeContext` |
| 3.3 | Converter `ScreenManager` para classe de instância com construtor `(ScreenRuntimeContext)` |
| 3.4 | Criar fachada estática `ScreenManager.getDefault()` que retorna instância padrão |
| 3.5 | Atualizar `StartApplication` para criar `ScreenRuntimeContext` e `ScreenManager` |
| 3.6 | Manter todos os métodos estáticos existentes como `@Deprecated` delegando para `getDefault()` |

**Classes modificadas**: `ScreenRuntimeContext` (nova), `ScreenManager`,
`StartApplication`

**Impacto em controllers existentes**: **Nenhum** (fachada estática delegando mantida)

**Estimativa**: 3 a 4 dias

---

### Fase 4 — Consolidar registries no contexto

| Tarefa | Descrição |
|--------|-----------|
| 4.1 | Mover `ScreenControllerRegistry.CONTEXTS` e `INSTANCE_TO_CLASS` para `ScreenRuntimeContext` |
| 4.2 | Mover `ScreenManagerSharedData.CACHE` para `ScreenRuntimeContext` |
| 4.3 | Mover `ScreenHierarchyRegistry.CHILDREN_BY_PARENT` para `ScreenRuntimeContext` |
| 4.4 | Mover `EventBinder.EVENT_MAP` para `ScreenRuntimeContext` |
| 4.5 | Eliminar `getCache()` de `ScreenManagerSharedData` e substituir por métodos específicos |
| 4.6 | As 4 classes originais tornam-se fachadas `@Deprecated` delegando ao contexto |
| 4.7 | Atualizar `ScreenAssembler` para receber `ScreenRuntimeContext` |
| 4.8 | Atualizar `TreeManager`, `TableManager` para receber contexto (com overload retrocompat) |

**Classes modificadas**: `ScreenRuntimeContext`, `ScreenControllerRegistry`,
`ScreenManagerSharedData`, `ScreenHierarchyRegistry`, `EventBinder`,
`ScreenAssembler`, `TreeManager`, `TableManager`

**Impacto em controllers existentes**: **Nenhum** (fachadas delegando mantidas)

**Estimativa**: 3 a 4 dias

---

### Fase 5 — Mover estado de validação para contexto

| Tarefa | Descrição |
|--------|-----------|
| 5.1 | Criar `ValidationState` dentro de `ScreenRuntimeContext` contendo `buttonTriggers` e `focusSuppression` |
| 5.2 | Atualizar `ValidationBinderGeneric` para receber `ValidationState` em vez de usar campos `static` |
| 5.3 | Remover `BUTTON_TRIGGERS` e `FocusSuppression.SUPPRESSED` estáticos |

**Classes modificadas**: `ScreenRuntimeContext`, `ValidationBinderGeneric`

**Estimativa**: 1 a 2 dias

---

### Fase 6 — Limpeza e remoção de fachadas

| Tarefa | Descrição |
|--------|-----------|
| 6.1 | Migrar controllers de exemplo (`ExampleController`, `ProjectTreeEditorCallbacks`) para usar instância |
| 6.2 | Atualizar documentação e exemplos |
| 6.3 | Remover fachadas `@Deprecated` e campos estáticos residuais |
| 6.4 | Validar que `Status` pode ser removido ou reduzido a constantes |

**NOTA**: esta fase só pode ser executada quando todos os consumidores
conhecidos tiverem sido migrados. Pode ser adiada indefinidamente se houver
controllers de usuários externos.

**Estimativa**: 2 a 3 dias

---

## 8. Detalhamento Técnico por Classe

### 8.1 `Status` → `EventContext`

#### Estado atual (7 campos estáticos mutáveis)

```java
public class Status {
    public static boolean VALIDA;
    public static EventType EVENT;
    public static FocusExitReason EXIT_REASON;
    public static boolean VAL_ERRSIM;
    public static Object NODE;
    public static boolean CONFIRMED_SELECTION;
    private static boolean SKIP_VALIDATION;
}
```

#### 14 classes que acessam `Status` diretamente

| Classe | Leitura | Escrita |
|--------|---------|---------|
| `EventBinderEvents` | `VAL_ERRSIM`, `NODE`, `shouldValidateOnExit()` | `EVENT`, `VALIDA`, `clearError()`, `registerExitReason()`, `markValidationSkip()` |
| `EventBinderTextInputControl` | `consumeValidationSkip()`, `shouldValidateOnExit()` | `VALIDA` |
| `EventBinderButton` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderCheckBox` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderCheckEntryLabel` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderComboBox` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderListView` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderTableView` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderTextEntryLabel` | `consumeValidationSkip()` | `VALIDA` |
| `EventBinderTreeView` | `consumeValidationSkip()` | `VALIDA` |
| `ValidationBinderGeneric` | — | `VALIDA` |
| `MessageUtil` | `EXIT_REASON` | `CONFIRMED_SELECTION`, `EXIT_REASON` |
| `ExampleController` | — | `clearError()`, `markError(node)` |
| `ProjectTreeEditorCallbacks` | — | `markError(node)` |

#### Plano de migração

1. **`EventContext`** criado com os mesmos 7 campos como campos de instância
2. **`Status`** reescrito para delegar a um `EventContext` estático default
3. **`EventBinderEvents`** recebe `EventContext` no construtor
4. **`CallbackInvoker`** tenta resolver a assinatura `callback*(screen, EventContext)` antes de `callback*(screen)`
5. Controllers migram no seu ritmo — ambas as assinaturas aceitas

#### Cenário de uso futuro (múltiplas janelas)

```java
// Janela principal
EventContext mainCtx = new EventContext();

// Modal
EventContext modalCtx = new EventContext();

// Cada janela opera sem colisão de estado
```

---

### 8.2 `ScreenManager` → Instância + `ScreenRuntimeContext`

#### Estado atual (9 campos estáticos, 26 métodos estáticos)

| Campo | Escopo |
|-------|--------|
| `mainStage` | O Stage principal da aplicação |
| `screenInstance` | Instância da tela ativa |
| `propertiesBinderGeneric` | Binder imutável (pode permanecer `static final`) |
| `changeInProgress` | Guard de reentrância para `changeTo` |
| `deferredScreenClass` | Tela adiada durante composição |
| `screenHistory` | Pilha de navegação |
| `currentMetadata` | Metadados da tela ativa |
| `composingMetadata` | ThreadLocal com pilha de composição |
| `childStages` | Map de janelas-filhas |

#### Quem chama `ScreenManager` estaticamente

| Chamador | Métodos usados |
|----------|---------------|
| `StartApplication` | `init()`, `changeTo()` |
| `ScreenAssembler` | `pushCompositionMetadata()`, `popCompositionMetadata()` |
| `SizeBinderGeneric` | `getMainStage()` |
| `SizeBinderImageView` | `getMainStage()` |
| `ExampleController` | `openChildWindow()`, `getScreenReference()`, `setNodeVisibility()` |
| `ProjectTreeEditorCallbacks` | `closeChildWindow()` |

#### Estratégia de migração

1. Criar `ScreenRuntimeContext` com todos os campos de estado
2. Converter `ScreenManager` para classe de instância
3. Criar `static ScreenManager defaultInstance` + `getDefault()`
4. Cada método estático antigo vira `@Deprecated` e delega:

```java
/** @deprecated Use ScreenManager.getDefault().changeTo(cls) */
@Deprecated
public static void changeTo(Class<?> cls) {
    defaultInstance.changeTo(cls);
}
```

5. `StartApplication` atualizado:

```java
@Override
public void start(Stage stage) {
    ScreenRuntimeContext context = new ScreenRuntimeContext();
    ScreenManager manager = new ScreenManager(context);
    manager.init(stage);
    manager.changeTo(Example.class);
}
```

---

### 8.3 `ElementManager.literal` → Parâmetro explícito

#### Problema

```java
// ScreenAssembler.createNodes()
ElementManager.setLiteral(definition.literal());  // global write
Object element = ElementManager.createElement(type);  // global read + clear
```

Se duas chamadas interleavearem (futuro), o literal de uma pode ser
consumido pela outra.

#### Solução

```java
// Novo overload
public static Object createElement(Class<?> type, String literal) {
    Object element = registry.getOrDefault(type, () -> createDefault(type)).get();
    if (literal != null && !literal.isEmpty()) {
        applyLiteral(element, literal);
    }
    return element;
}
```

Remover `setLiteral()` e o campo `static String literal`.

---

### 8.4 `ScreenControllerRegistry` → Registros no contexto

#### Estado atual

```java
private static final Map<Class<?>, ScreenContext> CONTEXTS = new ConcurrentHashMap<>();
private static final Map<Object, Class<?>> INSTANCE_TO_CLASS = Collections.synchronizedMap(new WeakHashMap<>());
```

Chamado exclusivamente por `ScreenManager` (register, unregister, get).

#### Migração

Mover os dois mapas para `ScreenRuntimeContext`. O `ScreenManager` acessa
via `context.getControllerContexts()`. A classe `ScreenControllerRegistry`
vira fachada `@Deprecated` ou é removida.

---

### 8.5 `ScreenManagerSharedData` → Cache no contexto

#### Estado atual

```java
private static final Map<Object, Map<String, Object>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
```

Chamado por 5 classes: `ScreenAssembler`, `ScreenManager`,
`ValidationBinderGeneric`, `TreeManager`, `TableManager`.

#### Problema adicional: `getCache()` expõe o mapa interno

`TreeManager` e `ScreenAssembler.reorderChildren()` chamam `getCache()`
e iteram sobre o mapa diretamente. Isso quebra encapsulamento.

#### Migração

1. Mover `CACHE` para `ScreenRuntimeContext`
2. Substituir `getCache()` por métodos específicos:
   - `getElementsByScreen(Object screenInstance)` → `Map<String, Object>`
   - `findAcronymByElement(Object screenInstance, Object element)` → `String`
3. Atualizar `TreeManager` e `ScreenAssembler` para usar os novos métodos

---

### 8.6 `EventBinder.EVENT_MAP` → Mapa no contexto

#### Estado atual

```java
private static final Map<Object, Map<Object, List<Runnable>>> EVENT_MAP =
    Collections.synchronizedMap(new WeakHashMap<>());
```

Chamado por `ScreenAssembler` (`attach`) e `ScreenManager` (`deleteEvents`).

#### Migração

Mover para `ScreenRuntimeContext`. O `EventBinder` recebe o contexto
como parâmetro e opera sobre ele.

---

### 8.7 `ScreenHierarchyRegistry` → Mapa no contexto

#### Estado atual

```java
private static final Map<Object, List<Object>> CHILDREN_BY_PARENT = new IdentityHashMap<>();
```

Chamado apenas por `ScreenAssembler` e `ScreenManager`.

#### Migração

Trivial — mover para `ScreenRuntimeContext`.

---

### 8.8 `ValidationBinderGeneric` — Estado estático

#### `BUTTON_TRIGGERS`

```java
private static final Map<ButtonBase, List<TriggerAction>> BUTTON_TRIGGERS =
    Collections.synchronizedMap(new WeakHashMap<>());
```

Registra botões que disparam validação quando `@ScreenValidation(validateOn = "btnSave")`.
Cada entrada é per-button e limpa automaticamente pelo `WeakHashMap`.

#### `FocusSuppression.SUPPRESSED`

```java
private static final Set<TextInputControl> SUPPRESSED =
    Collections.newSetFromMap(new IdentityHashMap<>());
```

Set temporário de controles que devem pular a próxima validação.

#### Migração

Ambos movem para `ValidationState` dentro do `ScreenRuntimeContext`.
O `ValidationBinderGeneric` recebe o estado como parâmetro de `applyAll()`.

---

## 9. Retrocompatibilidade — Padrão de Fachada

Para cada classe migrada, manter uma fachada estática durante o período
de transição:

```java
public class Status {

    // Instância padrão (global, retrocompat)
    private static final EventContext DEFAULT_CONTEXT = new EventContext();

    /** @deprecated Use EventContext diretamente */
    @Deprecated
    public static boolean VALIDA;
    // Na prática, getters/setters delegam:

    /** @deprecated Use EventContext.getDefault().isValida() */
    @Deprecated
    public static void markError() {
        DEFAULT_CONTEXT.markError();
    }

    /** @deprecated Use EventContext.getDefault().clearError() */
    @Deprecated
    public static void clearError() {
        DEFAULT_CONTEXT.clearError();
    }

    /**
     * Retorna o contexto de evento padrão (global).
     */
    public static EventContext getDefault() {
        return DEFAULT_CONTEXT;
    }
}
```

Esse padrão garante que:

1. Controllers existentes (`Status.clearError()`, `ScreenManager.changeTo()`)
   **continuam compilando e funcionando**
2. Novos controllers podem adotar a API baseada em instância gradualmente
3. A depreciação é visível em IDEs e pode ser tratada com calma

---

## 10. Grafo de Dependências Proposto (Pós-Migração)

```
StartApplication
  └─→ cria ScreenRuntimeContext (instância)
  └─→ cria ScreenManager(context) (instância)
        ├─→ ScreenAssembler(context) (instância)
        │     ├─→ context.sharedDataCache (instância)
        │     ├─→ EventBinder(context) (instância)
        │     │     ├─→ CallbackInvoker (stateless, static OK)
        │     │     └─→ EventBinder*(eventContext) (instância)
        │     │           └─→ EventBinderEvents(eventContext) (instância)
        │     │                 └─→ EventContext (por evento)
        │     ├─→ context.childrenByParent (instância)
        │     ├─→ ElementManager.createElement(type, literal) (stateless)
        │     ├─→ RegionManager (stateless, static OK)
        │     ├─→ ValidationBinderGeneric(context.validationState) (instância)
        │     │     └─→ EventContext (por evento)
        │     └─→ context.composingMetadata
        ├─→ context.controllerContexts (instância)
        ├─→ context.sharedDataCache (instância)
        ├─→ context.childrenByParent (instância)
        └─→ context.eventMap (instância)

Controllers
  ├─→ ScreenManager.getDefault() ou instância injetada
  ├─→ EventContext (recebido como parâmetro de callback)
  └─→ MessageUtil (stateless + EventContext como parâmetro)
```

**Diferença chave**: nenhuma seta aponta para campo `static` mutável.
Todo estado percorre o grafo via referência de instância.

---

## 11. Critérios de Aceitação

| # | Critério | Verificação |
|---|----------|-------------|
| CA1 | `EventContext` criado e testável em isolamento | Testes unitários sem JavaFX |
| CA2 | `EventBinderEvents` e 11 subclasses usam `EventContext` | Code review + testes |
| CA3 | `Status` continua funcionando via fachada `@Deprecated` | `ExampleController` compila e roda sem mudança |
| CA4 | `CallbackInvoker` aceita assinatura com e sem `EventContext` | Teste parametrizado |
| CA5 | `ElementManager.literal` removido | Campo não existe mais, `createElement(type, literal)` funcional |
| CA6 | `ScreenManager` funciona como instância | Teste: dois `ScreenManager` com contextos distintos coexistem |
| CA7 | Fachadas estáticas delegam para `getDefault()` | Comportamento idêntico ao atual |
| CA8 | `mvnw -q -DskipTests compile` passa em cada fase | Build local |
| CA9 | `mvnw -q test` passa em cada fase | Testes existentes + novos |
| CA10 | Nenhum controller externo precisa mudar para funcionar | Retrocompat via overloads |

---

## 12. Riscos e Mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Refatoração gigante desestabiliza o framework | Alto | Média | Fases incrementais; cada fase compila e funciona isoladamente |
| Controllers de usuários externos quebram | Alto | Baixa | Fachada `@Deprecated` + overloads retrocompatíveis |
| Performance deteriora com indireção extra | Baixo | Baixa | Binders e invoker já fazem reflexão; 1 nível de indireção é negligível |
| `EventContext` precisa ser thread-local em caso de eventos concorrentes | Médio | Baixa (JavaFX é single-thread) | Documentar que o pipeline de eventos roda na FX Application Thread |
| Alguém migra parcialmente e deixa metade `static`, metade instância | Médio | Média | Fachada delega automaticamente; testes validam que ambos os caminhos convergem |
| `WeakHashMap` pode coletar referências se contexto não mantiver hard-ref | Médio | Baixa | `ScreenRuntimeContext` mantém referências fortes; documenta ciclo de vida |

---

## 13. Estimativas Consolidadas

| Fase | Escopo | Estimativa |
|------|--------|------------|
| **Fase 1** | `EventContext` → substitui `Status` | 3-4 dias |
| **Fase 2** | `ElementManager.literal` → parâmetro | 0,5 dia |
| **Fase 3** | `ScreenRuntimeContext` + `ScreenManager` instância | 3-4 dias |
| **Fase 4** | Consolidar 4 registries no contexto | 3-4 dias |
| **Fase 5** | Estado de validação no contexto | 1-2 dias |
| **Fase 6** | Limpeza e remoção de fachadas | 2-3 dias |
| **Total** | — | **~14-18 dias** |

---

## 14. Métricas de Acompanhamento

| Métrica | Meta | Ferramenta |
|---------|------|-----------|
| Campos `static` mutáveis restantes | 0 ao final da Fase 6 | Grep `static.*=` excluindo `final` imutáveis |
| Classes com `@Deprecated` pendente | 0 ao final da Fase 6 | IDE warnings |
| Testes que dependem de limpeza manual de `Status` | 0 ao final da Fase 1 | Code review |
| Cobertura de `EventContext` | ≥ 95% | JaCoCo |
| Build time | Sem regressão | Surefire report |

---

## 15. Glossário

| Termo | Definição |
|-------|-----------|
| **Context Object** | Padrão de design onde o estado de uma operação é encapsulado em um objeto passado explicitamente entre componentes, em vez de acessado via variáveis globais. |
| **Fachada estática** | Classe com métodos `static` que delegam para uma instância padrão. Permite migração gradual sem quebrar chamadores existentes. |
| **Injeção de dependência manual** | Passar dependências via construtor ou parâmetro de método, sem usar framework de DI (Spring, Guice). |
| **Estado por evento** | Estado que existe apenas durante o processamento de um único evento (foco, clique, validação) e é descartado ao final. |
| **Estado por tela** | Estado associado a uma instância de tela específica (cache de elementos, eventos registrados). |
| **Estado global** | Estado compartilhado por toda a aplicação, acessível de qualquer lugar via referência estática. |
| **Guard de reentrância** | Flag (`changeInProgress`) que impede que uma operação seja chamada recursivamente durante sua própria execução. |
| **WeakHashMap** | Implementação de `Map` que permite coleta de garbage das chaves quando não há outra referência forte. Usada nos registries para evitar memory leaks. |
