package ru.noties.maqueta.sample;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import ru.noties.debug.Debug;

public class Live extends LiveData<Live> {

    private final SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public Live(@NonNull Context context, @NonNull String name) {
        this.preferences = context.getSharedPreferences(name, 0);
    }

    public String get() {
        return preferences.getString("key", null);
    }

    public void set(String value) {
        preferences.edit().putString("key", value).apply();
    }

    @Override
    protected void onActive() {
        Debug.i();
        ensureRegistered();
    }

    @Override
    protected void onInactive() {
        Debug.i();
        unregister();
    }

    private void ensureRegistered() {
        if (listener == null) {
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Debug.e("updated");
                    setValue(Live.this);
                }
            };
            preferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private void unregister() {
        if (listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
            listener = null;
        }
    }
}
