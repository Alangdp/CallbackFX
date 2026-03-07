# PRD — Implementar Testes Automatizados no Framework de Tela

| Campo            | Valor                                          |
|------------------|-------------------------------------------------|
| **Título**       | Implementar testes automatizados no framework de tela |
| **Versão**       | 1.0                                             |
| **Data**         | 2026-03-01                                      |
| **Autor**        | Equipe CallbackFX                               |
| **Status**       | Proposta                                        |


---

## 0. Não deve quebrar nenhuma função a alteração derve ser totalmente interna.

---

## 1. Contexto e Motivação

O CallbackFX é um framework JavaFX modular (Java 17) que monta telas
declarativamente a partir de classes anotadas com `@Screen`, `@ScreenField`,
`@ScreenFieldSize`, `@ScreenFieldPosition`, `@ScreenProperties` e
`@ScreenValidation`. Toda a composição — hierarquia pai-filho, tamanhos,
posições, propriedades visuais, validação de dados e bindagem de eventos — é
resolvida em tempo de execução via reflexão, sem FXML.

Atualmente o projeto **não possui nenhum teste automatizado** (zero arquivos em
`src/test/`), embora o JUnit 5 (5.10.2) já esteja configurado no `pom.xml` com
escopo `test`. Qualquer alteração no pipeline de composição
(`AnnotationProcessor` → `ScreenAssembler` → binders → `EventBinder`) só pode
ser validada manualmente rodando `mvnw javafx:run` e inspecionando a tela
visualmente.

Esse cenário traz riscos concretos:

* **Regressões silenciosas** — uma mudança em `ElementManager.addChild` ou em
  `ScreenMetadata.getFields()` (que ordena por hierarquia + `order`) pode
  embaralhar a árvore de nós sem que ninguém perceba até a UI abrir.
* **Fragilidade do contrato de callbacks** — a convenção de nomes
  `callback<Evento><Acronimo>` (e.g. `callbackAltcamSaveButton`) é resolvida em
  runtime por `CallbackInvoker`; um rename quebra o binding sem erro de
  compilação.
* **Validação não verificada** — `ValidationBinderGeneric` (578 linhas)
  implementa regras complexas de `maxLength`, ranges numéricos, datas, bloqueio
  de botão via `validateOn`, e troca de foco em caso de erro de validação, tudo cabe
  em testes unitários.
* **Custo de onboarding** — sem testes, novos contribuidores não conseguem
  confirmar se suas alterações estão corretas sem subir a aplicação completa.

---

## 2. Objetivos

| #  | Objetivo                           | Métrica de sucesso                                          |
|----|------------------------------------|-------------------------------------------------------------|
| O1 | Garantir corretude do motor (core) | 100% das classes do pipeline de composição cobertas por ≥1 teste |
| O2 | Prevenir regressões visuais        | Testes de UI detectam mudanças na árvore de nós e na interação |
| O3 | Proteger contratos de callback     | Toda convenção de nome é verificada; renames são flagados    |
| O4 | Validar regras de dados            | Cada regra de `@ScreenValidation` tem teste explícito       |
| O5 | Rodar em CI sem UI                 | Camadas 1, 3 e 4 executam em headless; camada 2 com Monocle |

---

## 3. Escopo

### 3.1 Dentro do escopo

* Testes unitários do motor (camada 1)
* Testes de UI automatizados com TestFX (camada 2)
* Testes estruturais da tela montada (camada 3)
* Testes de contrato do controller (camada 4)
* Snapshot testing de cenas renderizadas (camada 5)
* Configuração de dependências no `pom.xml`
* Configuração de CI headless (Monocle) para testes JavaFX

### 3.2 Fora do escopo

* Testes de performance / benchmark
* Testes end-to-end com múltiplas janelas encadeadas
* Refatoração do framework para melhorar testabilidade (pode ser proposta futura)
* Criação de mocks para `Stage`/`Scene` customizados

---

## 4. Arquitetura de Testes — 5 Camadas

### Camada 1 — Teste do Motor (Core do Framework)

**Escopo**: verificar que o pipeline `AnnotationProcessor` →
`ScreenMetadata` → `ScreenAssembler` → binders funciona corretamente a
partir de classes anotadas, **sem depender de interação visual**.

> Alguns testes desta camada precisam do toolkit JavaFX inicializado
> (porque `ScreenAssembler.compose()` instancia `Node`s reais via
> `ElementManager`). Contudo, **não sobem Stage nem Scene** — rodam
> em headless com Monocle.

**Classes sob teste e cenários**:

#### 4.1.1 `AnnotationProcessor`

