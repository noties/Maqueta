package ru.noties.maqueta.compiler.writer;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import android.support.annotation.NonNull;

public abstract class SourceCodeUtils {

    public static ParameterSpec makeNonNullParameter(@NonNull FieldSpec spec) {
        return makeNonNullParameter(spec.type, spec.name);
    }

    public static ParameterSpec makeNonNullParameter(@NonNull TypeName typeName, @NonNull String name) {
        final ParameterSpec.Builder builder = ParameterSpec.builder(typeName, name);
        if (!typeName.isPrimitive()) {
            builder.addAnnotation(TypeNames.Android.NON_NULL);
        }
        return builder.build();
    }

    private SourceCodeUtils() {
    }
}
