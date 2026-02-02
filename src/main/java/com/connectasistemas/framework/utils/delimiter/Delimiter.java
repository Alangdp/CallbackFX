package com.connectasistemas.framework.utils.delimiter;

import java.util.function.BiConsumer;

import javafx.scene.input.DragEvent;

/**
 * Representa um recorte percentual da {@link javafx.scene.layout.Region} alvo,
 * permitindo a execução de callbacks no evento de drop.
 */
public class Delimiter {

	/**
	 * Callback padrão que não realiza nenhuma operação, usado quando o usuário não fornece ação.
	 */
	private static final BiConsumer<DragEvent, Delimiter> NO_OP = (event, delimiter) -> { };

	/** Percentual de largura relativo à {@link Region} que hospeda o overlay. */
	private final double widthPercent;
	/** Percentual de altura relativo à {@link Region} que hospeda o overlay. */
	private final double heightPercent;
	/** Offset horizontal inicial do delimitador em relação à largura total. */
	private final double startXPercent;
	/** Offset vertical inicial do delimitador em relação à altura total. */
	private final double startYPercent;
	/** Rótulo exibido no overlay para identificação visual. */
	private final String label;
	/** Função executada quando um evento de drop ocorre dentro do delimitador. */
	private final BiConsumer<DragEvent, Delimiter> onDrop;

	/**
	 * Cria um delimitador com callback, offsets percentuais e rótulo opcional.
	 *
	 * @param widthPercent percentual de largura relativo ao componente pai (0 &lt; x &le; 100)
	 * @param heightPercent percentual de altura relativo ao componente pai (0 &lt; x &le; 100)
	 * @param startXPercent deslocamento horizontal inicial em porcentagem (0 &lt;= x &lt; 100)
	 * @param startYPercent deslocamento vertical inicial em porcentagem (0 &lt;= x &lt; 100)
	 * @param label texto exibido sobre o delimitador (opcional)
	 * @param onDrop callback executado no drop; se nulo, nada será executado
	 */
	public Delimiter(
			double widthPercent,
			double heightPercent,
			double startXPercent,
			double startYPercent,
			String label,
			BiConsumer<DragEvent, Delimiter> onDrop) {

		
		// OBS: Guarda o percentual de largura configurado para orientar o layout.
		this.widthPercent = widthPercent;

		// OBS: Guarda o percentual de altura configurado para compor a área do delimitador.
		this.heightPercent = heightPercent;

		// OBS: Define o deslocamento horizontal inicial relativo à esquerda da Region.
		this.startXPercent = startXPercent;

		// OBS: Define o deslocamento vertical inicial relativo ao topo da Region.
		this.startYPercent = startYPercent;

		// OBS: Permite rótulos nulos para delimitadores apenas funcionais.
		this.label = label;

		// OBS: Garante callback padrão para eliminar verificações de nulidade durante o drop.
		this.onDrop = onDrop == null ? NO_OP : onDrop;
	}

	public Delimiter(double widthPercent, double heightPercent, double startXPercent, double startYPercent, String label) {
		this(widthPercent, heightPercent, startXPercent, startYPercent, label, NO_OP);
	}

	public Delimiter(double widthPercent, double heightPercent, double startXPercent, double startYPercent,
			BiConsumer<DragEvent, Delimiter> onDrop) {
		this(widthPercent, heightPercent, startXPercent, startYPercent, null, onDrop);
	}

	public Delimiter(double widthPercent, double heightPercent, double startXPercent, double startYPercent) {
		this(widthPercent, heightPercent, startXPercent, startYPercent, null, NO_OP);
	}

	public Delimiter(double widthPercent, double heightPercent, String label, BiConsumer<DragEvent, Delimiter> onDrop) {
		this(widthPercent, heightPercent, 0d, 0d, label, onDrop);
	}

	public Delimiter(double widthPercent, double heightPercent, BiConsumer<DragEvent, Delimiter> onDrop) {
		this(widthPercent, heightPercent, 0d, 0d, null, onDrop);
	}

	public Delimiter(double widthPercent, double heightPercent, String label) {
		this(widthPercent, heightPercent, 0d, 0d, label, NO_OP);
	}

	public Delimiter(double widthPercent, double heightPercent) {
		this(widthPercent, heightPercent, 0d, 0d, null, NO_OP);
	}

	/**
	 * @return percentual de largura configurado para o delimitador.
	 */
	public double getWidthPercent() {
		return widthPercent;
	}

	/**
	 * @return percentual de altura configurado para o delimitador.
	 */
	public double getHeightPercent() {
		return heightPercent;
	}

	/**
	 * @return deslocamento horizontal em porcentagem.
	 */
	public double getStartXPercent() {
		return startXPercent;
	}

	/**
	 * @return deslocamento vertical em porcentagem.
	 */
	public double getStartYPercent() {
		return startYPercent;
	}

	/**
	 * @return rótulo textual exibido sobre o delimitador.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return callback invocado durante o drop.
	 */
	public BiConsumer<DragEvent, Delimiter> getOnDrop() {
		return onDrop;
	}

	/**
	 * Executa o callback associado, permitindo que subclasses façam pré-processamento.
	 *
	 * @param event evento de drop recebido pelo delimitador
	 */
	public void executeDrop(DragEvent event) {
		if (event == null) return;

		// OBS: Encaminha o evento para o callback associado, preservando o contexto deste delimitador.
		onDrop.accept(event, this);
	}
}
