package ru.noties.maqueta.compiler.writer.generator.observewith;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;

class ObservableLiveData extends AbsObservable {


    ObservableLiveData(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        super(environment, typeDef);
    }

    @Override
    void process(@NonNull TypeSpec.Builder builder) {
        super.process(builder);

        final TypeName typeName = ParameterizedTypeName.get(
                ClassName.get("android.arch.lifecycle", "LiveData"),
                generatedClassName
        );

        builder.superclass(typeName);
    }

    @Override
    @NonNull
    MethodSpec notifyChange() {
        return MethodSpec.methodBuilder("observeWithLiveDataNotifyChange")
                .addModifiers(Modifier.PRIVATE)
                .addStatement("setValue(this)")
                .build();
    }

    @NonNull
    @Override
    List<FieldSpec> fields() {
        //noinspection unchecked
        return Collections.EMPTY_LIST;
    }

    @NonNull
    @Override
    List<MethodSpec> methods() {

        // onActive
        // onInactive

        final MethodSpec onActive = MethodSpec.methodBuilder("onActive")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$1N()", ENSURE_REGISTERED_NAME)
                .build();

        final MethodSpec onInactive = MethodSpec.methodBuilder("onInactive")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$1N()", UNREGISTER_NAME)
                .build();

        return Arrays.asList(onActive, onInactive);
    }
}
