package ru.noties.maqueta.compiler.writer.generator.observewith;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;

import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;

@SuppressWarnings({"NullableProblems", "ConstantConditions"})
class ObservableNoOp extends AbsObservable {

    ObservableNoOp(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {
        super(environment, typeDef);
    }

    @Override
    void process(@NonNull TypeSpec.Builder builder) {
        // no op
    }

    @Override
    @NonNull
    MethodSpec notifyChange() {
        return null;
    }

    @NonNull
    @Override
    List<FieldSpec> fields() {
        return null;
    }

    @NonNull
    @Override
    List<MethodSpec> methods() {
        return null;
    }
}
