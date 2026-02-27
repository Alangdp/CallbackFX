# DragUtils – Guia de Uso

Este guia documenta todos os recursos expostos por `com.connectasistemas.framework.utils.drag.DragUtils` e pelas classes auxiliares `DragHandlers` e `DelimiterSpec`.

O utilitário suporta **dois sistemas de drag independentes** que podem coexistir:

- **Sistema de mouse** (`MouseEvent`) — drag customizado via press/drag/release, sem `Dragboard`. É o sistema principal do framework e o mais utilizado.
- **Sistema nativo JavaFX** (`DragEvent` / `Dragboard`) — drag-and-drop padrão da plataforma, usado quando a interoperabilidade com outros componentes JavaFX é necessária.

Ambos os sistemas compartilham o mesmo estado global (`isDragging`, `getDragData`, etc.) e o mesmo mecanismo de hover detection.

---

## Conceitos principais

- **DragHandlers**: encapsula os `EventHandler<MouseEvent>` usados em cada fase do drag (`press`, `drag`, `release`, `dragUp`, `dragDetected`). Construído via `DragHandlers.builder()`.
- **DragRegistration** / **NodeDragRegistration**: classes internas ao framework (pacote `internal`) que instalam os handlers no componente. O uso é transparente para quem consome `DragUtils`.
- **Drag Data**: mecanismo de transporte de dados entre fonte e destino, independente de `Dragboard`. Funciona em ambos os sistemas via `setDragData` / `getDragData`.
- **Hover Detection**: sistema que notifica regiões quando um drag entra ou sai de sua área durante o arrasto.
- **RegionOverlayPane + DelimiterSpec**: overlay visual criado sobre uma `Region`, dividido em zonas interativas (`Delimiter`). Cada zona possui tamanho e offset em porcentagem e dispara uma ação ao receber o drop.

---

## Estado global de drag

O utilitário mantém um estado global que indica se há um drag em andamento, a posição atual do mouse e o elemento sendo arrastado.

```java
if (DragUtils.isDragging()) {
    double x = DragUtils.getCurrentDragX();
    double y = DragUtils.getCurrentDragY();
}

Region fonte = DragUtils.getCurrentDragSource();
```

### Atualizando o estado

Deve ser chamado pelos handlers de drag para manter o sistema sincronizado. Ao chamar `setDragState(false, ...)`, o framework automaticamente dispara os `mouseDropHandlers` das regiões em hover, limpa os estados de hover e limpa o `dragData`.

```java
// Ao iniciar (onPress)
DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());

// Versão com a fonte explícita (exclui a fonte das notificações de hover)
DragUtils.setDragState(true, event.getSceneX(), event.getSceneY(), minhaRegion);

// Durante o arrasto (onDrag) — atualiza posição
DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());

// Ao soltar (onRelease) — dispara drops e limpa tudo automaticamente
DragUtils.setDragState(false, 0, 0);
```

### Métodos relacionados

| Método | Descrição |
|---|---|
| `isDragging()` | `true` se há um drag em andamento |
| `getCurrentDragX()` | Posição X atual do mouse na Scene |
| `getCurrentDragY()` | Posição Y atual do mouse na Scene |
| `getCurrentDragSource()` | `Region` sendo arrastada, ou `null` |
| `setDragState(boolean, double, double)` | Atualiza estado sem especificar fonte |
| `setDragState(boolean, double, double, Region)` | Atualiza estado especificando a fonte |

---

## Drag Data — transporte de dados entre classes

Permite transportar qualquer objeto da fonte para o destino sem depender de `Dragboard` ou variáveis estáticas externas. Funciona em ambos os sistemas.

```java
// Na fonte — define o dado ao iniciar o drag
DragUtils.setDragData(meuObjeto);

// No destino — recupera com segurança de tipo
MinhaClasse item = DragUtils.getDragData(MinhaClasse.class);

// Limpeza manual (opcional — é feita automaticamente ao finalizar o drag)
DragUtils.clearDragData();
```

