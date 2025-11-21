package com.connectasistemas.framework.interfaces;

import java.util.List;

/**
 * Interface abstrata para aplicar eventos em Nodes do JavaFX
 * entcam            | callbackEntcamNome
 * saicam            | callbackSaicamNome
 * teclad            | callbackTecladNome
 * altcam            | callbackAltcamNome
 */
public abstract class EventBinderEvents {

    public abstract List<Runnable> applyEntcamSaicamEvent();
    public abstract List<Runnable> applyTecladEvent();
    public abstract List<Runnable> applyAltcamEvent();
}
