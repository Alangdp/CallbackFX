package com.connectasistemas.framework.controller;

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
}
