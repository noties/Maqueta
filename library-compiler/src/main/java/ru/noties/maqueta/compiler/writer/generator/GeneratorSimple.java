package ru.noties.maqueta.compiler.writer.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import ru.noties.maqueta.compiler.parser.MaquetaKeyDef;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.SourceCodeUtils;
import ru.noties.maqueta.compiler.writer.TypeNames;

class GeneratorSimple extends AbsGenerator {

    private static final String CONTEXT = "context";
    private static final String NAME = "name";

    GeneratorSimple(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        super(environment, typeDef);
    }

    @NonNull
    @Override
    protected List<FieldSpec> fields() {

        // we do not have here specific fields for this implementation

        //noinspection unchecked
        return Collections.EMPTY_LIST;
    }

    @NonNull
    @Override
    protected MethodSpec constructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.Android.CONTEXT, CONTEXT))
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.STRING, NAME))
                .addStatement("this.$1N = $2N.getSharedPreferences($3N, 0)", SP_NAME, CONTEXT, NAME)
                .build();
    }

    @NonNull
    @Override
    protected List<MethodSpec> methods() {

        final List<MaquetaKeyDef> keyDefs = typeDef.keys();
        final List<MethodSpec> methodSpecs = new ArrayList<>(1 + (keyDefs.size() * 2));
        methodSpecs.add(staticCreate());

        for (MaquetaKeyDef keyDef : keyDefs) {
            methodSpecs.add(getter(keyDef, ""));
            methodSpecs.add(setter(keyDef, SETTER_VALUE));
        }

        return methodSpecs;
    }

    private MethodSpec staticCreate() {
        return MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(generatedClassName)
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.Android.CONTEXT, CONTEXT))
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.STRING, NAME))
                .addStatement("return new $1T($2N, $3N)", generatedClassName, CONTEXT, NAME)
                .build();
    }
}
