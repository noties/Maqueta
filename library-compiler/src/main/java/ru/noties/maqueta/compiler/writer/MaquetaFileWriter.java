package ru.noties.maqueta.compiler.writer;

import java.io.IOException;
import java.io.Writer;

import android.support.annotation.NonNull;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import ru.noties.maqueta.compiler.MaquetaException;
import ru.noties.maqueta.compiler.MaquetaLogger;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.generator.SourceCodeGenerator;

public class MaquetaFileWriter {

    private final ProcessingEnvironment environment;
    private final MaquetaLogger logger;
    private final Filer filer;

    public MaquetaFileWriter(@NonNull ProcessingEnvironment environment) {
        this.environment = environment;
        this.logger = new MaquetaLogger("MAQUETA-WRITER", environment);
        this.filer = environment.getFiler();
    }

    public void write(@NonNull MaquetaTypeDef typeDef) throws MaquetaException {

        final SourceCodeGenerator sourceCodeGenerator = SourceCodeGenerator.create(environment, typeDef);

        Writer writer = null;
        try {
            final JavaFileObject object = filer.createSourceFile(createSourceFileName(typeDef));
            writer = object.openWriter();
            writer.write(sourceCodeGenerator.generate());
        } catch (IOException e) {
            logger.error("Exception during writing Java source file, package: `%s`, class: `%s`", typeDef.packageName(), typeDef.className());
            throw new MaquetaException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // no op
                }
            }
        }
    }

    @NonNull
    private static String createSourceFileName(@NonNull MaquetaTypeDef typeDef) {
        final String out;
        final String packageName = typeDef.packageName();
        if (packageName.length() == 0) {
            // default package
            out = typeDef.className();
        } else {
            out = packageName + "." + typeDef.className();
        }
        return out;
    }
}
