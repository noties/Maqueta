package ru.noties.maqueta.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import ru.noties.maqueta.Maqueta;
import ru.noties.maqueta.compiler.MaquetaException;
import ru.noties.maqueta.compiler.MaquetaLogger;

public class MaquetaParser {

    private final MaquetaLogger logger;
    private final Elements elements;

    public MaquetaParser(@NonNull ProcessingEnvironment environment) {
        this.logger = new MaquetaLogger("MAQUETA-PARSER", environment);
        this.elements = environment.getElementUtils();
    }

    @NonNull
    public MaquetaTypeDef parse(@NonNull Element element) throws MaquetaException {

        if (!(element instanceof TypeElement)) {
            logger.error(element, "Unexpected element annotated with @Maqueta annotation.");
            throw new MaquetaException();
        }

        final TypeElement typeElement = (TypeElement) element;
        final Maqueta maqueta = typeElement.getAnnotation(Maqueta.class);

        final MaquetaTypeInfo typeInfo = parseTypeInfo(typeElement, maqueta);
        final Maqueta.ObserveWith observeWith = parseObserveWith(maqueta);
        final List<MaquetaFieldDef> fields = parseFields(typeElement);
        final List<MaquetaKeyDef> keys = parseKeys(typeElement);

        return new MaquetaTypeDef(
                typeElement,
                typeInfo.packageName,
                typeInfo.className,
                observeWith,
                fields,
                keys
        );
    }

    @NonNull
    private MaquetaTypeInfo parseTypeInfo(@NonNull TypeElement element, @NonNull Maqueta maqueta) throws MaquetaException {

        // we do not validate here too much and let java compiler decide if it allows such a class name

        // cannot be null here
        final String value = maqueta.className().trim();

        if (value.length() == 0) {
            logger.error(element, "`className` argument must not be empty. It must be fully qualified " +
                    "class name (`com.example.MyClass`) or start with a dot (`.`) to place in the " +
                    "same package as this element (`.MyClass`)");
            throw new MaquetaException();
        }

        final MaquetaTypeInfo out;

        if ('.' == value.charAt(0)) {
            // same package
            final PackageElement packageElement = elements.getPackageOf(element);
            out = new MaquetaTypeInfo(
                    packageElement.getQualifiedName().toString(),
                    value.substring(1)
            );
        } else {

            final String packageName;
            final String className;
            {
                final int lastDotIndex = value.lastIndexOf('.');
                if (lastDotIndex == -1) {
                    packageName = "";
                    className = value;
                } else {
                    packageName = value.substring(0, lastDotIndex);
                    className = value.substring(lastDotIndex + 1);
                }
            }
            out = new MaquetaTypeInfo(packageName, className);
        }

        return out;
    }

    @NonNull
    private Maqueta.ObserveWith parseObserveWith(@NonNull Maqueta maqueta) throws MaquetaException {
        return maqueta.observeWith();
    }

    @Nullable
    private List<MaquetaFieldDef> parseFields(@NonNull TypeElement typeElement) throws MaquetaException {

        final List<? extends Element> enclosed = typeElement.getEnclosedElements();
        final List<MaquetaFieldDef> out = new ArrayList<>(2);

        Maqueta.Field field;

        for (Element element : enclosed) {

            if (ElementKind.FIELD == element.getKind()) {

                field = element.getAnnotation(Maqueta.Field.class);
                if (field != null) {
                    out.add(parseField(element, field));
                }
            }
        }

        if (out.size() == 0) {
            return null;
        } else {
            return out;
        }
    }

    @NonNull
    private MaquetaFieldDef parseField(
            Element element,
            @NonNull Maqueta.Field field
    ) throws MaquetaException {
        return new MaquetaFieldDef(element, field.getter());
    }

    @NonNull
    private List<MaquetaKeyDef> parseKeys(@NonNull TypeElement typeElement) throws MaquetaException {

        final List<? extends Element> enclosed = typeElement.getEnclosedElements();
        final List<MaquetaKeyDef> out = new ArrayList<>(enclosed.size());

        Maqueta.Key key;
        Maqueta.Field field;

        for (Element element : enclosed) {
            if (ElementKind.FIELD == element.getKind()) {
                key = element.getAnnotation(Maqueta.Key.class);
                field = element.getAnnotation(Maqueta.Field.class);
                if (field != null) {
                    // skip only if there is no @Key annotation also -> then throw
                    if (key != null) {
                        logger.error(element, "Field is annotated with both @Maqueta.Field & @Maqueta.Key");
                        throw new MaquetaException();
                    } else {
                        continue;
                    }
                }
                out.add(parseKey(element, key));
            }
        }

        if (out.size() == 0) {
            logger.error(typeElement, "Class has no keys");
            throw new MaquetaException();
        }

        return out;
    }

