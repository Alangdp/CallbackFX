package com.connectasistemas.framework.snapshot;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Utilitário para testes de snapshot.
 * Captura a cena renderizada e compara com imagem de referência.
 */
public final class SnapshotTestHelper {

    // Diretório das imagens de referência
    private static final Path REFERENCE_DIR = Path.of("src", "test", "resources", "snapshots");

    // Diretório para falhas
    private static final Path FAILURE_DIR = Path.of("target", "snapshot-failures");

    private SnapshotTestHelper() {
    }

    /**
     * Compara uma imagem renderizada com a referência armazenada.
     *
     * @param scene      cena a ser capturada
     * @param snapshotId identificador do snapshot (nome do arquivo sem extensão)
     * @param threshold  porcentagem máxima de pixels diferentes (0.0 a 1.0)
     */
    public static void assertSnapshotMatches(Scene scene, String snapshotId, double threshold) {
        boolean updateMode = Boolean.getBoolean("updateSnapshots");

        WritableImage captured = captureScene(scene);
        if (captured == null) {
            fail("Não foi possível capturar snapshot da cena");
            return;
        }

        BufferedImage capturedBuf = toBufferedImage(captured);
        Path referenceFile = REFERENCE_DIR.resolve(snapshotId + ".png");

        // Modo de atualização: salva a imagem como nova referência
        if (updateMode || !Files.exists(referenceFile)) {
            saveImage(capturedBuf, referenceFile);
            return;
        }

        // Carrega referência e compara
        BufferedImage referenceBuf;
        try {
            referenceBuf = ImageIO.read(referenceFile.toFile());
        } catch (IOException e) {
            fail("Não foi possível ler imagem de referência: " + referenceFile);
            return;
        }

        double diffPercent = compareImages(referenceBuf, capturedBuf);

        if (diffPercent > threshold) {
            // Salva a imagem gerada para inspeção
            saveImage(capturedBuf, FAILURE_DIR.resolve(snapshotId + "-actual.png"));
            fail("Snapshot '" + snapshotId + "' diferiu em " +
                    String.format("%.2f%%", diffPercent * 100) +
                    " (threshold: " + String.format("%.2f%%", threshold * 100) + ")");
        }
    }

    /**
     * Captura a cena JavaFX como WritableImage na thread do JavaFX.
     */
    static WritableImage captureScene(Scene scene) {
        if (scene == null) {
            return null;
        }

        AtomicReference<WritableImage> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                WritableImage image = scene.snapshot(null);
                ref.set(image);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ref.get();
    }

    /**
     * Converte WritableImage do JavaFX para BufferedImage do AWT.
     */
    static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buf.setRGB(x, y, reader.getArgb(x, y));
            }
        }

        return buf;
    }

    /**
     * Compara duas imagens pixel a pixel.
     *
     * @return fração de pixels diferentes (0.0 a 1.0)
     */
    static double compareImages(BufferedImage reference, BufferedImage actual) {
        int width = Math.min(reference.getWidth(), actual.getWidth());
        int height = Math.min(reference.getHeight(), actual.getHeight());
        int totalPixels = Math.max(reference.getWidth(), actual.getWidth())
                * Math.max(reference.getHeight(), actual.getHeight());

        if (totalPixels == 0) {
            return 0.0;
        }

        int diffCount = 0;

        // Conta pixels diferentes por tamanho diferente
        diffCount += Math.abs(reference.getWidth() * reference.getHeight() - width * height);

        // Compara pixels na área comum
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (reference.getRGB(x, y) != actual.getRGB(x, y)) {
                    diffCount++;
                }
            }
        }

        return (double) diffCount / totalPixels;
    }

    /**
     * Salva a imagem no caminho informado, criando diretórios se necessário.
     */
    private static void saveImage(BufferedImage image, Path path) {
        try {
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException e) {
            fail("Não foi possível salvar imagem: " + path);
        }
    }
}
