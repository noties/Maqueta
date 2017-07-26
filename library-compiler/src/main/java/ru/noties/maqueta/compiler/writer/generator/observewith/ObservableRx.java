package ru.noties.maqueta.compiler.writer.generator.observewith;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;

class ObservableRx extends AbsObservable {

    private static final String ENSURE_SUBJECT_NAME = "observeWithRxEnsureSubject";
    private static final String CHECK_IF_HAS_OBSERVERS_NAME = "observeWithRxCheckIfHasObservers";

    private final FieldSpec subject;

    ObservableRx(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        super(environment, typeDef);

        final TypeName subjectType = ParameterizedTypeName.get(
                ClassName.get("io.reactivex.subjects", "PublishSubject"),
                generatedClassName
        );

        subject = FieldSpec.builder(subjectType, "observeWithRxSubject")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    @Override
    @NonNull
    MethodSpec notifyChange() {
        return MethodSpec.methodBuilder("observeWithRxNotifyChange")
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if ($1N != null)", subject.name)
                .addStatement("$1N.onNext(this)", subject.name)
                .endControlFlow()
                .build();
    }

    @NonNull
    @Override
    List<FieldSpec> fields() {
        return Collections.singletonList(subject);
    }

    @NonNull
    @Override
    List<MethodSpec> methods() {

        // observe()
        // ensureSubject()
        // checkIfHasNoObservers() -> unregister()

        final TypeName observableType = ParameterizedTypeName.get(
                ClassName.get("io.reactivex", "Observable"),
                generatedClassName
        );

        final MethodSpec observe = MethodSpec.methodBuilder("observe")
                .addModifiers(Modifier.PUBLIC)
                .returns(observableType)
                .addStatement("$1N()", ENSURE_SUBJECT_NAME)
                .addStatement("$1N()", ENSURE_REGISTERED_NAME)
                .addStatement(
                        "return $1N.hide()\n" +
                                "    .doFinally(new $2T() {\n" +
                                "        @$3T public void run() throws $4T {\n" +
                                "            $5N();\n" +
                                "        }\n" +
                                "    })",
                        subject.name,
                        ClassName.get("io.reactivex.functions", "Action"),
                        Override.class,
                        Exception.class,
                        CHECK_IF_HAS_OBSERVERS_NAME
                )
                .build();

        final MethodSpec ensureSubject = MethodSpec.methodBuilder(ENSURE_SUBJECT_NAME)
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if ($1N == null)", subject.name)
                .addStatement("this.$1N = $2T.create()", subject.name, ClassName.get("io.reactivex.subjects", "PublishSubject"))
                .endControlFlow()
                .build();

        final MethodSpec checkIfHasObservers = MethodSpec.methodBuilder(CHECK_IF_HAS_OBSERVERS_NAME)
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if ($1N != null && !$1N.hasObservers())", subject.name)
                .addStatement("$1N.onComplete()", subject.name)
                .addStatement("$1N()", UNREGISTER_NAME)
                .addStatement("this.$1N = null", subject.name)
                .endControlFlow()
                .build();

        return Arrays.asList(observe, ensureSubject, checkIfHasObservers);
    }
}