| # | Cenário | Entrada | Saída esperada |
|---|---------|---------|----------------|
| 1 | Processar classe com `@Screen` válida | `Example.class` | `ScreenMetadata` com título `"CallbackFX Showcase"`, width 1280, height 780 |
| 2 | Processar classe **sem** `@Screen` | Classe pura sem anotação | `ScreenMetadata` vazia (sem título, campos vazios) |
| 3 | Validar campo `custom=true` sem `CustomElement` | Classe com campo anotado `@ScreenField(custom=true)` de tipo `VBox` | `IllegalStateException` |
| 4 | Validar campo implementa `CustomElement` sem `custom=true` | Classe com campo do tipo `MetricsCard` mas `custom=false` | `IllegalStateException` |
| 5 | Validar `CustomElement` que não estende `Region` | Classe fictícia | `IllegalStateException` |
| 6 | Processar todos os 41 campos de `Example` | `Example.class` | Metadata com 41 entradas no map, cada acronym único |
| 7 | Detectar acronym duplicado | Classe com dois campos de mesmo acronym | Exceção lançada por `metadata.addField()` |

#### 4.1.2 `ScreenMetadata`

| # | Cenário | Observação |
|---|---------|------------|
| 1 | `getFields()` retorna mapa ordenado por hierarquia pai-filho | Campos filhos aparecem após seus pais |
| 2 | `getFields()` respeita `order` entre irmãos | Para `featureRoot`, os filhos `layoutNode`..`utilsNode` saem na ordem 1 a 7 |
| 3 | `overrideRoot(Region)` substitui a raiz criada | A raiz injetada é a mesma retornada por `root()` |
| 4 | `setTitle()` / `getTitle()` roundtrip | Valor preservado |
| 5 | `addField()` com acronym duplicado lança exceção | Impede colisão de nomes |

#### 4.1.3 `ScreenAssembler.compose()`

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | Montar `Example.class` → raiz é `BorderPane` | `root instanceof BorderPane` |
| 2 | `father = "root"` com `position = BOTTOM` → Label em `root.getBottom()` | `root.getBottom() instanceof Label` com texto `"Status: pronto"` |
| 3 | `father = "root"` com `position = CENTER` → `SplitPane` no centro | `root.getCenter() instanceof SplitPane` |
| 4 | Hierarquia `layoutSplit` → filhos `explorerPanel` e `contentTabs` | `splitPane.getItems().size() == 2` |
| 5 | Hierarquia completa `featureTree` → `featureRoot` → 7 filhos `TreeItem` | `featureRoot.getChildren().size() == 7` |
| 6 | Ids aplicados automaticamente | `root.lookup("#filterInput") != null` |
| 7 | Literal aplicado em Label | `statusBar.getText().equals("Status: pronto")` |
| 8 | Literal aplicado em TreeItem | `featureRoot.getValue().equals("Framework")` |
| 9 | Literal aplicado em Tab | `overviewTab.getText().equals("Visão geral")` |
| 10 | Literal aplicado em Button | `addFolderButton.getText().equals("Nova pasta")` |
| 11 | Tela aninhada (campo com `@Screen`) compõe recursivamente | Nó raiz retornado é a raiz da tela filha |
| 12 | Elemento `custom=true` (`MetricsCard`) instanciado corretamente | Campo é `instanceof MetricsCard` e `instanceof VBox` |
| 13 | Callback instance instanciada | `metadata.callbackInstance() instanceof ExampleController` |

#### 4.1.4 `SizeBinderGeneric`

| # | Cenário | Campo de referência | Validação |
|---|---------|---------------------|-----------|
| 1 | `width = 260` aplicado | `filterInput` | `prefWidth == 260` |
| 2 | `vgrow = true` aplicado | `featureTree` | `VBox.getVgrow(node) == Priority.ALWAYS` |
| 3 | `hgrow = true` aplicado | `root` | `HBox.getHgrow(node) == Priority.ALWAYS` |
| 4 | `padding = {16,16,16,16}` aplicado | `explorerPanel` | `getInsets() == Insets(16,16,16,16)` |
| 5 | `spacing = 12` aplicado em VBox | `explorerPanel` | `getSpacing() == 12` |
| 6 | `height = 220` aplicado | `eventLogList` | `prefHeight == 220` |
| 7 | `minHeight = 260` aplicado | `featureTree` | `minHeight == 260` |
| 8 | Tamanho percentual (0 < valor ≤ 1) multiplicado pela dimensão do stage | Campo hipotético | Valor final = fração × dimensão |

#### 4.1.5 `PositionBinderGeneric`

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `alignment = CENTER_LEFT` em VBox filho | `VBox.getAlignment() == Pos.CENTER_LEFT` |
| 2 | `alignment = CENTER` em StackPane filho | `StackPane.setAlignment(Pos.CENTER)` |

#### 4.1.6 `PropertiesBinderGeneric`

