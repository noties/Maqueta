package ru.noties.maqueta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Maqueta {

    /**
     * Fully qualified class name (`com.example.MyClass`) or starting with the dot to indicate
     * that generated class must be in the same package as descriptor
     */
    String className();

    ObserveWith observeWith() default ObserveWith.NONE;


    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Key {

        String name() default "";

        String defaultValue() default "";

        Type serializedType() default Type.AUTO;
    }

    /**
     * Annotation to indicate that a {@link java.lang.reflect.Field} in a descriptor class
     * should be present in the final Maqueta generated class.
     *
     * Please note, that this field\'s value won\'t be shared between multiple instances
     * and it must be provided via builder factory method that will be generated.
     * But if field\'s type is a primitive one, no validation will occur.
     *
     * This might be helpful if, for example, a default value must be dynamic, so
     * it can be provided when constructing a class and be used as a default value
     * for a key or to be an argument in some evaluation
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Field {

        boolean getter() default true;
    }

    enum Type {
        AUTO, BOOLEAN, INT, LONG, FLOAT, STRING, SET_STRING
    }

    enum ObserveWith {
        NONE, RX, LIVE_DATA, LISTENER
    }
}
