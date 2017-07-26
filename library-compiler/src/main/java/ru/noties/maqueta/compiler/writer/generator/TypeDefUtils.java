package ru.noties.maqueta.compiler.writer.generator;

import java.util.List;

import android.support.annotation.NonNull;

import ru.noties.maqueta.compiler.parser.MaquetaFieldDef;
import ru.noties.maqueta.compiler.parser.MaquetaKeyDef;
import ru.noties.maqueta.compiler.parser.MaquetaTypeDef;

abstract class TypeDefUtils {

    static boolean hasSerializers(@NonNull MaquetaTypeDef typeDef) {

        boolean hasSerializers = false;

        for (MaquetaKeyDef keyDef : typeDef.keys()) {
            hasSerializers |= keyDef.serialize();
        }

        return hasSerializers;
    }

    static boolean hasFields(@NonNull MaquetaTypeDef typeDef) {
        final List<MaquetaFieldDef> fieldDefs = typeDef.fields();
        return fieldDefs != null
                && fieldDefs.size() > 0;
    }

    private TypeDefUtils() {
    }
}
