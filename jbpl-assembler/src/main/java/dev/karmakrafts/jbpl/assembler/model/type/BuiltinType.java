package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

public enum BuiltinType implements Type {
    // @formatter:off
    VOID    (org.objectweb.asm.Type.VOID_TYPE),
    I8      (org.objectweb.asm.Type.BYTE_TYPE),
    I16     (org.objectweb.asm.Type.SHORT_TYPE),
    I32     (org.objectweb.asm.Type.INT_TYPE),
    I64     (org.objectweb.asm.Type.LONG_TYPE),
    F32     (org.objectweb.asm.Type.FLOAT_TYPE),
    F64     (org.objectweb.asm.Type.DOUBLE_TYPE),
    CHAR    (org.objectweb.asm.Type.CHAR_TYPE),
    BOOL    (org.objectweb.asm.Type.BOOLEAN_TYPE),
    OBJECT  (org.objectweb.asm.Type.getObjectType("java/lang/Object")),
    STRING  (org.objectweb.asm.Type.getObjectType("java/lang/String"));
    // @formatter:on

    private final org.objectweb.asm.Type materialType;

    BuiltinType(final @NotNull org.objectweb.asm.Type materialType) {
        this.materialType = materialType;
    }

    @Override
    public boolean isMaterializable() {
        return true;
    }

    @Override
    public boolean isObject() {
        return this == OBJECT || this == STRING;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        return materialType;
    }
}
