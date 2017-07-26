package ru.noties.maqueta.compiler.parser;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import javax.lang.model.element.TypeElement;

import ru.noties.maqueta.Maqueta;

public class MaquetaTypeDef {


    private final TypeElement element;

    private final String packageName;
    private final String className;

    private final Maqueta.ObserveWith observeWith;

    private final List<MaquetaFieldDef> fields;
    private final List<MaquetaKeyDef> keys;


    MaquetaTypeDef(
            @NonNull TypeElement element,
            @NonNull String packageName,
            @NonNull String className,
            @NonNull Maqueta.ObserveWith observeWith,
            @Nullable List<MaquetaFieldDef> fields,
            @NonNull List<MaquetaKeyDef> keys
    ) {
        this.element = element;
        this.packageName = packageName;
        this.className = className;
        this.observeWith = observeWith;
        this.fields = fields;
        this.keys = keys;
    }

    @SuppressWarnings("unused")
    @NonNull
    public TypeElement element() {
        return element;
    }

    @NonNull
    public String packageName() {
        return packageName;
    }

    @NonNull
    public String className() {
        return className;
    }

    @NonNull
    public Maqueta.ObserveWith observeWith() {
        return observeWith;
    }

    @Nullable
    public List<MaquetaFieldDef> fields() {
        return fields;
    }

    @NonNull
    public List<MaquetaKeyDef> keys() {
        return keys;
    }
}
