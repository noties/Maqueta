package ru.noties.maqueta;

// no annotations because we cannot know what defaults are used
// mostly it's NOT_NULL, but in case of a string, it can be... as we allow eval strings there
public interface MaquetaSerializer<T, R> {

    R serialize(T t);

    T deserialize(R r);
}