| # | Cenário | Campo de referência | Validação |
|---|---------|---------------------|-----------|
| 1 | `focusTraversable = false` | `statusBar` | `isFocusTraversable() == false` |
| 2 | `styleClass = "status-bar"` | `statusBar` | `getStyleClass().contains("status-bar")` |
| 3 | `cursor = HAND` | `addFolderButton` | `getCursor() == Cursor.HAND` |
| 4 | `tooltip = "Abre o editor de pastas"` | `addFolderButton` | `tooltip.getText() == "..."` |
| 5 | `wrapText = true` | `explorerHeader` | `isWrapText() == true` |
| 6 | `expanded = true` em TreeItem | `featureRoot` | `isExpanded() == true` |
| 7 | `resizable = true` na `@ScreenProperties` de classe | Tela `Example` | Propriedade registrada |
| 8 | `applyToTab(props, tab)` com tooltip | `overviewTab` | Tooltip definido |
| 9 | `visible = false` → `isVisible() == false` e `isManaged() == false` | Campo fabricado | Nó oculto |

#### 4.1.7 `ElementManager`

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `createElement(TextField.class)` retorna `TextField` | Tipo correto |
| 2 | `createElement(Label.class)` com literal → texto setado | `getText() == literal` |
| 3 | `createElement(Button.class)` com literal → texto setado | `getText() == literal` |
| 4 | `addChild(BorderPane, Node, ScreenField)` com `Position.TOP` | `borderPane.getTop() == node` |
| 5 | `addChild(TabPane, Tab, ...)` | `tabPane.getTabs().contains(tab)` |
| 6 | `addChild(TreeView, TreeItem, ...)` | `treeView.getRoot() == treeItem` |
| 7 | `addChild(Pane, Node, ...)` sem posição → `getChildren().contains(node)` | Adicionado à lista |
| 8 | `createElement(MetricsCard.class)` com `custom=true` | Instância de `MetricsCard` |
| 9 | Tipo desconhecido lança exceção | `createElement(UnknownWidget.class)` → erro |

#### 4.1.8 `RegionManager`

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `createRegion(BorderPane.class)` | Retorna `BorderPane` |
| 2 | `createRegion(VBox.class)` | Retorna `VBox` |
| 3 | `createRegion(HBox.class)` | Retorna `HBox` |
| 4 | Tipo não registrado lança exceção | `createRegion(Canvas.class)` → erro |

#### 4.1.9 `ScreenManagerSharedData`

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `setScreenData` / `getScreenData` roundtrip | Valor preservado |
| 2 | `setScreenData` com chave duplicada lança exceção | Impede sobrescrita |
| 3 | `getScreenData` com chave inexistente lança exceção | Erro descritivo |
| 4 | `resetScreenData(key)` limpa dados da instância | `getCache().get(key) == null` |
| 5 | Tela `null` lança exceção | `NullPointerException` ou `IllegalArgumentException` |

#### 4.1.10 Utilitários puros (sem JavaFX)

**`StringUtils`**

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `concat("a", "b", "c")` → `"abc"` | Concatenação simples |
| 2 | `concat()` sem argumentos → `""` | Vazio |
| 3 | `concat(null, "x")` → não quebra | Tratamento de null |
| 4 | `isEmpty(null)` → `true` | |
| 5 | `isEmpty("")` → `true` | |
| 6 | `isBlank("  ")` → `true` | |
| 7 | `capitalize("hello")` → `"Hello"` | |
| 8 | `replaceParams("Nome: %1, Ativo: %2", "Proj", "Sim")` | Substituição posicional |

**`NumberUtils`**

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `toInt("42")` → `42` | Parse correto |
| 2 | `toInt("abc")` → `0` | Fallback seguro |
| 3 | `toInt(null)` → `0` | Fallback seguro |

**`DateTimeUtils`**

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `currentTimestamp()` → formato `yyyy-MM-dd HH:mm:ss` | Regex match |
| 2 | `format(LocalDateTime)` → string formatada | |
| 3 | `addDays("2026-03-01 00:00:00", 5)` → `"2026-03-06 00:00:00"` | Soma correta |
| 4 | `normalizeTimestamp(epochMillis)` → formato padrão | Conversão de epoch |

---

### Camada 2 — Teste de UI Automatizado (JavaFX Real com TestFX)

**Escopo**: subir a aplicação JavaFX com `TestFX` e simular interação do
usuário — cliques, digitação, navegação por teclado, troca de aba.

**Dependência** (a adicionar no `pom.xml`):

```xml
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-core</artifactId>
    <version>4.0.18</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.18</version>
    <scope>test</scope>
</dependency>
<!-- Para rodar headless em CI -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>openjfx-monocle</artifactId>
    <version>jdk-12.0.1+2</version>
    <scope>test</scope>
</dependency>
```