`getDragData` retorna `null` se não houver dado ou se o tipo não corresponder ao esperado.

### Métodos relacionados

| Método | Descrição |
|---|---|
| `setDragData(Object)` | Define o dado associado ao drag atual |
| `getDragData(Class<T>)` | Retorna o dado com cast seguro, ou `null` |
| `clearDragData()` | Limpa o dado (chamado automaticamente pelo `setDragState(false, ...)`) |

---

## Sistema de mouse — registro de handlers

Use para componentes que implementam drag via `MouseEvent` (press/drag/release). Funciona tanto para `Region` quanto para qualquer `Node` (ex: `TreeCell`).

```java
DragHandlers handlers = DragHandlers.builder()
    .onPress(event -> {
        DragUtils.setDragData(meuItem);
        DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());
    })
    .onDrag(event -> {
        if (!DragUtils.isDragging()) return;
        DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());
    })
    .onRelease(event -> {
        if (!DragUtils.isDragging()) return;
        DragUtils.triggerDelimiterAtPosition(event.getSceneX(), event.getSceneY());
        DragUtils.setDragState(false, 0, 0); // dispara drops e limpa tudo
    })
    .onDragUp(event -> atualizarCursor())   // executado continuamente durante o drag
    .build();

// Para Region
DragUtils.registerDragHandlers(minhaRegion, handlers);

// Para Node genérico (TreeCell, Label, etc.)
DragUtils.registerDragHandlers(meuNode, handlers);
```

**Diferença entre `onDrag` e `onDragUp`:** ambos são chamados durante o movimento do mouse, mas `onDragUp` é projetado para atualizações contínuas como reposicionamento de cursor ou feedback visual, enquanto `onDrag` é para a lógica principal do arrasto.

**Sobre `NodeDragRegistration`:** quando registrado em um `Node` (ex: `TreeCell`), o PRESS é capturado no próprio node, mas DRAG e RELEASE são capturados na `Scene`. Isso resolve o problema comum em que o mouse sai da área do node durante o arrasto e os eventos param de ser recebidos. O registro na Scene é feito de forma lazy, aguardando o node entrar na Scene se ainda não estiver.

### Métodos relacionados

| Método | Descrição |
|---|---|
| `registerDragHandlers(Region, DragHandlers)` | Instala handlers em uma Region |
| `registerDragHandlers(Node, DragHandlers)` | Instala handlers em qualquer Node |
| `unregisterDragHandlers(Region)` | Remove handlers de uma Region |
| `unregisterDragHandlers(Node)` | Remove handlers de um Node |

---

## Sistema de mouse — recebendo o drop

Para o sistema de mouse, o destino usa `registerMouseDropHandler`, que é disparado automaticamente quando o mouse é solto sobre uma região que possui hover detection ativo.

```java
// No destino — registra o handler uma única vez (ex: no construtor)
DragUtils.registerHoverDetection(
    this,
    () -> this.setStyle("-fx-background-color: #e0ffe0;"), // onEnter
    () -> this.setStyle("")                                  // onExit
);

DragUtils.registerMouseDropHandler(this, dado -> {
    String item = (String) dado;
    // ou com segurança de tipo:
    // MinhaClasse item = DragUtils.getDragData(MinhaClasse.class);
    System.out.println("Recebi: " + item);
});
```

**Pré-requisito:** a região destino **precisa** ter hover detection registrado (`registerHoverDetection`) para que o framework saiba que ela está sob o mouse no momento do drop.

### Métodos relacionados

| Método | Descrição |
|---|---|
| `registerMouseDropHandler(Region, Consumer<Object>)` | Registra callback de drop para o sistema de mouse |
| `unregisterMouseDropHandler(Region)` | Remove o callback de drop |

---

## Sistema nativo JavaFX — Dragboard

