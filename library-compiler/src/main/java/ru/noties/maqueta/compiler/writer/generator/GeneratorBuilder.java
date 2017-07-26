package ru.noties.maqueta.compiler.writer.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.annotation.NonNull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import ru.noties.maqueta.Maqueta;
import ru.noties.maqueta.compiler.parser.MaquetaFieldDef;
import ru.noties.maqueta.compiler.parser.MaquetaKeyDef;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;
import ru.noties.maqueta.compiler.writer.SourceCodeUtils;
import ru.noties.maqueta.compiler.writer.TypeNames;

class GeneratorBuilder extends AbsGenerator {

    private static final String BUILDER_CLASS_NAME = "Builder";

    private static final String CONTEXT_NAME = "context";
    private static final FieldSpec CONTEXT = FieldSpec.builder(TypeNames.Android.CONTEXT, CONTEXT_NAME)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

    private static final String NAME_NAME = "name";
    private static final FieldSpec NAME = FieldSpec.builder(TypeNames.STRING, NAME_NAME)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

    private final ClassName builderClassName;

    private final Types typeUtils;

    // as we share the same amount of fields in builder & generated class, we will prepare then before-hand
    // NB, builder has 2 additional fields: Context & String
    private Map<MaquetaKeyDef, FieldSpec> serializers;
    private List<FieldSpec> fieldSpecs;

    GeneratorBuilder(
            @NonNull ProcessingEnvironment environment,
            @NonNull MaquetaTypeDef typeDef
    ) {
        super(environment, typeDef);
        this.builderClassName = generatedClassName.nestedClass(BUILDER_CLASS_NAME);
        this.typeUtils = environment.getTypeUtils();

        prepareCommonFields();
    }

    private void prepareCommonFields() {

        final List<FieldSpec> fieldSpecs = new ArrayList<>(2);
        final Map<MaquetaKeyDef, FieldSpec> serializers = new HashMap<>(3);

        FieldSpec spec;
        Element element;

        if (typeDef.fields() != null) {

            //noinspection ConstantConditions
            for (MaquetaFieldDef fieldDef : typeDef.fields()) {
                element = fieldDef.element();
                spec = FieldSpec.builder(TypeNames.get(element.asType()), element.getSimpleName().toString())
                        .addModifiers(Modifier.PRIVATE)
                        .build();
                fieldSpecs.add(spec);
            }
        }

        for (MaquetaKeyDef keyDef : typeDef.keys()) {

            String fieldName;
            TypeName typeName;

            if (keyDef.serialize()) {

                element = keyDef.element();
                fieldName = element.getSimpleName().toString() + "Serializer";
                typeName = createSerializerType(element, keyDef.type());

                spec = FieldSpec.builder(typeName, fieldName)
                        .addModifiers(Modifier.PRIVATE)
                        .build();

                fieldSpecs.add(spec);

                serializers.put(keyDef, spec);
            }
        }

        this.fieldSpecs = fieldSpecs;
        this.serializers = serializers;
    }

