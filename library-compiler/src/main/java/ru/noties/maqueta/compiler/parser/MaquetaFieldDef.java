package ru.noties.maqueta.compiler.parser;

import android.support.annotation.NonNull;
import javax.lang.model.element.Element;

public class MaquetaFieldDef {

    private final Element element;

    private final boolean getter;

    MaquetaFieldDef(@NonNull Element element, boolean getter) {
        this.element = element;
        this.getter = getter;
    }

    @NonNull
    public Element element() {
        return element;
    }

    public boolean getter() {
        return getter;
    }

    @Override
    public String toString() {
        return "MaquetaFieldDef{" +
                "element=" + element +
                ", getter=" + getter +
                '}';
    }
}
