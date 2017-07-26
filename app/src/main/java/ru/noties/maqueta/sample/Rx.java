package ru.noties.maqueta.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;
import ru.noties.debug.Debug;

public class Rx {

    private final SharedPreferences preferences;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private PublishSubject<Rx> subject;

    public Rx(@NonNull Context context, @NonNull String name) {
        this.preferences = context.getSharedPreferences(name, 0);
    }

    public String get() {
        return preferences.getString("key", null);
    }

    public void set(String value) {
        preferences.edit().putString("key", value).apply();
    }

    public Observable<Rx> observe() {
        ensureSubject();
        ensureListener();
        return subject.hide()
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        checkIfShouldUnregister();
                    }
                });
    }

    private void ensureSubject() {
        Debug.i("subject == null: %s", subject == null);
        if (subject == null) {
            subject = PublishSubject.create();
        }
    }

    private void ensureListener() {
        Debug.i("listener == null: %s", listener == null);
        if (listener == null) {
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (subject != null) {
                        subject.onNext(Rx.this);
                    }
                }
            };
            preferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private void checkIfShouldUnregister() {
        Debug.i("subject != null: %s, hasObservers: %s", (subject != null), (subject != null ? subject.hasObservers() : null));
        if (subject != null) {
            if (!subject.hasObservers()) {
                subject.onComplete();
                unregisterListener();
                subject = null;
            }
        }
    }

    private void unregisterListener() {
        Debug.i("listener != null: %s", listener != null);
        if (listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
            listener = null;
        }
    }
}