Use `registerDropHandler` quando a interação usa o mecanismo nativo de drag-and-drop do JavaFX (com `Dragboard`). Neste caso, o dado pode vir tanto do `Dragboard` quanto de `getDragData`.

```java
// Na fonte — inicia o drag nativo no onDragDetected
.onDragDetected(event -> {
    Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
    ClipboardContent content = new ClipboardContent();
    content.putString(meuItem);
    db.setContent(content);
    DragUtils.setDragData(meuItem); // opcional: também disponibiliza via getDragData
    event.consume();
})

// No destino — recebe via DragEvent
DragUtils.registerDropHandler(minhaRegion, event -> {
    String item = event.getDragboard().getString();
    // ou: String item = DragUtils.getDragData(String.class);
    event.setDropCompleted(true);
    event.consume();
});
```

`registerDropHandler` chama internamente `region.setOnDragDropped(onDrop)`.

### Métodos relacionados

| Método | Descrição |
|---|---|
| `registerDropHandler(Region, EventHandler<DragEvent>)` | Registra handler para o sistema nativo JavaFX |

---

## Hover Detection

Notifica regiões quando o mouse passa sobre elas durante um drag. É o mecanismo que o framework usa internamente para saber onde o drop deve ser entregue.

```java
DragUtils.registerHoverDetection(
    minhaRegion,
    () -> System.out.println("Drag entrou"),
    () -> System.out.println("Drag saiu")
);
```

**Observação:** o elemento sendo arrastado (`currentDragSource`) é automaticamente excluído das notificações para evitar que notifique a si mesmo.

### Métodos relacionados

| Método | Descrição |
|---|---|
| `registerHoverDetection(Region, Runnable, Runnable)` | Registra callbacks de entrada e saída |
| `unregisterHoverDetection(Region)` | Remove o registro e dispara onExit se estava em hover |

---

## Overlays com DelimiterSpec

Cria zonas interativas visuais sobre uma `Region`. Os valores de `size` e `offset` são em **porcentagem** relativa ao tamanho da região.

```java
List<DelimiterSpec> specs = DragUtils.immutableSpecs(
    DelimiterSpec.builder("esquerda")
        .size(20, 100)      // 20% da largura, 100% da altura
        .offset(0, 0)       // canto superior esquerdo
        .label("Mover para esquerda")
        .onTrigger((event, delimiter) -> moverParaEsquerda())
        .build(),
    DelimiterSpec.builder("direita")
        .size(20, 100)
        .offset(80, 0)      // começa em 80% da largura
        .label("Mover para direita")
        .onTrigger((event, delimiter) -> moverParaDireita())
        .build()
);

RegionOverlayPane overlay = DragUtils.setupOverlay(minhaRegion, specs);
```

Para ativar o trigger manualmente (ex: no `onRelease` do sistema de mouse):

```java
DragUtils.triggerDelimiterAtPosition(event.getSceneX(), event.getSceneY());
```

### Métodos relacionados

| Método | Descrição |
|---|---|
| `setupOverlay(Region, List<DelimiterSpec>)` | Cria ou retorna o overlay da Region. Exige ao menos um spec |
| `showOverlay(Region)` | Exibe o overlay configurado |
| `hideOverlay(Region)` | Oculta o overlay configurado |
| `disposeOverlay(Region)` | Remove o overlay e libera recursos |
| `getSelectedDelimiterKey(Region)` | Retorna a `key` do último Delimiter disparado |
| `clearSelectedDelimiter(Region)` | Limpa a seleção atual |
| `triggerDelimiterAtPosition(double, double)` | Dispara o trigger do delimiter sob a posição (Scene). Retorna `true` se disparou |
| `immutableSpecs(DelimiterSpec...)` | Cria lista imutável de specs |

---

## DelimiterSpec em detalhes