**Classe base sugerida**: `ExampleUITestBase` que faz `ScreenAssembler.compose(Example.class)` e monta uma `Scene` + `Stage` dentro de `@Start`.

**Cenários de teste**:

| #  | Cenário                                      | Ação no `FxRobot`                          | Validação                                                   |
|----|----------------------------------------------|--------------------------------------------|--------------------------------------------------------------|
| 1  | Digitar no filtro                            | `robot.clickOn("#filterInput"); robot.write("Layout")` | `filterInput.getText() == "Layout"` |
| 2  | Filtro respeita `maxLength = 50`             | `robot.write(string com 60 chars)`         | `filterInput.getText().length() <= 50` |
| 3  | Clicar em botão "Nova pasta"                 | `robot.clickOn("#addFolderButton")`        | Callback `callbackAltcamAddFolderButton` executado |
| 4  | Trocar para aba "Detalhes"                   | `robot.clickOn("Detalhes")`               | `contentTabs.getSelectionModel().getSelectedItem() == detailsTab` |
| 5  | Trocar para aba "Insights"                   | `robot.clickOn("Insights")`               | Aba de insights selecionada |
| 6  | Preencher formulário no "Detalhes"           | Digitar em `projectNameField`, `ownerField`, `versionField` | Campos preenchidos |
| 7  | Salvar com validação aprovada                | Preencher campos obrigatórios + `robot.clickOn("#saveButton")` | Sem mensagem de erro; callback executado |
| 8  | Salvar com campo obrigatório vazio           | Deixar `projectNameField` vazio + clicar "Salvar" | Validação bloqueia; mensagem de erro é exibida |
| 9  | Validação de `versionField` com valor > 99   | Digitar "100" + focar fora / clicar salvar | Rejeitado pela validação de range `minValue=1, maxValue=99` |
| 10 | Validação de `versionField` com valor < 1    | Digitar "0" + focar fora / clicar salvar   | Rejeitado |
| 11 | Clicar "Limpar" reseta formulário            | Preencher + `robot.clickOn("#clearButton")` | Todos os campos voltam ao estado inicial |
| 12 | Clicar "Recarregar dados"                    | `robot.clickOn("#refreshButton")`          | Tabela atualizada, callback invocado |
| 13 | Selecionar item da TreeView                  | `robot.clickOn("Layouts")`                 | `featureTree.getSelectionModel().getSelectedItem().getValue() == "Layouts"` |
| 14 | Duplo-clique na TreeView                     | `robot.doubleClickOn("Eventos")`           | `callbackDoubleClickFeatureTree` invocado |
| 15 | Selecionar linha na TableView                | `robot.clickOn(célula da tabela)`          | `callbackClickDatasetTable` invocado |
| 16 | Duplo-clique na TableView                    | `robot.doubleClickOn(célula da tabela)`    | `callbackDoubleClickDatasetTable` invocado |
| 17 | Navegação por Tab (foco traversal)           | `robot.press(KeyCode.TAB)` repetido        | Foco percorre campos com `focusTraversable != false` |
| 18 | Clicar "Exportar"                            | `robot.clickOn("#exportButton")`           | Callback de exportação executado |
| 19 | Cursor do botão é HAND                       | Mover mouse sobre `#addFolderButton`       | `getCursor() == Cursor.HAND` |
| 20 | CheckEntryLabel alterna estado               | `robot.clickOn(checkBox do activeToggle)`  | `activeToggle.isSelected()` alterna |

---

### Camada 3 — Teste Estrutural da Tela (sem interação)

**Escopo**: verificar que a árvore de nós montada contém exatamente os
elementos esperados, suas quantidades e posições, **sem simular cliques**.
Esses testes detectam remoções acidentais de campos ou alterações na
hierarquia.

