package ru.noties.maqueta.compiler.writer.generator.observewith;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import ru.noties.maqueta.compiler.parser.MaquetaKeyDef;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.TypeNames;
import ru.noties.maqueta.compiler.writer.generator.SourceCodeGenerator;

abstract class AbsObservable {

    private static final FieldSpec LISTENER = FieldSpec.builder(TypeNames.Android.SHARED_PREFERENCES_LISTENER, "observeWithSharedPreferencesListener")
            .addModifiers(Modifier.PRIVATE)
            .build();

    static final String ENSURE_REGISTERED_NAME = "observeWithEnsureRegistered";
    static final String UNREGISTER_NAME = "observeWithUnregister";

    private final MaquetaTypeDef typeDef;

    final ClassName generatedClassName;

    private MethodSpec notifyChange;


    @SuppressWarnings("UnusedParameters")
    AbsObservable(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        this.typeDef = typeDef;

        this.generatedClassName = ClassName.get(typeDef.packageName(), typeDef.className());
    }

    void process(@NonNull TypeSpec.Builder builder) {

        this.notifyChange = notifyChange();

        final MethodSpec ensureListenerRegistered = ensureListenerRegisteredMethod();

        final MethodSpec unregister = MethodSpec.methodBuilder(UNREGISTER_NAME)
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if ($1N != null)", LISTENER.name)
                .addStatement("$1N.unregisterOnSharedPreferenceChangeListener($2N)", SourceCodeGenerator.SP_NAME, LISTENER.name)
                .addStatement("this.$1N = null", LISTENER.name)
                .endControlFlow()
                .build();

        builder
                .addField(LISTENER)
                .addFields(fields())
                .addMethod(notifyChange)
                .addMethod(ensureListenerRegistered)
                .addMethod(unregister)
                .addMethods(methods());
    }

    @NonNull
    abstract MethodSpec notifyChange();

    @NonNull
    abstract List<FieldSpec> fields();

    @NonNull
    abstract List<MethodSpec> methods();

    private MethodSpec ensureListenerRegisteredMethod() {

        final TypeName hashSetString = ParameterizedTypeName.get(
                ClassName.get(HashSet.class),
                TypeNames.STRING
        );

        final List<MaquetaKeyDef> keyDefs = typeDef.keys();

        final CodeBlock.Builder keys = CodeBlock.builder()
                .beginControlFlow("final $1T keys = new $2T($3L)", TypeNames.SET_STRING, hashSetString, keyDefs.size())
                .beginControlFlow("");
        for (MaquetaKeyDef keyDef : keyDefs) {
            keys.addStatement("add($S)", keyDef.name());
        }
        keys
                .endControlFlow()
                .endControlFlow()
                .addStatement("");

        return MethodSpec.methodBuilder(ENSURE_REGISTERED_NAME)
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if ($1N == null)", LISTENER.name)
                .beginControlFlow("this.$1N = new $2T()", LISTENER.name, TypeNames.Android.SHARED_PREFERENCES_LISTENER)
                .addCode(keys.build())
                .beginControlFlow("@$1T public void onSharedPreferenceChanged($2T sp, $3T key)", Override.class, TypeNames.Android.SHARED_PREFERENCES, TypeNames.STRING)
                .beginControlFlow("if ($1N.contains($2N))", "keys", "key")
                .addStatement("$1N()", notifyChange.name)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("")
                .addStatement("$1N.registerOnSharedPreferenceChangeListener($2N)", SourceCodeGenerator.SP_NAME, LISTENER.name)
                .endControlFlow()
                .build();
    }
}