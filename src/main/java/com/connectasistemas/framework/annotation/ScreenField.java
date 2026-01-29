package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.Position;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma variável da classe que o framework ira tratar com campo
 * OBS: Isso que marca o campo com callbacks genéricos
 * 
 * Exemplo:
 * 
 * @ScreenField(acronym = "btnSave", father = "topPane", position = Position.LEFT, order = 1)
 * private Button btnSave;
 * 
 * Isso diz para o framework que a variável btnSave é um botão que estará na região LEFT do pai topPane...
 * ...isso faz com que a renderizar a tela as váriavel já esteja instanciada e pronta para uso...
 * ...além disso, o framework irá procurar por métodos de callback na classe de callbacks.
 * 
 * OBS: Os callbacks variam de acordo com o tipo do elemento
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenField {

    // Acrônimo do elemento na tela
    String acronym();

    // Acrônimo do elemento pai na tela 
    // OBS: Caso não possuir pai será adicionado na raiz do layout
    String father() default "";

    // Define a posição do elemento em relação ao pai
    // OBS: Varia de acordo com o layout do pai
    Position position() default Position.CENTER;

    // Literal do elemento (se aplicável)
    String literal() default "";

    // Ordem do elemento na criação da tela
    // OBS: As anotações do java não vem em ordem durante o processamento...
    // ...campos com valor zero serão processados como últimos
    int order() default 0;

    // Indica que o campo representa um elemento customizado (implementa CustomElement)
    boolean custom() default false;
}