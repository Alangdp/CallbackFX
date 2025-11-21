package com.connectasistemas.framework.processor;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.utils.ScreenMetadata;

// Processa as anotações @Screen e @ScreenField
// Carrega o título, size, fields e a classe de callbacks

/**
 * Processa anotações @Screen e @ScreenField
 * OBS: Gerencia a criação de campos e da janela via reflexão
 */
public class AnnotationProcessor {

    // Armazena os metadados extraídos
    private final ScreenMetadata metadata = new ScreenMetadata();

    /**
     * Processa uma classe de tela anotada com @Screen
     *
     * @param cls Classe genérica a ser processada
     * @return metadata Dados/Configurações da janela
     */
    public ScreenMetadata processScreen(Class<?> cls) {

        // Verifica se a classe tem @Screen
        // OBS: Caso não possua a anotação retorna dados vazios
        if (!cls.isAnnotationPresent(Screen.class)) {
            return metadata;
        }

        // Lê a anotação @Screen
        Screen screen = cls.getAnnotation(Screen.class);

        // Define título e tamanho
        metadata.setTitle(screen.title());
        metadata.setWidth(screen.width());
        metadata.setHeight(screen.height());

        // Instância a classe de callbacks se houver declarada na anotação
        // OBS: Seguindo o MVC a classe de callbacks seria o Controller
        Class<?> cbClass = screen.callbacks();
        if (cbClass != null && cbClass != Void.class) {
            try {
                Object cbInstance = cbClass.getDeclaredConstructor().newInstance();
                metadata.setCallbackInstance(cbInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Processa campos com @ScreenField
        processFields(cls);

        return metadata;
    }

    // Processa todos os campos anotados com @ScreenField

    /**
     * Processa uma classe de tela anotada com @ScreenElement
     * OBS: Extrai os elementos da classe recebida
     * @param cls Classe genérica a ser processada
     */
    private void processFields(Class<?> cls) {
        for (var field : cls.getDeclaredFields()) {
            // Verifica se o campo possui @ScreenField
            if (!field.isAnnotationPresent(ScreenField.class)) {
                continue;
            }

            // Obtém a anotação
            ScreenField f = field.getAnnotation(ScreenField.class);

            // Adiciona no metadata
            metadata.addField(f.acronym(), field);
        }
    }
}