| Método do Builder | Descrição |
|---|---|
| `builder(String key)` | Cria o builder com a chave identificadora |
| `size(double width, double height)` | Tamanho em porcentagem da Region hospedeira |
| `offset(double offsetX, double offsetY)` | Deslocamento em porcentagem a partir do canto superior esquerdo |
| `label(String label)` | Texto exibido dentro do Delimiter. Default: vazio |
| `onTrigger(BiConsumer<DragEvent, Delimiter>)` | Ação executada ao soltar o drag sobre o delimiter |

**Getters disponíveis:** `getKey()`, `getWidth()`, `getHeight()`, `getOffsetX()`, `getOffsetY()`, `getOnTrigger()`.

---

## DragHandlers em detalhes

| Método do Builder | Descrição |
|---|---|
| `onPress(EventHandler<MouseEvent>)` | Disparado ao pressionar o mouse |
| `onDrag(EventHandler<MouseEvent>)` | Disparado durante o movimento com botão pressionado |
| `onRelease(EventHandler<MouseEvent>)` | Disparado ao soltar o botão do mouse |
| `onDragUp(EventHandler<MouseEvent>)` | Executado continuamente durante o drag (ideal para cursor e feedback) |
| `onDragDetected(EventHandler<MouseEvent>)` | Disparado pelo evento `DRAG_DETECTED` do JavaFX (usado para iniciar drag nativo) |

---

## Exemplo completo — sistema de mouse entre classes distintas

**Classe fonte (ex: `MinhaTreeView`):**

```java
private void setupDragSource(TreeCell<String> cell) {
    DragHandlers handlers = DragHandlers.builder()
        .onPress(event -> {
            if (cell.isEmpty()) return;
            DragUtils.setDragData(cell.getItem()); // transporta o item
            DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());
            cell.setStyle("-fx-opacity: 0.5;");
            cell.setCursor(Cursor.MOVE);
        })
        .onDrag(event -> {
            if (!DragUtils.isDragging()) return;
            DragUtils.setDragState(true, event.getSceneX(), event.getSceneY());
            cell.setCursor(Cursor.CLOSED_HAND);
        })
        .onRelease(event -> {
            if (!DragUtils.isDragging()) return;
            DragUtils.triggerDelimiterAtPosition(event.getSceneX(), event.getSceneY());
            DragUtils.setDragState(false, 0, 0); // dispara drops + limpa tudo
            cell.setStyle("");
            cell.setCursor(Cursor.DEFAULT);
        })
        .build();

    DragUtils.registerDragHandlers(cell, handlers);
}
```

**Classe destino (ex: `MinhaPainel`):**

```java
public void setupDragEvent() {
    DragUtils.registerHoverDetection(
        this,
        () -> this.setStyle("-fx-background-color: #e0ffe0;"),
        () -> this.setStyle("")
    );

    DragUtils.registerMouseDropHandler(this, dado -> {
        String item = (String) dado;
        System.out.println("Recebi: " + item);
    });
}
```

---

## Boas práticas

- Sempre remova registros ao descartar uma `Region` para evitar vazamentos de memória, chamando `unregisterDragHandlers`, `unregisterMouseDropHandler`, `unregisterHoverDetection` e `disposeOverlay` conforme necessário.
- Use `clearSelectedDelimiter` ao encerrar fluxos com overlay para que a próxima interação comece sem estado prévio.
- Para o `registerMouseDropHandler` funcionar, a região destino **obrigatoriamente** precisa ter `registerHoverDetection` registrado — é o hover que sinaliza ao framework onde o drop deve ser entregue.
- Não use `registerDropHandler` (sistema nativo) junto com `registerMouseDropHandler` (sistema de mouse) na mesma região destino para o mesmo fluxo, pois pertencem a sistemas distintos e o nativo nunca será disparado pelo sistema de mouse.
- Todos os valores de tamanho e offset em `DelimiterSpec` são percentuais (0–100), não pixels.
- Centralize a construção de `DelimiterSpec` em métodos utilitários se diferentes telas compartilharem a mesma geometria de zonas.