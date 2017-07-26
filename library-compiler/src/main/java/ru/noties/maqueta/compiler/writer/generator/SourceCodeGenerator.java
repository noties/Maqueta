package ru.noties.maqueta.compiler.writer.generator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;

import ru.noties.maqueta.compiler.MaquetaException;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.generator.observewith.ObserveWith;

public final class SourceCodeGenerator {

    public static SourceCodeGenerator create(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {

        final AbsGenerator generator;

        final boolean builder = TypeDefUtils.hasSerializers(typeDef)
                || TypeDefUtils.hasFields(typeDef);

        if (builder) {
            generator = new GeneratorBuilder(environment, typeDef);
        } else {
            generator = new GeneratorSimple(environment, typeDef);
        }

        final ObserveWith observeWith = ObserveWith.create(environment, typeDef);

        return new SourceCodeGenerator(typeDef, generator, observeWith);
    }

    public static final String SP_NAME = "preferences";

    private final MaquetaTypeDef typeDef;
    private final AbsGenerator generator;
    private final ObserveWith observeWith;

    private SourceCodeGenerator(
            @NonNull MaquetaTypeDef typeDef,
            @NonNull AbsGenerator generator,
            @NonNull ObserveWith observeWith
    ) {
        this.typeDef = typeDef;
        this.generator = generator;
        this.observeWith = observeWith;
    }

    public String generate() throws MaquetaException {

        final TypeSpec.Builder builder = generator.generate();

        observeWith.process(builder);

        //noinspection ConstantConditions
        final JavaFile javaFile = JavaFile.builder(typeDef.packageName(), builder.build())
                .indent("    ")
                .build();

        return javaFile.toString();
    }
}
