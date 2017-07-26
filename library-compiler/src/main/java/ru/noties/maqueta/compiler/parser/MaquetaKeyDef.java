package ru.noties.maqueta.compiler.parser;

import android.support.annotation.NonNull;
import javax.lang.model.element.Element;

import ru.noties.maqueta.Maqueta;

public class MaquetaKeyDef {

    private final Element element;

    private final String name;
    private final String defaultValue;

    private final boolean serialize;
    private final Maqueta.Type type;

    MaquetaKeyDef(
            @NonNull Element element,
            @NonNull String name,
            @NonNull String defaultValue,
            boolean serialize,
            @NonNull Maqueta.Type type
    ) {
        this.element = element;
        this.name = name;
        this.defaultValue = defaultValue;
        this.serialize = serialize;
        this.type = type;
    }

    public Element element() {
        return element;
    }

    public String name() {
        return name;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public boolean serialize() {
        return serialize;
    }

    public Maqueta.Type type() {
        return type;
    }
}
