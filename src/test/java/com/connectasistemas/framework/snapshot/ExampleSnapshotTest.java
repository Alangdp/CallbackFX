package com.connectasistemas.framework.snapshot;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.core.JavaFXTestHelper;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Testes de snapshot para a tela {@link Example}.
 * Camada 5 — captura e comparação visual.
 *
 * Na primeira execução, as imagens de referência são geradas automaticamente.
 * Para atualizar referências: {@code mvnw test -DupdateSnapshots=true}
 */
class ExampleSnapshotTest {

    // Tolerância de 2% para diferenças entre plataformas (fontes, renderização)
    private static final double THRESHOLD = 0.02;

    private ScreenView view;
    private Scene scene;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        view = ScreenAssembler.compose(Example.class);

        // Cria a Scene na thread do JavaFX usando o nó raiz da tela
        // O root pode já estar no scene-graph, então usamos view.root() que é o wrapper
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Usa o metadata root que é o container externo
                Region rootRegion = view.root();
                // Se já estiver em uma cena, reutiliza
                if (rootRegion.getScene() != null) {
                    scene = rootRegion.getScene();
                } else {
                    scene = new Scene(rootRegion, 1280, 780);
                }
            } catch (Exception e) {
                // Fallback: cria Scene com um StackPane wrapper
                javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(view.root());
                scene = new Scene(wrapper, 1280, 780);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: Snapshot da tela completa ----

    @Test
    @DisplayName("Snapshot da tela Example completa em 1280x780")
    void shouldMatchFullScreenSnapshot() {
        SnapshotTestHelper.assertSnapshotMatches(scene, "example-full", THRESHOLD);
    }

    // ---- Cenário 2: Snapshot da aba Visão geral ----

    @Test
    @DisplayName("Snapshot da aba Visão geral")
    void shouldMatchOverviewTabSnapshot() {
        Example screen = (Example) view.screenInstance();
        setSelectedTabIndex(screen, 0);
        SnapshotTestHelper.assertSnapshotMatches(scene, "example-overview-tab", THRESHOLD);
    }

    // ---- Cenário 3: Snapshot da aba Detalhes ----

    @Test
    @DisplayName("Snapshot da aba Detalhes")
    void shouldMatchDetailsTabSnapshot() {
        Example screen = (Example) view.screenInstance();
        setSelectedTabIndex(screen, 1);
        SnapshotTestHelper.assertSnapshotMatches(scene, "example-details-tab", THRESHOLD);
    }

    // ---- Cenário 4: Snapshot da aba Insights ----

    @Test
    @DisplayName("Snapshot da aba Insights")
    void shouldMatchInsightsTabSnapshot() {
        Example screen = (Example) view.screenInstance();
        setSelectedTabIndex(screen, 2);
        SnapshotTestHelper.assertSnapshotMatches(scene, "example-insights-tab", THRESHOLD);
    }

    // ---- Cenário 5: Snapshot do painel explorer ----

    @Test
    @DisplayName("Snapshot do painel explorer")
    void shouldMatchExplorerPanelSnapshot() {
        // Captura a cena completa. O painel explorer é o primeiro item do SplitPane.
        // Como não podemos recortar facilmente em headless, capturamos a cena toda.
        SnapshotTestHelper.assertSnapshotMatches(scene, "example-explorer-panel", THRESHOLD);
    }

    /**
     * Seleciona uma aba no TabPane da tela na thread do JavaFX.
     */
    private void setSelectedTabIndex(Example screen, int index) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                screen.contentTabs.getSelectionModel().select(index);
                latch.countDown();
            });
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
