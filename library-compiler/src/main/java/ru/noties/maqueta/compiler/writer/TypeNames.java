package ru.noties.maqueta.compiler.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;
import javax.lang.model.type.TypeMirror;

import ru.noties.maqueta.MaquetaSerializer;

public abstract class TypeNames {

    private static final Map<TypeMirror, TypeName> CACHE = new HashMap<>(3);

    public static TypeName get(@NonNull TypeMirror mirror) {
        TypeName typeName = CACHE.get(mirror);
        if (typeName == null) {
            typeName = TypeName.get(mirror);
            CACHE.put(mirror, typeName);
        }
        return typeName;
    }

    public abstract static class Android {

        public static final ClassName CONTEXT = ClassName.get("android.content", "Context");

        @SuppressWarnings("WeakerAccess")
        public static final ClassName NON_NULL = ClassName.get("android.support.annotation", "NonNull");

        public static final ClassName SHARED_PREFERENCES = ClassName.get("android.content", "SharedPreferences");

        public static final ClassName SHARED_PREFERENCES_LISTENER
                = ClassName.get("android.content", "SharedPreferences", "OnSharedPreferenceChangeListener");

        private Android() {
        }
    }

    public static final ClassName MAQUETA_SERIALIZER = ClassName.get(MaquetaSerializer.class);

    public static final ClassName STRING = ClassName.get(String.class);

    public static final TypeName SET_STRING = ParameterizedTypeName.get(ClassName.get("java.util", "Set"), STRING);


    private TypeNames() {
    }

}
