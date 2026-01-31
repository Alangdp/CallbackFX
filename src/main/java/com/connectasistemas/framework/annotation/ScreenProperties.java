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

	/**
	 * Define se o elemento inicia habilitado.
	 */
	boolean enabled() default true;

	/**
	 * Controla a visibilidade inicial (visível e managed).
	 */
	boolean visible() default true;

	/**
	 * Determina se o nó participa do layout ao iniciar.
	 */
	boolean managed() default true;

	/**
	 * Permite foco de teclado logo após a criação.
	 */
	boolean focusTraversable() default true;

	/**
	 * Solicita foco automaticamente depois da montagem.
	 */
	boolean focusOnLoad() default false;

	/**
	 * Habilita redimensionamento do Stage associado.
	 */
	boolean resizable() default true;

	/**
	 * Opacidade inicial (1.0 opaco, 0.0 invisível).
	 */
	double opacity() default 1.0;

	/**
	 * Ignora eventos de mouse no nó.
	 */
	boolean mouseTransparent() default false;

	/**
	 * Permite cliques mesmo fora da forma do nó (usa bounds).
	 */
	boolean pickOnBounds() default false;

	/**
	 * Ativa cache de renderização para otimizar performance.
	 */
	boolean cache() default false;

	/**
	 * Permite edição em componentes editáveis.
	 */
	boolean editable() default true;

	/**
	 * Ativa quebra automática em labels/texto.
	 */
	boolean wrapText() default false;

	/**
	 * CSS inline aplicado diretamente no nó.
	 */
	String style() default "";

	/**
	 * Lista de classes CSS separadas por espaço.
	 */
	String styleClass() default "";

	/**
	 * ID do nó; altere apenas quando necessário.
	 */
	String id() default "";

	/**
	 * Tooltip textual exibido ao passar o mouse.
	 */
	String tooltip() default "";

	/**
	 * Cursor usado ao sobrepor o elemento.
	 */
	CursorType cursor() default CursorType.DEFAULT;

	/**
	 * Rotação inicial em graus.
	 */
	double rotate() default 0.0;

	/**
	 * Escala horizontal inicial.
	 */
	double scaleX() default 1.0;

	/**
	 * Escala vertical inicial.
	 */
	double scaleY() default 1.0;

	/**
	 * Escala de profundidade inicial.
	 */
	double scaleZ() default 1.0;

	/**
	 * Translação horizontal inicial.
	 */
	double translateX() default 0.0;

	/**
	 * Translação vertical inicial.
	 */
	double translateY() default 0.0;

	/**
	 * Translação em Z inicial.
	 */
	double translateZ() default 0.0;

	/**
	 * Mostra ou oculta o root em TreeView.
	 */
	boolean showRoot() default true;

	/**
	 * Expande automaticamente TreeItems anotados.
	 */
	boolean expanded() default true;

	/**
	 * Abre a janela em modo tela cheia.
	 */
	boolean fullScreen() default false;

	/**
	 * Maximiza o Stage após a criação.
	 */
	boolean maximized() default false;

	/**
	 * Mantém o Stage acima das demais janelas.
	 */
	boolean alwaysOnTop() default false;
}
