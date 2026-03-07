# CallbackFX

Framework JavaFX declarativo (Java 17) que monta telas a partir de anotações — sem FXML.

Toda a composição de hierarquia, tamanhos, posições, propriedades visuais, validação de dados e bindagem de eventos é resolvida em tempo de execução via reflexão.

## Requisitos

- **Java 17+**

## Anotações Principais

| Anotação | Função |
|---|---|
| `@Screen` | Define uma tela (título, dimensões, controller de callbacks) |
| `@ScreenField` | Declara um campo/componente na tela (tipo, pai, ordem, acronym) |
| `@ScreenFieldSize` | Dimensões do campo (width, height, padding, spacing, grow) |
| `@ScreenFieldPosition` | Posicionamento (alignment, position no pai) |
| `@ScreenProperties` | Propriedades visuais (styleClass, cursor, tooltip, visible, etc.) |
| `@ScreenValidation` | Regras de validação (maxLength, ranges, obrigatoriedade) |

## Build e Execução

```bash
# Compilar
./mvnw compile

# Executar a aplicação de exemplo
./mvnw javafx:run

# Rodar os testes (headless)
./mvnw test
```

## Testes

O projeto possui 5 camadas de testes automatizados:

| Camada | Escopo |
|---|---|
| 1 — Core | Pipeline de anotações, metadata, binders |
| 2 — UI | Interação simulada com TestFX |
| 3 — Estrutura | Hierarquia de nós da tela montada |
| 4 — Contrato | Convenção de callbacks do controller |
| 5 — Snapshot | Comparação visual por captura de imagem |

### Snapshot Testing

Os testes de snapshot capturam a cena renderizada e comparam com imagens de referência armazenadas por sistema operacional em:

```
src/test/resources/snapshots/
  windows/
  linux/
  mac/
```

#### Gerar snapshots pela primeira vez

Na primeira execução em um novo sistema operacional, as imagens de referência são criadas automaticamente — basta rodar os testes:

```bash
./mvnw test -pl . -Dtest="*SnapshotTest"
```

O diretório correspondente ao OS (ex: `snapshots/linux/`) será criado com as imagens.

#### Atualizar snapshots existentes

Se houve uma mudança visual intencional e as referências precisam ser regeneradas:

```bash
./mvnw test -DupdateSnapshots=true
```

Isso sobrescreve as imagens de referência do OS atual com as novas capturas.

#### Como funciona

- Cada OS mantém suas próprias imagens de referência, evitando falhas por diferenças de renderização de fontes e anti-aliasing entre plataformas.
- A comparação usa tolerância por canal de cor (10/255) para absorver variações menores de sub-pixel entre máquinas do mesmo OS.
- Imagens que falharam na comparação são salvas em `target/snapshot-failures/` para inspeção.
- O threshold padrão é de **2%** de pixels diferentes.

## Estrutura do Projeto

```
src/
  main/java/com/connectasistemas/framework/
    annotation/      # @Screen, @ScreenField, etc.
    enums/           # Position, Cursor, enums do framework
    fxelements/      # Componentes customizados (MetricsCard, etc.)
    interfaces/      # Contratos públicos (CustomElement, etc.)
    internal/        # Motor de composição (processor, binders, events)
    utils/           # Utilitários (drag-and-drop, delimiters, etc.)
  test/java/com/connectasistemas/framework/
    core/            # Testes do motor (camada 1)
    structure/       # Testes estruturais (camada 3)
    contract/        # Testes de contrato (camada 4)
    snapshot/        # Testes de snapshot (camada 5)
    util/            # Testes de utilitários
docs/                # PRDs e documentação interna
```