| #  | Cenário                                         | Validação                                                         |
|----|--------------------------------------------------|-------------------------------------------------------------------|
| 1  | Raiz da tela é `BorderPane`                      | `root instanceof BorderPane`                                      |
| 2  | Bottom da raiz contém label de status            | `root.getBottom() instanceof Label`                               |
| 3  | Center da raiz contém `SplitPane`                | `root.getCenter() instanceof SplitPane`                           |
| 4  | `SplitPane` tem exatamente 2 itens               | `layoutSplit.getItems().size() == 2`                              |
| 5  | Primeiro item do split é `VBox` (explorer)       | `items.get(0) instanceof VBox`                                    |
| 6  | Segundo item do split é `TabPane`                | `items.get(1) instanceof TabPane`                                 |
| 7  | `TabPane` tem 3 abas                             | `contentTabs.getTabs().size() == 3`                               |
| 8  | Abas na ordem: "Visão geral", "Detalhes", "Insights" | Verificar `getText()` de cada Tab na posição esperada         |
| 9  | `explorerPanel` contém 4 filhos diretos          | Header (Label), filterInput (TextField), featureTree (TreeView), addFolderButton (Button) |
| 10 | `featureTree` tem raiz "Framework" expandida     | `featureTree.getRoot().getValue() == "Framework"` e `isExpanded()` |
| 11 | Raiz da árvore tem 7 filhos                      | layoutNode..utilsNode                                              |
| 12 | `overviewContainer` existe dentro da aba "Visão geral" | Nó localizável |
| 13 | `datasetTable` tem 4 colunas                     | projectNameColumn, projectTypeColumn, projectStatusColumn, projectUpdatedColumn |
| 14 | Textos das colunas: "Nome", "Tipo", "Status", "Atualizado em" | `getText()` de cada `TableColumn` |
| 15 | `detailsContainer` presente na aba "Detalhes"    | Nó localizável                                                    |
| 16 | Formulário de detalhes contém campos esperados    | `projectNameField`, `ownerField`, `versionField`, `activeToggle`, `descriptionInput`, `saveButton`, `clearButton` |
| 17 | `insightsContainer` presente na aba "Insights"   | Nó localizável                                                    |
| 18 | `metricsCard` é instância de `MetricsCard`       | `instanceof MetricsCard` e `instanceof VBox`                     |
| 19 | `eventLogList` dentro de `eventLogSection`       | Hierarquia preservada                                             |
| 20 | Todos os 41 campos possuem ID setado via acronym | Iterar campos e verificar `node.getId() != null`                 |
| 21 | `delimiterPreviewPane` é `StackPane`             | Tipo correto e presente                                           |
| 22 | `tableActions` contém 2 botões (refresh + export)| `HBox` com 2 filhos do tipo `Button`                             |

---

### Camada 4 — Teste de Contrato do Controller

**Escopo**: garantir que a classe declarada em `@Screen(callbacks = ...)`
possui todos os métodos esperados pela convenção de nomes, e que
`CallbackInvoker` os encontra e executa.

#### 4.4.1 `CallbackInvoker`

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | `buildName("config", "Example")` → `"callbackConfigExample"` | Convenção de nome |
| 2 | `buildName("altcam", "saveButton")` → `"callbackAltcamSaveButton"` | |
| 3 | `buildName("entcam", "filterInput")` → `"callbackEntcamFilterInput"` | |
| 4 | `exists(controller, "config", "Example")` → `true` | Método existe no `ExampleController` |
| 5 | `exists(controller, "altcam", "saveButton")` → `true` | |
| 6 | `exists(controller, "click", "campoInexistente")` → `false` | Método não existe |
| 7 | `call(controller, screen, "config", "Example")` executa sem erro | Invocação reflexiva funcional |

#### 4.4.2 Completude do `ExampleController`

Verificar que para cada `@ScreenField` com eventos aplicáveis, existe o método
de callback correspondente no controller. Os callbacks documentados no
`ExampleController` são:

| Callback esperado                           | Existe? |
|---------------------------------------------|---------|
| `callbackConfigExample`                     | ✓       |
| `callbackAltcamFilterInput`                 | ✓       |
| `callbackTecladFilterInput`                 | ✓       |
| `callbackAltcamRefreshButton`               | ✓       |
| `callbackAltcamExportButton`                | ✓       |
| `callbackAltcamAddFolderButton`             | ✓       |
| `callbackAltcamFeatureTree`                 | ✓       |
| `callbackClickFeatureTree`                  | ✓       |
| `callbackDoubleClickFeatureTree`            | ✓       |
| `callbackAltcamDatasetTable`                | ✓       |
| `callbackClickDatasetTable`                 | ✓       |
| `callbackDoubleClickDatasetTable`           | ✓       |
| `callbackAltcamSaveButton`                  | ✓       |
| `callbackAltcamClearButton`                 | ✓       |

**Teste parametrizado**: iterar os acronyms de `Example` que possuem tipo com
eventos (Button, TextField, TreeView, TableView, ListView, Tab, CheckBox) e
verificar via `CallbackInvoker.exists()` que pelo menos o callback `altcam`
está definido.

#### 4.4.3 Eventos ligados

| # | Cenário | Validação |
|---|---------|-----------|
| 1 | Após `compose(Example.class)`, `EventBinder` registra listeners | `EVENT_MAP` contém a tela e seus nós |
| 2 | `EventBinder.deleteEvents(screenInstance)` remove todos | Map fica vazio para aquela instância |
| 3 | Cada binder retorna runnables de desregistro | `List<Runnable>` não vazio |

---

