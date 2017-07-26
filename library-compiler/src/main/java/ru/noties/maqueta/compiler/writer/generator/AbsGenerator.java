package ru.noties.maqueta.compiler.writer.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import ru.noties.maqueta.Maqueta;
import ru.noties.maqueta.compiler.parser.MaquetaKeyDef;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.TypeNames;

abstract class AbsGenerator {

    final MaquetaTypeDef typeDef;
    final ClassName generatedClassName;

    @SuppressWarnings("UnusedParameters")
    AbsGenerator(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        this.typeDef = typeDef;
        this.generatedClassName = ClassName.get(typeDef.packageName(), typeDef.className());
    }

    TypeSpec.Builder generate() {
        return TypeSpec.classBuilder(typeDef.className())
                .addModifiers(Modifier.PUBLIC)
                .addField(SP)
                .addFields(fields())
                .addMethod(constructor())
                .addMethods(methods());
    }

    protected abstract List<FieldSpec> fields();

    protected abstract MethodSpec constructor();

    protected abstract List<MethodSpec> methods();


    static final String SP_NAME = SourceCodeGenerator.SP_NAME;
    static final String SETTER_VALUE = "value";

    private static final FieldSpec SP = FieldSpec.builder(TypeNames.Android.SHARED_PREFERENCES, SP_NAME)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

    static MethodSpec getter(@NonNull MaquetaKeyDef keyDef, @NonNull String preProcess) {
        return MethodSpec.methodBuilder(keyDef.element().getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeNames.get(keyDef.element().asType()))
                .addStatement(
                        "return $1N($2N.get$3N($4S, $5N))",
                        preProcess, SP_NAME, preferencesTypeMethodEndName(keyDef.type()), keyDef.name(), keyDef.defaultValue()
                )
                .build();
    }

    MethodSpec setter(@NonNull MaquetaKeyDef keyDef, @NonNull String value) {
        return MethodSpec.methodBuilder(keyDef.element().getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedClassName)
                .addParameter(TypeNames.get(keyDef.element().asType()), SETTER_VALUE)
                .addStatement(
                        "$1N.edit().put$2N($3S, $4N).apply()",
                        SP_NAME, preferencesTypeMethodEndName(keyDef.type()), keyDef.name(), value
                )
                .addStatement("return this")
                .build();
    }

    private static String preferencesTypeMethodEndName(@NonNull Maqueta.Type type) {
        final String out;
        switch (type) {

            case BOOLEAN:
                out = "Boolean";
                break;

            case INT:
                out = "Int";
                break;

            case LONG:
                out = "Long";
                break;

            case FLOAT:
                out = "Float";
                break;

            case STRING:
                out = "String";
                break;

            case SET_STRING:
                out = "StringSet";
                break;

            default:
                throw new IllegalStateException("Unexpected type:" + type);
        }

        return out;
    }
}
