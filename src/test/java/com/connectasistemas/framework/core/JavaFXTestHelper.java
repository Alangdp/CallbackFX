package com.connectasistemas.framework.core;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Auxiliar para inicializar o toolkit JavaFX em ambiente de teste headless.
 * Garante que o toolkit seja iniciado apenas uma vez, independente da ordem de
 * execução das classes de teste.
 */
public final class JavaFXTestHelper {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private JavaFXTestHelper() {
    }

    /**
     * Inicializa o toolkit JavaFX caso ainda não esteja ativo.
     * Seguro para chamadas concorrentes — apenas a primeira execução efetua
     * a inicialização.
     */
    public static void initToolkit() {
        if (INITIALIZED.compareAndSet(false, true)) {
            try {
                Platform.startup(() -> {
                    // Toolkit inicializado
                });
            } catch (IllegalStateException ignored) {
                // Toolkit já estava ativo (por outra classe de teste)
            }
        }
    }

    /**
     * Executa uma ação na thread do JavaFX e aguarda sua conclusão.
     *
     * @param action ação a ser executada no FX Application Thread
     */
    public static void runAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
