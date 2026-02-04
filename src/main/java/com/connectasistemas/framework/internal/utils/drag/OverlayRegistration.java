package com.connectasistemas.framework.internal.utils.drag;

import java.util.concurrent.atomic.AtomicReference;
import com.connectasistemas.framework.utils.delimiter.RegionOverlayPane;

/**
 * Registro interno que associa o overlay e a seleção ativa usados pelo DragUtils.
 */
public record OverlayRegistration(RegionOverlayPane overlay, AtomicReference<String> selection) {
}
