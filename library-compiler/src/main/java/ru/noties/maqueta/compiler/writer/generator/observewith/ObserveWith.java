package ru.noties.maqueta.compiler.writer.generator.observewith;

import com.squareup.javapoet.TypeSpec;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;

import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;

public final class ObserveWith {

    @NonNull
    public static ObserveWith create(@NonNull ProcessingEnvironment environment, @NonNull MaquetaTypeDef typeDef) {

        final AbsObservable observable;

        switch (typeDef.observeWith()) {

            case RX:
                observable = new ObservableRx(environment, typeDef);
                break;

            case LIVE_DATA:
                observable = new ObservableLiveData(environment, typeDef);
                break;

            case LISTENER:
                observable = new ObservableListener(environment, typeDef);
                break;

            default:
                observable = new ObservableNoOp(environment, typeDef);
                break;
        }

        return new ObserveWith(observable);
    }

    private final AbsObservable observable;

    private ObserveWith(@NonNull AbsObservable observable) {
        this.observable = observable;
    }

    public void process(@NonNull TypeSpec.Builder builder) {
        observable.process(builder);
    }
}
