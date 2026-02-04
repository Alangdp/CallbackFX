# DragUtils – Guia de Uso

Este guia resume os recursos expostos por `com.connectasistemas.framework.utils.drag.DragUtils` e pelas classes auxiliares `DragHandlers` e `DelimiterSpec`. O objetivo é destacar como registrar ciclos de drag em componentes JavaFX (`Region`), controlar o estado global de drag e configurar overlays de `Delimiter` para delimitar áreas interativas.

## Conceitos principais

- **DragHandlers**: encapsula os `EventHandler<MouseEvent>` usados em cada fase do drag (`press`, `drag`, `release`, `dragUp`). É construído via `DragHandlers.builder()` e entregue ao utilitário para instalação.
- **DragRegistration**: classe interna ao framework (pacote `internal`) responsável por anexar os handlers ao componente. O uso é transparente para quem consome `DragUtils`.
- **RegionOverlayPane + DelimiterSpec**: o overlay é criado automaticamente pelo utilitário e recebe uma lista imutável de `Delimiter`s pré-configurados. Cada `DelimiterSpec` define tamanho (em porcentagem), deslocamento (em porcentagem), rótulo e ação disparada ao ser acionado.
- **Hover Detection**: sistema que notifica elementos quando um drag está ocorrendo sobre eles, útil para componentes como ChildFactory que precisam reagir visualmente.

## Estado global de drag

O utilitário mantém um estado global que indica se há um drag em andamento e a posição atual do mouse.

```java
// Verifica se há drag ativo
if (DragUtils.isDragging()) {
    double x = DragUtils.getCurrentDragX();
    double y = DragUtils.getCurrentDragY();
    // ...
}

// Obtém o elemento que está sendo arrastado (pode ser null)
Region fonte = DragUtils.getCurrentDragSource();
```

### Atualizando o estado de drag

Deve ser chamado pelos handlers de drag para manter o estado sincronizado:

```java
// Ao iniciar o drag
DragUtils.setDragState(true, event.getSceneX(), event.getSceneY(), elementoArrastado);

// Durante o drag (atualiza posição)
DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());

// Ao finalizar o drag
DragUtils.setDragState(false, event.getSceneX(), event.getSceneY());
```

### Métodos relacionados

- `isDragging()`: retorna `true` se há um drag em andamento.
- `getCurrentDragX()` / `getCurrentDragY()`: retorna a posição atual do mouse durante o drag.
- `getCurrentDragSource()`: retorna a `Region` sendo arrastada, ou `null` se não houver.
- `setDragState(boolean, double, double)`: atualiza o estado de drag com a posição.
- `setDragState(boolean, double, double, Region)`: atualiza o estado de drag especificando o elemento sendo arrastado.

## Registro de handlers de drag

```java
Region area = new Pane();

DragHandlers handlers = DragHandlers.builder()
    .onPress(event -> System.out.println("Press"))
    .onDrag(event -> System.out.println("Arrastando"))
    .onRelease(event -> System.out.println("Soltou"))
    .onDragUp(event -> atualizarCursor())
    .build();

DragUtils.registerDragHandlers(area, handlers);
```

### Métodos relacionados

- `registerDragHandlers(Region, DragHandlers)`: instala os handlers. Substitui registros anteriores para a mesma `Region`.
- `unregisterDragHandlers(Region)`: remove os handlers previamente registrados (no-op para regiões não registradas).

## Hover Detection

Permite que elementos sejam notificados quando um drag entra ou sai de sua área. Útil para feedback visual durante operações de arrastar e soltar.

```java
Region alvo = new Pane();

DragUtils.registerHoverDetection(
    alvo,
    () -> System.out.println("Drag entrou na área"),
    () -> System.out.println("Drag saiu da área")
);
```

**Observação**: O elemento que está sendo arrastado (`currentDragSource`) é automaticamente excluído das notificações de hover para evitar que ele notifique a si mesmo.

### Métodos relacionados

- `registerHoverDetection(Region, Runnable, Runnable)`: registra callbacks de entrada e saída para o elemento.
- `unregisterHoverDetection(Region)`: remove o registro de hover detection.

## Overlays com DelimiterSpec

Os valores de `size` e `offset` são expressos em **porcentagem** relativa ao tamanho da `Region` hospedeira.

```java
List<DelimiterSpec> specs = DragUtils.immutableSpecs(
    DelimiterSpec.builder("left")
        .size(20, 100)      // 20% da largura, 100% da altura
        .offset(0, 0)       // canto superior esquerdo
        .label("Mover para esquerda")
        .onTrigger((event, delimiter) -> moverParaEsquerda())
        .build(),
    DelimiterSpec.builder("right")
        .size(20, 100)      // 20% da largura, 100% da altura
        .offset(80, 0)      // começa em 80% da largura
        .label("Mover para direita")
        .onTrigger((event, delimiter) -> moverParaDireita())
        .build()
);

RegionOverlayPane overlay = DragUtils.setupOverlay(area, specs);
```

### Métodos relacionados

- `setupOverlay(Region, List<DelimiterSpec>)`: cria (ou retorna) o overlay associado à `Region`. Exige ao menos um `DelimiterSpec`.
- `showOverlay(Region)` / `hideOverlay(Region)`: controla a visibilidade do overlay já configurado.
- `disposeOverlay(Region)`: remove o overlay e limpa referências.
- `getSelectedDelimiterKey(Region)`: retorna a chave (`key`) do último `Delimiter` disparado.
- `clearSelectedDelimiter(Region)`: limpa a seleção atual.
- `triggerDelimiterAtPosition(double, double)`: dispara manualmente o trigger do delimiter que está sob a posição especificada (em coordenadas de Scene). Retorna `true` se um trigger foi disparado.

## DelimiterSpec em detalhes

- `size(double width, double height)`: define o tamanho do `Delimiter` em **porcentagem** da `Region` hospedeira. Valores default: `0`.
- `offset(double offsetX, double offsetY)`: desloca o delimitador em **porcentagem** a partir do canto superior esquerdo do componente host.
- `label(String label)`: texto exibido dentro do `Delimiter`. Default vazio.
- `onTrigger(BiConsumer<DragEvent, Delimiter>)`: ação executada ao soltar o drag sobre o delimitador. Recebe o evento e o `Delimiter` correspondente.
- `key`: fornecido no builder, é usado para identificar a seleção via `getSelectedDelimiterKey`.

### Getters disponíveis

- `getKey()`: retorna a chave identificadora.
- `getWidth()` / `getHeight()`: retorna as dimensões em porcentagem.
- `getOffsetX()` / `getOffsetY()`: retorna os offsets em porcentagem.
- `getOnTrigger()`: retorna o callback configurado.

## Utilitários auxiliares

- `immutableSpecs(DelimiterSpec...)`: retorna lista imutável com as specs fornecidas. Útil para evitar modificações acidentais.

## Boas práticas

- Sempre remova handlers, overlays e registros de hover (`unregisterDragHandlers`, `disposeOverlay`, `unregisterHoverDetection`) ao descartar uma `Region` para evitar vazamentos de memória.
- Use `clearSelectedDelimiter` ao encerrar fluxos de drag para que a próxima interação comece sem estado prévio.
- Atualize o estado global via `setDragState` nos handlers de drag para que o sistema de hover detection funcione corretamente.
- Centralize a construção de `DelimiterSpec` em métodos utilitários se diferentes telas compartilharem a mesma geometria.
- Lembre-se de que todos os valores de tamanho e offset em `DelimiterSpec` são percentuais (0-100), não pixels.
