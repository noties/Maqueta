package ru.noties.maqueta.compiler.writer.generator.observewith;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import ru.noties.maqueta.MaquetaListener;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.SourceCodeUtils;

class ObservableListener extends AbsObservable {

    private final TypeName typeListener;

    private final FieldSpec listeners;
    private final FieldSpec iterating;
    private final FieldSpec removeAll;
    private final MethodSpec checkIfShouldUnregister;

    ObservableListener(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        super(environment, typeDef);

        typeListener = ParameterizedTypeName.get(
                ClassName.get(MaquetaListener.class),
                generatedClassName
        );

        this.listeners = listenersField();

        this.iterating = FieldSpec.builder(TypeName.BOOLEAN, "observeWithListenerIterating")
                .addModifiers(Modifier.PRIVATE)
                .build();

        this.removeAll = FieldSpec.builder(TypeName.BOOLEAN, "observeWithListenerRemoveAll")
                .addModifiers(Modifier.PRIVATE)
                .build();

        this.checkIfShouldUnregister = MethodSpec.methodBuilder("observeWithListenerCheckIfShouldUnregister")
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("synchronized ($1N)", listeners.name)
                .beginControlFlow("if ($1N.size() == 0)", listeners.name)
                .addStatement("$1N()", UNREGISTER_NAME)
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    @Override
    @NonNull
    MethodSpec notifyChange() {
        return MethodSpec.methodBuilder("observeWithListenerNotifyChange")
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("synchronized ($1N)", listeners.name)
                .addStatement("this.$1N = true", iterating.name)
                .beginControlFlow("for (int i = $1N.size() - 1; i > -1; i--)", listeners.name)
                .addStatement("$1N.get($2N).apply(this)", listeners.name, "i")
                .endControlFlow()
                .beginControlFlow("if ($1N)", removeAll.name)
                .addStatement("$1N.clear()", listeners.name)
                .addStatement("this.$1N = false", removeAll.name)
                .addStatement("$1N()", checkIfShouldUnregister.name)
                .endControlFlow()
                .addStatement("this.$1N = false", iterating.name)
                .endControlFlow()
                .build();
    }

    @NonNull
    @Override
    List<FieldSpec> fields() {
        return Arrays.asList(listeners, iterating, removeAll);
    }

    @NonNull
    @Override
    List<MethodSpec> methods() {

        final MethodSpec register = MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SourceCodeUtils.makeNonNullParameter(typeListener, "listener"))
                .beginControlFlow("synchronized ($1N)", listeners.name)
                .addStatement("$1N()", ENSURE_REGISTERED_NAME)
                .addStatement("$1N.add($2N)", listeners.name, "listener")
                .endControlFlow()
                .build();

        final MethodSpec unregister = MethodSpec.methodBuilder("unregister")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SourceCodeUtils.makeNonNullParameter(typeListener, "listener"))
                .beginControlFlow("synchronized ($1N)", listeners.name)
                .addStatement("$1N.remove($2N)", listeners.name, "listener")
                .addStatement("$1N()", checkIfShouldUnregister.name)
                .endControlFlow()
                .build();

        final MethodSpec unregisterAll = MethodSpec.methodBuilder("unregisterAll")
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("synchronized ($1N)", listeners.name)
                .beginControlFlow("if ($1N)", iterating.name)
                .addStatement("this.$1N = true", removeAll.name)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$1N.clear()", listeners.name)
                .addStatement("$1N()", checkIfShouldUnregister.name)
                .endControlFlow()
                .endControlFlow()
                .build();

        return Arrays.asList(register, unregister, unregisterAll, checkIfShouldUnregister);
    }

    private FieldSpec listenersField() {
        final TypeName typeList = ParameterizedTypeName.get(
                ClassName.get(List.class),
                typeListener
        );

        final TypeName typeArrayList = ParameterizedTypeName.get(
                ClassName.get(ArrayList.class),
                typeListener
        );

        return FieldSpec.builder(typeList, "observeWithListenerListeners")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $1T(2)", typeArrayList)
                .build();
    }
}
