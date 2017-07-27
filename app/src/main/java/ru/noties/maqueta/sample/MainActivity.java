package ru.noties.maqueta.sample;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.noties.debug.AndroidLogDebugOutput;
import ru.noties.debug.Debug;
import ru.noties.maqueta.MaquetaListener;
import ru.noties.maqueta.MaquetaSerializer;

public class MainActivity extends LifecycleActivity {

    static {
        Debug.init(new AndroidLogDebugOutput(true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Whatever whatever = Whatever.builder(this, "whatever")
                .id(23)
                .dateSerializer(new MaquetaSerializer<Date, Long>() {
                    @Override
                    public Long serialize(Date date) {
                        return date.getTime();
                    }

                    @Override
                    public Date deserialize(Long aLong) {
                        return new Date(aLong);
                    }
                })
                .build();

        Debug.i("key: %s, date: %s", whatever.key(), whatever.date());

        whatever.unregister(new MaquetaListener<Whatever>() {
            @Override
            public void apply(@NonNull Whatever whatever) {
                Debug.e("updated, key: %s, date: %s", whatever.key(), whatever.date());
            }
        });

        whatever.clear();
    }
}
