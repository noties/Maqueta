package ru.noties.maqueta;

import android.support.annotation.NonNull;

// class to be used only if Maqueta.observeWith = LISTENER
public interface MaquetaListener<T> {

    void apply(@NonNull T t);
}