    private MaquetaKeyDef parseKey(@NonNull Element element, @Nullable Maqueta.Key key) throws MaquetaException {

        final String name = parseKeyName(element, key);
        final MaquetaKeyTypeInfo typeInfo = parseKeyTypeInfo(element, key);
        final String defaultValue = parseKeyDefaultValue(key, typeInfo.type);

        return new MaquetaKeyDef(element, name, defaultValue, typeInfo.serialize, typeInfo.type);
    }

    @NonNull
    private String parseKeyName(@NonNull Element element, @Nullable Maqueta.Key key) throws MaquetaException {

        final String value = key != null
                ? key.name()
                : null;

        return value != null && value.length() > 0
                ? value
                : element.getSimpleName().toString();
    }

    @NonNull
    private String parseKeyDefaultValue(
            @Nullable Maqueta.Key key,
            @NonNull Maqueta.Type type
    ) throws MaquetaException {

        final String value = key != null
                ? key.defaultValue()
                : null;

        return value != null && value.length() > 0
                ? value
                : defaultValuesForType(type);
    }

    private String defaultValuesForType(@NonNull Maqueta.Type type) {

        final String out;

        switch (type) {

            case BOOLEAN:
                out = "false";
                break;

            case INT:
                out = "0";
                break;

            case LONG:
                out = "0L";
                break;

            case FLOAT:
                out = ".0F";
                break;

            case STRING:
                out = "\"\"";
                break;

            case SET_STRING:
                out = "java.util.Collections.EMPTY_SET";
                break;

            default:
                throw new IllegalStateException("Unexpected type: " + type);
        }

        return out;
    }

    @NonNull
    private MaquetaKeyTypeInfo parseKeyTypeInfo(@NonNull Element element, @Nullable Maqueta.Key key) throws MaquetaException {

        // parse real type of the field

        final TypeMirror typeMirror = element.asType();
        final String typeMirrorRaw = typeMirror.toString();

        final Maqueta.Type type;
        final boolean serialize;

        // if key != null && serializedType != AUTO -> we need a serializer no matter what type this key (even supported)

        final Maqueta.Type serializedType = key != null
                ? key.serializedType()
                : Maqueta.Type.AUTO;

        if (serializedType != Maqueta.Type.AUTO) {

            type = serializedType;
            serialize = true;

        } else {

            switch (typeMirrorRaw) {

                case "boolean":
                case "java.lang.Boolean":
                    type = Maqueta.Type.BOOLEAN;
                    break;

                case "int":
                case "java.lang.Integer":
                    type = Maqueta.Type.INT;
                    break;

                case "long":
                case "java.lang.Long":
                    type = Maqueta.Type.LONG;
                    break;

                case "float":
                case "java.lang.Float":
                    type = Maqueta.Type.FLOAT;
                    break;

                case "java.lang.String":
                    type = Maqueta.Type.STRING;
                    break;

                case "java.util.Set<java.lang.String>":
                    type = Maqueta.Type.SET_STRING;
                    break;

                default:
                    final Maqueta.Type[] types = Maqueta.Type.values();
                    final List<Maqueta.Type> supportedTypes = Arrays.asList(types)
                            .subList(1, types.length);
                    logger.error(element, "Key type `%s` is not natively supported by SharedPreferences, " +
                            "`serializedType` argument must be specified in order to generate " +
                            "serializer information. Supported types are %s", typeMirrorRaw, supportedTypes);
                    throw new MaquetaException();
            }
            serialize = false;
        }

        return new MaquetaKeyTypeInfo(type, serialize);
    }

    private static class MaquetaTypeInfo {

        final String packageName;
        final String className;

        private MaquetaTypeInfo(@Nullable String packageName, @NonNull String className) {
            this.packageName = packageName;
            this.className = className;
        }
    }

    private static class MaquetaKeyTypeInfo {

        final Maqueta.Type type;
        final boolean serialize;

        MaquetaKeyTypeInfo(@NonNull Maqueta.Type type, boolean serialize) {
            this.type = type;
            this.serialize = serialize;
        }
    }
}