    @NonNull
    private TypeSpec createBuilderTypeSpec() {

        final MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.Android.CONTEXT, CONTEXT_NAME))
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.STRING, NAME_NAME))
                .addStatement("this.$1N = $1N", CONTEXT_NAME)
                .addStatement("this.$1N = $1N", NAME_NAME)
                .build();

        final MethodSpec validate = builderValidateMethod();

        final MethodSpec build = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedClassName)
                .addStatement("$1N()", validate.name)
                .addStatement("return new $1T(this)", generatedClassName)
                .build();

        final TypeSpec.Builder builder = TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(constructor)
                .addMethod(validate)
                .addMethod(build)
                .addField(CONTEXT)
                .addField(NAME)
                .addFields(fieldSpecs)
                .addMethods(builderMethods());

        return builder.build();
    }

    private MethodSpec builderValidateMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("validate")
                .addModifiers(Modifier.PRIVATE);
        for (FieldSpec spec : fieldSpecs) {
            if (!spec.type.isPrimitive()) {
                builder.addStatement(
                        "if ($1N == null) throw new $2T($3S)",
                        spec.name,
                        IllegalStateException.class,
                        String.format(
                                "Argument `%s` of type `%s` is required to build `%s`",
                                spec.name, spec.type, generatedClassName.reflectionName()
                        )
                );
            }
        }
        return builder.build();
    }

    private List<MethodSpec> builderMethods() {
        final List<MethodSpec> methodSpecs = new ArrayList<>(fieldSpecs.size());
        MethodSpec methodSpec;
        for (FieldSpec fieldSpec : fieldSpecs) {
            methodSpec = MethodSpec.methodBuilder(fieldSpec.name)
                    .returns(builderClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(SourceCodeUtils.makeNonNullParameter(fieldSpec))
                    .addStatement("this.$1N = $1N", fieldSpec.name)
                    .addStatement("return this")
                    .build();
            methodSpecs.add(methodSpec);
        }
        return methodSpecs;
    }

    @Override
    TypeSpec.Builder generate() {
        return super.generate()
                .addType(createBuilderTypeSpec());
    }

    @NonNull
    @Override
    protected List<FieldSpec> fields() {
        return fieldSpecs;
    }

    @NonNull
    @Override
    protected MethodSpec constructor() {

        final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(SourceCodeUtils.makeNonNullParameter(builderClassName, "builder"))
                .addStatement("this.$1N = builder.$2N.getSharedPreferences(builder.$3N, 0)", SP_NAME, CONTEXT_NAME, NAME_NAME);

        for (FieldSpec fieldSpec : fieldSpecs) {
            builder.addStatement("this.$1N = builder.$1N", fieldSpec.name);
        }

        return builder.build();
    }

    @NonNull
    @Override
    protected List<MethodSpec> methods() {

        final List<MaquetaKeyDef> keyDefs = typeDef.keys();
        final List<MaquetaFieldDef> fieldDefs = typeDef.fields();

        final int size = (keyDefs.size() * 2) + (fieldDefs != null ? fieldDefs.size() : 0) + 1;

        final List<MethodSpec> list = new ArrayList<>(size);

        list.add(staticBuilderMethod());

        if (fieldDefs != null) {

            Element element;
            MethodSpec spec;
            String name;

            for (MaquetaFieldDef fieldDef : fieldDefs) {
                if (fieldDef.getter()) {
                    element = fieldDef.element();
                    name = element.getSimpleName().toString();
                    spec = MethodSpec.methodBuilder(name)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(TypeNames.get(element.asType()))
                            .addStatement("return this.$1N", name)
                            .build();
                    list.add(spec);
                }
            }
        }

        FieldSpec spec;

        for (MaquetaKeyDef keyDef : keyDefs) {
            if (keyDef.serialize()) {
                spec = serializers.get(keyDef);
                list.add(getter(keyDef, spec.name + ".deserialize"));
                list.add(setter(keyDef, spec.name + ".serialize(" + SETTER_VALUE + ")"));
            } else {
                list.add(getter(keyDef, ""));
                list.add(setter(keyDef, SETTER_VALUE));
            }
        }

        return list;
    }

    @NonNull
    private MethodSpec staticBuilderMethod() {
        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(builderClassName)
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.Android.CONTEXT, CONTEXT_NAME))
                .addParameter(SourceCodeUtils.makeNonNullParameter(TypeNames.STRING, NAME_NAME))
                .addStatement("return new $1T($2N, $3N)", builderClassName, CONTEXT_NAME, NAME_NAME)
                .build();
    }

    private TypeName createSerializerType(@NonNull Element element, @NonNull Maqueta.Type type) {

        final TypeName serializedType;
        switch (type) {

            case BOOLEAN:
                serializedType = TypeName.BOOLEAN.box();
                break;

            case INT:
                serializedType = TypeName.INT.box();
                break;

            case LONG:
                serializedType = TypeName.LONG.box();
                break;

            case FLOAT:
                serializedType = TypeName.FLOAT.box();
                break;

            case STRING:
                serializedType = TypeNames.STRING;
                break;

            case SET_STRING:
                serializedType = TypeNames.SET_STRING;
                break;

            default:
                throw new RuntimeException("Unexpected type:" + type);
        }

        final TypeMirror mirror = element.asType();
        final TypeName raw;
        if (mirror.getKind().isPrimitive()) {
            final TypeElement typeElement = typeUtils.boxedClass(typeUtils.getPrimitiveType(mirror.getKind()));
            raw = TypeNames.get(typeElement.asType());
        } else {
            raw = TypeNames.get(mirror);
        }

        return ParameterizedTypeName.get(
                TypeNames.MAQUETA_SERIALIZER,
                raw,
                serializedType
        );
    }
}
