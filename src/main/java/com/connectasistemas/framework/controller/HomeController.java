package com.connectasistemas.framework.controller;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.view.HomeView;
import javafx.scene.input.KeyEvent;

/**
 * Controller da janela Home
 * OBS: os métodos aqui seguem a seguinte tabela de nomenclatura para os callbacks:
 * Prefixo do evento  |	Callback gerado
 * entcam	          | callbackEntcamNome
 * saicam	          | callbackSaicamNome
 * teclad	          | callbackTecladNome
 * altcam	          | callbackAltcamNome
 * valida	          | callbackValidaNome
 * prebrw	          | callbackPrebrwNome
 * posbrw	          | callbackPosbrwNome
 * outace	          | callbackOutaceNome
 * ceplgr	          | callbackCeplgrNome
 *
 * Os paragrafos geram o nome:
 *
 * callback<Prefixo do evento><Nome da variável>
 */
public class HomeController {

    /**
     * Callback de config da janela
     */
    public void callbackConfigHomeView(HomeView screen) {
        System.out.println("Config da janela");
    }

    /**
     * Callback de entrada de campo para nome
     */
    public void callbackEntcamNome(HomeView screen) {
        System.out.println("Entrou nome");
    }

    /**
     * Callback de saída de campo para nome
     */
    public void callbackSaicamNome(HomeView screen) {
        System.out.println("Saiu nome");
    }

    /**
     * Callback de teclado
     */
    public void callbackTecladNome(HomeView screen, KeyEvent e) {
        System.out.println("Tecla: " + e.getCode());
    }

    /**
     * Callback chamado após a construção da HomeView.
     */
    public void callbackInitializeHomeView(HomeView screen) {
        // Disabilita todos os elementos da tela
        ScreenManager.disableWindow(HomeView.class);

        // Habilita os botões de avançar e cancelar
        ScreenManager.enableNode(screen.advanceButton);
        ScreenManager.enableNode(screen.cancelButton);

        // Habilita os campos de chave
        ScreenManager.enableNode(screen.bookCode);
        ScreenManager.enableNode(screen.bookCreatedAt);
    }

    /**
     * Exemplo de validação: ano do livro precisa ser numérico e dentro da faixa configurada.
     */
    public void callbackValidaBookYear(HomeView screen, boolean valido, String valorDigitado) {
        System.out.printf("Validação de Ano → válido: %s, valor: %s%n", valido, valorDigitado);
    }
}
