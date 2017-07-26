package ru.noties.maqueta.sample;

import java.util.Date;
import java.util.Set;

import ru.noties.maqueta.Maqueta;

abstract class Maquetas {

    @Maqueta(className = ".State", observeWith = Maqueta.ObserveWith.RX)
    private static class State {

        @Maqueta.Key(name = "isFinished", defaultValue = "BuildConfig.DEBUG")
        boolean b;

        int i;
        long l;
        float f;
        String s;
        Set<String> ss;
    }

    @Maqueta(className = ".Whatever", observeWith = Maqueta.ObserveWith.LISTENER)
    private static class Whatever {

        @Maqueta.Field
        long id;

        @Maqueta.Key(defaultValue = "BuildConfig.APPLICATION_ID")
        String key;

        @Maqueta.Key(serializedType = Maqueta.Type.LONG)
        Date date;
    }

    @Maqueta(className = ".YoYo", observeWith = Maqueta.ObserveWith.LIVE_DATA)
    private static class YoYo {

        String key;

        @Maqueta.Key(defaultValue = "System.currentTimeMillis()")
        long firstLaunch;
    }
}