### Camada 5 — Snapshot Testing (Nível Avançado)

**Escopo**: renderizar a cena montada em imagem e comparar com referência,
detectando mudanças visuais inesperadas.

**Abordagem**:

1. Usar `Scene.snapshot(WritableImage)` do JavaFX para capturar a tela renderizada.
2. Armazenar imagem de referência em `src/test/resources/snapshots/`.
3. Em cada execução, gerar nova imagem e comparar pixel a pixel com tolerância
   configurável (e.g. 0,5% de diferença permitida).
4. Se a diferença exceder o threshold, o teste falha e salva a imagem gerada
   em `target/snapshot-failures/` para inspeção manual.
5. Para atualizar as referências, rodar com flag `-DupdateSnapshots=true`.

**Cenários**:

| # | Cenário | Cena | Resolução |
|---|---------|------|-----------|
| 1 | Snapshot da tela de exemplo completa | `Example` montado em 1280×780 | 1280×780 |
| 2 | Snapshot da aba "Visão geral" | Aba ativa: overviewTab | Recortada |
| 3 | Snapshot da aba "Detalhes" | Aba ativa: detailsTab | Recortada |
| 4 | Snapshot da aba "Insights" | Aba ativa: insightsTab | Recortada |
| 5 | Snapshot do painel explorer | Recorte do VBox esquerdo | Recortada |

**Utilitário sugerido**:

```java
public final class SnapshotTestHelper {

    /**
     * Compara uma imagem renderizada com a referência armazenada.
     *
     * @param scene       cena a ser capturada
     * @param snapshotId  identificador do snapshot (nome do arquivo sem extensão)
     * @param threshold   porcentagem máxima de pixels diferentes (0.0 a 1.0)
     */
    public static void assertSnapshotMatches(Scene scene, String snapshotId, double threshold) {
        // Implementação: captura, compara, falha ou atualiza
    }
}
```

---

## 5. Dependências e Configuração

### 5.1 Novas dependências (`pom.xml`)

```xml
<!-- TestFX Core -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-core</artifactId>
    <version>4.0.18</version>
    <scope>test</scope>
</dependency>

<!-- TestFX JUnit 5 -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.18</version>
    <scope>test</scope>
</dependency>

<!-- Monocle para execução headless -->
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>openjfx-monocle</artifactId>
    <version>jdk-12.0.1+2</version>
    <scope>test</scope>
</dependency>
```

### 5.2 Configuração do `maven-surefire-plugin`

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <argLine>
            --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
            --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
            -Djava.awt.headless=true
            -Dtestfx.robot=glass
            -Dtestfx.headless=true
            -Dprism.order=sw
            -Dprism.text=t2k
        </argLine>
    </configuration>
</plugin>
```

### 5.3 `module-info.java` de teste

Criar `src/test/java/module-info.java` (ou usar `--add-opens` no surefire)
para permitir acesso reflexivo aos pacotes internos do framework durante os
testes.

### 5.4 Estrutura de diretórios

```
src/test/
├── java/
│   └── com/connectasistemas/framework/
│       ├── core/                          # Camada 1 — Motor
│       │   ├── AnnotationProcessorTest.java
│       │   ├── ScreenMetadataTest.java
│       │   ├── ScreenAssemblerTest.java
│       │   ├── SizeBinderGenericTest.java
│       │   ├── PositionBinderGenericTest.java
│       │   ├── PropertiesBinderGenericTest.java
│       │   ├── ElementManagerTest.java
│       │   ├── RegionManagerTest.java
│       │   └── ScreenManagerSharedDataTest.java
│       ├── util/                          # Camada 1 — Utilitários puros
│       │   ├── StringUtilsTest.java
│       │   ├── NumberUtilsTest.java
│       │   └── DateTimeUtilsTest.java
│       ├── ui/                            # Camada 2 — TestFX
│       │   ├── ExampleUITestBase.java     # Classe base com @Start
│       │   ├── FilterInteractionTest.java
│       │   ├── TabNavigationTest.java
│       │   ├── FormValidationTest.java
│       │   ├── TreeViewInteractionTest.java
│       │   ├── TableViewInteractionTest.java
│       │   └── ButtonActionsTest.java
│       ├── structure/                     # Camada 3 — Estrutural
│       │   ├── ExampleScreenStructureTest.java
│       │   └── HierarchyIntegrityTest.java
│       ├── contract/                      # Camada 4 — Contratos
│       │   ├── CallbackInvokerTest.java
│       │   ├── ExampleControllerContractTest.java
│       │   └── EventBindingTest.java
│       └── snapshot/                      # Camada 5 — Snapshots
│           ├── SnapshotTestHelper.java
│           └── ExampleSnapshotTest.java
└── resources/
    └── snapshots/                         # Imagens de referência
        ├── example-full.png
        ├── example-overview-tab.png
        ├── example-details-tab.png
        ├── example-insights-tab.png
        └── example-explorer-panel.png
