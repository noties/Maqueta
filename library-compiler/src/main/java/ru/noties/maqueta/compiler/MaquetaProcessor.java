package ru.noties.maqueta.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import ru.noties.maqueta.Maqueta;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.MaquetaFileWriter;

public class MaquetaProcessor extends AbstractProcessor {

    private ProcessingEnvironment environment;

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Maqueta.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.environment = processingEnvironment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (!roundEnvironment.processingOver()) {

            final MaquetaLogger logger = new MaquetaLogger("MAQUETA", processingEnv);

            final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Maqueta.class);

            final int size = elements != null
                    ? elements.size()
                    : 0;

            if (size > 0) {

                final long start = System.currentTimeMillis();

                final ru.noties.maqueta.compiler.parser.MaquetaParser parser = new ru.noties.maqueta.compiler.parser.MaquetaParser(environment);
                final List<MaquetaTypeDef> typeDefs = new ArrayList<>(size);

                for (Element element : elements) {
                    try {
                        typeDefs.add(parser.parse(element));
                    } catch (MaquetaException e) {
                        throw new RuntimeException(e);
                    }
                }

                final MaquetaFileWriter fileWriter = new MaquetaFileWriter(environment);
                for (MaquetaTypeDef typeDef : typeDefs) {
                    try {
                        fileWriter.write(typeDef);
                    } catch (MaquetaException e) {
                        throw new RuntimeException(e);
                    }
                }

                final long end = System.currentTimeMillis();
                logger.note("Processing of %d elements took: %d ms", size, (end - start));

            }/* else {
//                logger.note("Processing skipped, no @Maqueta annotated elements");
            }*/
        }

        return false;
    }
}
