# Framework de Callbacks para JavaFX

Este repositório reúne um microframework experimental para acelerar a criação de telas JavaFX dirigidas por callbacks nomeados. A ideia é aproximar o modelo do que seria um "function set": você descreve a tela com anotações, aponta um controller com métodos convencionados e o framework cuida de instanciar campos, ligar eventos e encaminhar os callbacks corretos.

## Visão geral
- **Declaração com anotações**: marque classes de tela com `@Screen` e campos com `@ScreenField` para informar título, tamanho e os componentes que devem ser exibidos.
- **Callbacks determinísticos**: em vez de lidar com múltiplos listeners JavaFX manualmente, basta implementar métodos com o prefixo esperado (ex.: `callbackEntcamNome`).
- **Binding automático de eventos**: `EventBinder` conecta os eventos JavaFX básicos (foco, alteração de texto, teclado) com os callbacks detectados.
- **Construção dinâmica de UI**: `ScreenManager` reflete as anotações e cria os componentes suportados (`TextField`, `Label`, `CheckBox` por padrão) usando `ElementManager`.

## Arquitetura rápida
1. `HelloApplication` inicializa o `ScreenManager` e solicita a troca para a primeira tela (`HomeView`).
2. `ScreenManager` instancia a view, dispara `AnnotationProcessor` e recebe um `ScreenMetadata` com título, dimensões, campos e controller.
3. Para cada campo anotado, `ElementManager` cria o `Node` correspondente e `EventBinder` registra os listeners necessários.
4. Quando um evento do JavaFX dispara, `EventBinder` delega para `CallbackInvoker`, que monta o nome do método e o executa na instância do controller.

Os principais pacotes ficam em `src/main/java/com/connectasistemas/framework`:
- `annotation`: define `@Screen` e `@ScreenField`.
- `processor`: lê as anotações e produz `ScreenMetadata`.
- `utils`: concentra `ScreenManager`, `ElementManager`, `EventBinder`, `CallbackInvoker` e `ScreenMetadata`.
- `controller` e `view`: exemplo funcional (`HomeController`/`HomeView`).

## Convenção de callbacks
Cada método do controller segue o padrão `callback<Prefixo><NomeDoCampo>`. O nome do campo vem da sigla (`acronym`) declarada no `@ScreenField`. Prefixos suportados:

| Evento | Descrição | Método esperado |
| --- | --- | --- |
| `entcam` | Foco entrou no campo | `callbackEntcamNome` |
| `saicam` | Foco saiu do campo | `callbackSaicamNome` |
| `teclad` | Tecla pressionada | `callbackTecladNome` |
| `altcam` | Texto alterado | `callbackAltcamNome` |
| `valida` | Ponto de validação (reservado) | `callbackValidaNome` |
| `prebrw`/`posbrw` | Pré/Pós abertura de browse | `callbackPrebrwNome` / `callbackPosbrwNome` |

> Os prefixos adicionais já estão previstos em `CallbackInvoker` e podem ser implementados gradualmente.

## Exemplo rápido
### View anotada
```java
@Screen(title = "Home", callbacks = HomeController.class)
public class HomeView {

	@ScreenField(acronym = "nome")
	private TextField nome;

	@ScreenField(acronym = "cend")
	private TextField endereco;
}
```

### Controller com callbacks
```java
public class HomeController {

	public void callbackEntcamNome(HomeView screen) {
		System.out.println("Entrou nome");
	}

	public void callbackSaicamNome(HomeView screen) {
		System.out.println("Saiu nome");
	}

	public void callbackTecladNome(HomeView screen, KeyEvent e) {
		System.out.println("Tecla: " + e.getCode());
	}
}
```

## Executando o projeto
Requisitos mínimos:
- JDK 17+
- Maven 3.9+ (o wrapper `mvnw` já está configurado)
- JavaFX 17 (trazido via dependências do Maven)

### Windows (PowerShell)
```powershell
cd c:\Users\alang\Documents\Feevale\EstruturaDados\framework
./mvnw.cmd clean javafx:run
```

### Linux/MacOS
```bash
cd /path/para/framework
./mvnw clean javafx:run
```

O plugin `javafx-maven-plugin` já aponta para `com.connectasistemas.framework.HelloApplication`, então o comando acima abre a tela exemplo com os `TextField`s e callbacks demonstrados.

## Como criar novas telas
1. **Declare a view**: crie uma classe em `view/`, anote com `@Screen` e liste o controller responsável em `callbacks`.
2. **Mapeie os campos**: para cada componente que deve ser manipulado, adicione `@ScreenField(acronym = "sigla")`.
3. **Implemente o controller**: coloque os métodos `callback<Prefixo><SiglaCapitalizada>` necessários. O primeiro parâmetro sempre é a instância da view; adicione parâmetros extra conforme o evento.
4. **Atualize o bootstrap**: chame `ScreenManager.changeTo(SuaView.class)` para abrir a nova tela.

## Extensões possíveis
- **Novos componentes**: registre um `Supplier<Node>` em `ElementManager.registry` para suportar outros controles JavaFX.
- **Mais eventos**: adicione novos prefixos em `EventBinder` e reutilize `CallbackInvoker` para a resolução dinâmica.
- **Layout customizado**: substituir o `HBox` padrão por containers declarados via anotação (ex.: apontar um `BorderPane` e posições específicas).
- **Validações e estados**: implementar os prefixos reservados (`valida`, `outace`, etc.) para fluxos de negócios mais completos.

## Roadmap / ideias
- Suporte a hierarquias de layout e posicionamento configuráveis.
- Registro declarativo de máscaras/bindings de dados.
- Tooling para gerar controllers a partir da convenção de nomes.
- Mecanismo de hot-reload de callbacks durante o desenvolvimento.

Contribuições, feedbacks e ideias são bem-vindos para evoluir esse "function set" inspirado em frameworks legados, mas com a flexibilidade do Java moderno.