```

---

## 6. Regras de Implementação

1. **Nenhum teste deve depender de tempo ou estado global mutável** — limpar
   `ScreenManagerSharedData`, `ScreenControllerRegistry`,
   `ScreenHierarchyRegistry` e `Status` no `@BeforeEach` / `@AfterEach`.

2. **Testes da Camada 1 que instanciam `Node`s precisam do toolkit JavaFX** —
   usar o padrão `@ExtendWith(ApplicationExtension.class)` do TestFX ou
   inicializar o toolkit manualmente com `Platform.startup(() -> {})` em
   `@BeforeAll`.

3. **Testes da Camada 1 que são puramente lógicos** (StringUtils, NumberUtils,
   DateTimeUtils, CallbackInvoker.buildName) **não devem inicializar JavaFX**.

4. **Isolamento** — cada teste deve montar sua própria tela via
   `ScreenAssembler.compose()` e limpar o cache compartilhado ao final.

5. **Nomenclatura** — seguir o padrão `should<Ação>When<Condição>`:
   - `shouldBuildBorderPaneAsRoot`
   - `shouldApplyPaddingToExplorerPanel`
   - `shouldRejectVersionAboveMax`

6. **Linguagem dos comentários e mensagens de assertion** — PT-BR, conforme
   diretriz do projeto.

7. **Não usar `String.format`** para mensagens — usar `StringUtils.concat()`.

8. **Assertions descritivas** — ao usar `assertEquals` / `assertTrue`,
   sempre informar a mensagem em caso de falha.

---

## 7. Prioridade e Faseamento

| Fase | Camada(s) | Estimativa | Pré-requisito |
|------|-----------|------------|---------------|
| **Fase 1** | Utilitários puros (StringUtils, NumberUtils, DateTimeUtils) | 1 dia | Nenhum |
| **Fase 2** | Core — AnnotationProcessor, ScreenMetadata | 2 dias | Fase 1 |
| **Fase 3** | Core — ScreenAssembler, ElementManager, RegionManager, SharedData | 3 dias | Fase 2 |
| **Fase 4** | Core — SizeBinder, PositionBinder, PropertiesBinder | 2 dias | Fase 3 |
| **Fase 5** | Contratos — CallbackInvoker, ExampleController, EventBinding | 2 dias | Fase 3 |
| **Fase 6** | Estrutural — Árvore de nós da Example | 1 dia | Fase 3 |
| **Fase 7** | Validação — ValidationBinderGeneric (regras de dados) | 3 dias | Fase 4 |
| **Fase 8** | UI — TestFX (interação com Example) | 4 dias | Fase 6 + TestFX configurado |
| **Fase 9** | Snapshots | 2 dias | Fase 8 |

**Total estimado**: ~20 dias de desenvolvimento.

---

## 8. Critérios de Aceitação

| # | Critério | Como verificar |
|---|----------|----------------|
| CA1 | `mvnw -q test` passa com 0 falhas | Build local + CI |
| CA2 | Cobertura ≥ 80% do pacote `internal` | Relatório JaCoCo (a configurar) |
| CA3 | Cobertura 100% do pacote `utils` (utilitários puros) | Relatório JaCoCo |
| CA4 | Testes de UI rodam headless em CI Linux/Windows | Surefire com Monocle |
| CA5 | Nenhum teste depende de outro (execução isolada) | `mvnw -Dtest=<Classe> test` |
| CA6 | Snapshot de referência gerado para `Example` | Arquivo `.png` no repositório |
| CA7 | Documentação inline em PT-BR em todos os testes | Code review |

---

## 9. Riscos e Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| Estado global estático (`ScreenManager`, `Status`, caches) causa interferência entre testes | Falhas intermitentes | `@BeforeEach` / `@AfterEach` limpam todo estado; classes de teste usam `@TestInstance(PER_CLASS)` apenas quando necessário |
| `ScreenAssembler.compose()` dispara `CallbackInvoker.call("config", ...)` que pode depender de Stage válido | `NullPointerException` em teste | Criar `ExampleController` tolerante a Stage nulo; ou mockear `ScreenManager.getMainStage()` |
| TestFX + Monocle incompatível com Java 17 modular | Testes de UI não rodam | Usar `--add-opens` extensivos no surefire; testar com versão estável do Monocle |
| Snapshots diferem entre OS (renderização de fonte) | Falsos positivos | Threshold generoso (1-2%); rodar snapshots apenas em ambiente controlado (CI com fonte fixa) |
| `WeakHashMap` nos caches pode coletar referências durante teste | Dados desaparecem | Manter referências fortes (variável local) durante o teste |

---

## 10. Métricas de Acompanhamento

| Métrica | Ferramenta | Meta |
|---------|------------|------|
| Quantidade de testes | JUnit Surefire Report | ≥ 120 testes ao final da Fase 9 |
| Cobertura de linha | JaCoCo | ≥ 80% geral |
| Tempo de execução total dos testes | Surefire | < 60s em CI |
| Taxa de flakiness | Histórico de CI | < 2% |

---

## 11. Classes do Framework — Mapa de Cobertura

Tabela consolidada de todas as classes-alvo e a camada de teste primária:

| Classe | Pacote | Camada primária | Camada secundária |
|--------|--------|-----------------|-------------------|
| `AnnotationProcessor` | `internal.processor` | 1 | — |
| `ScreenMetadata` | `internal.utils` | 1 | — |
| `ScreenAssembler` | `internal.utils` | 1 | 3 |
| `ElementManager` | `utils` | 1 | — |
| `RegionManager` | `internal.utils` | 1 | — |
| `ScreenManagerSharedData` | `internal.utils` | 1 | — |
| `SizeBinderGeneric` | `internal.sizes` | 1 | — |
| `SizeBinderImageView` | `internal.sizes` | 1 | — |
| `PositionBinderGeneric` | `internal.position` | 1 | — |
| `BorderPanePosition` | `internal.position` | 1 | — |
| `PropertiesBinderGeneric` | `internal.utils` | 1 | — |
| `ValidationBinderGeneric` | `internal.validation` | 1, 7 | 2 |
| `CallbackInvoker` | `internal.utils` | 4 | — |
| `EventBinder` | `internal.utils` | 4 | 2 |
| `EventBinderButton` | `internal.events` | 4 | 2 |
| `EventBinderTextInputControl` | `internal.events` | 4 | 2 |
| `EventBinderCheckBox` | `internal.events` | 4 | 2 |
| `EventBinderComboBox` | `internal.events` | 4 | 2 |
| `EventBinderListView` | `internal.events` | 4 | 2 |
| `EventBinderTreeView` | `internal.events` | 4 | 2 |
| `EventBinderTableView` | `internal.events` | 4 | 2 |
| `EventBinderTableColumn` | `internal.events` | 4 | 2 |
| `EventBinderTab` | `internal.events` | 4 | 2 |
| `EventBinderTextEntryLabel` | `internal.events` | 4 | 2 |
| `EventBinderCheckEntryLabel` | `internal.events` | 4 | 2 |
| `TabVisibilityManager` | `internal.properties` | 1 | 2 |
| `ScreenManager` | `utils` | 3 | 2 |
| `ScreenControllerRegistry` | `internal.utils` | 4 | — |
| `ScreenHierarchyRegistry` | `internal.utils` | 1 | — |
| `MessageUtil` | `internal.utils` | — | 2 |
| `StringUtils` | `utils` | 1 (puro) | — |
| `NumberUtils` | `utils` | 1 (puro) | — |
| `DateTimeUtils` | `utils` | 1 (puro) | — |
| `Status` | `utils` | 1 | — |
| `TableManager` | `utils` | 3 | 2 |
| `TreeManager` | `utils` | 3 | 2 |
| `TreeTableManager` | `utils` | 3 | — |
| `TextEntryLabel` | `fxelements` | 1 | 2 |
| `CheckEntryLabel` | `fxelements` | 1 | 2 |
| `Example` | `internal.examples` | 3 | 2, 5 |
| `ExampleController` | `internal.examples` | 4 | 2 |

---

## 12. Glossário

| Termo | Definição |
|-------|-----------|
| **Acronym** | Identificador único de um campo anotado com `@ScreenField`. Usado como `fx:id`, chave de cache e sufixo nos nomes de callback. |
| **Father** | Referência ao acronym do campo-pai na hierarquia. Vazio indica que o campo é filho direto da raiz. |
| **Callback convention** | Métodos no controller seguem o padrão `callback<Evento><AcronymCapitalizado>`. Ex.: `callbackAltcamSaveButton`. |
| **Compose** | Processo de transformar uma classe anotada em árvore de nós JavaFX, executado por `ScreenAssembler.compose()`. |
| **Headless** | Execução de testes JavaFX sem display físico, usando Monocle como backend gráfico. |
| **Monocle** | Implementação headless do Glass toolkit do JavaFX, permite rodar testes de UI em servidores sem GPU/display. |
| **TestFX** | Framework de testes para JavaFX que fornece `FxRobot` para simular interação de usuário (cliques, digitação, etc.). |
| **Snapshot testing** | Técnica que captura a renderização visual de um componente e compara com imagem de referência para detectar regressões visuais. |
