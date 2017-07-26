package ru.noties.maqueta.compiler;

import android.support.annotation.NonNull;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class MaquetaLogger {

    private final String tag;
    private final Messager messager;

    public MaquetaLogger(@NonNull String tag, @NonNull ProcessingEnvironment environment) {
        this.tag = tag;
        this.messager = environment.getMessager();
    }

    public void error(@NonNull String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, formatMessage(tag, message, args));
    }

    public void error(@NonNull Element element, @NonNull String message, Object... args) {
        error(message, args);
        messager.printMessage(Diagnostic.Kind.ERROR, "", element);
    }

//    public void warn(@NonNull String message, Object... args) {
//        messager.printMessage(Diagnostic.Kind.WARNING, formatMessage(tag, message, args));
//    }
//
//    public void warn(@NonNull Element element, @NonNull String message, Object... args) {
//        warn(message, args);
//        messager.printMessage(Diagnostic.Kind.WARNING, "", element);
//    }

    @SuppressWarnings("WeakerAccess")
    public void note(@NonNull String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, formatMessage(tag, message, args));
    }

//    public void note(@NonNull Element element, @NonNull String message, Object... args) {
//        note(message, args);
//        messager.printMessage(Diagnostic.Kind.NOTE, "", element);
//    }

    private static String formatMessage(@NonNull String tag, @NonNull String message, Object... args) {
        return String.format("[%s] %s", tag, (String.format(message, args)));
    }
}
