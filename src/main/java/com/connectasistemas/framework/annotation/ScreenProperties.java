package com.connectasistemas.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.connectasistemas.framework.enums.CursorType;

/**
 * Propriedades genéricas aplicáveis a telas ou componentes anotados.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ScreenProperties {

	// Define se o elemento inicia habilitado.
	boolean enabled() default true;

	// Define se o elemento deve iniciar visível e gerenciado pelo layout.
	boolean visible() default true;

	// Controla se o elemento participa do layout ao iniciar.
	boolean managed() default true;

	// Controla se o elemento aceita foco de teclado ao iniciar.
	boolean focusTraversable() default true;

	// Solicita foco logo após a criação.
	boolean focusOnLoad() default false;

	// Indica se o Stage pode ser redimensionado (quando aplicável).
	boolean resizable() default true;

	// Opacidade inicial (1.0 = opaco, 0.0 = invisível).
	double opacity() default 1.0;

	// Deixa o nó transparente para eventos de mouse.
	boolean mouseTransparent() default false;

	// Considera os limites do nó para seleção de clique.
	boolean pickOnBounds() default false;

	// Ativa cache de renderização do nó.
	boolean cache() default false;

	// Controla edição em campos de texto.
	boolean editable() default true;

	// Quebra de linha automática em labels ou áreas de texto.
	boolean wrapText() default false;

	// Estilo inline (CSS).
	String style() default "";

	// Lista de classes CSS separadas por espaço.
	String styleClass() default "";

	// ID do nó (Não recomendável alterar, pois o framework pode usar para algumas funções).
	String id() default "";

	// Tooltip textual.
	String tooltip() default "";

	// Cursor CSS (ex.: hand, crosshair).
	CursorType cursor() default CursorType.DEFAULT;

	// Rotação inicial em graus.
	double rotate() default 0.0;

	// Escala inicial.
	double scaleX() default 1.0;

	double scaleY() default 1.0;

	double scaleZ() default 1.0;

	// Translação inicial.
	double translateX() default 0.0;

	double translateY() default 0.0;

	double translateZ() default 0.0;

	// Flags adicionais para Stage.
	boolean fullScreen() default false;

	boolean maximized() default false;

	boolean alwaysOnTop() default false;
}
