package ru.noties.maqueta.sample;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.noties.debug.AndroidLogDebugOutput;
import ru.noties.debug.Debug;

public class MainActivity extends LifecycleActivity {

    static {
        Debug.init(new AndroidLogDebugOutput(true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        final Whatever whatever = Whatever.builder(this, "whatever_23")
//                .id(23L)
//                .dateSerializer(new MaquetaSerializer<Date, Long>() {
//                    @Override
//                    public Long serialize(Date date) {
//                        return date.getTime();
//                    }
//
//                    @Override
//                    public Date deserialize(Long aLong) {
//                        return new Date(aLong);
//                    }
//                })
//                .build();
//
//        whatever.register(new MaquetaListener<Whatever>() {
//            @Override
//            public void apply(Whatever whatever) {
//                Debug.i("date: %s, whatever: %s", whatever.date(), whatever);
//            }
//        });
//
//        Debug.i("id: %d, date: %s, key: %s", whatever.id(), whatever.date(), whatever.key());
//        Debug.e("updating whatever");
//        whatever.key(new Date().toString());

//        final Rx rx = new Rx(this, "rx");
//        Debug.i("rx: %s", rx.get());
//
//        final Disposable disposable = rx.observe()
//                .subscribe(new Consumer<Rx>() {
//                    @Override
//                    public void accept(@NonNull Rx rx) throws Exception {
//                        Debug.i("first: %s", rx.get());
//                    }
//                });
//
//        final Disposable second = rx.observe()
//                .subscribe(new Consumer<Rx>() {
//                    @Override
//                    public void accept(@NonNull Rx rx) throws Exception {
//                        Debug.i("second: %s", rx.get());
//                    }
//                });
//
//        rx.set(new Date().toString());
//
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//
//            int count = 5;
//
//            @Override
//            public void run() {
//                if (--count > 0) {
//                    rx.set(new Date().toString());
//                    handler.postDelayed(this, 1000L);
//                } else {
//                    disposable.dispose();
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            second.dispose();
//                        }
//                    }, 3000L);
//                }
//            }
//        }, 1000L);

        final Live live = new Live(this, "live");
        live.observe(this, new Observer<Live>() {
            @Override
            // wtf?! NON_NULL
            public void onChanged(@Nullable Live live) {
                Debug.e("live: %s", live.get());
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            int count = 5;

            @Override
            public void run() {
                if (--count > 0) {
                    live.set(new Date().toString());
                    handler.postDelayed(this, 1000L);
                }
            }
        }, 1000L);
    }
}
