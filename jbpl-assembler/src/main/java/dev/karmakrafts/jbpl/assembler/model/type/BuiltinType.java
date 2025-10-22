package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import org.jetbrains.annotations.NotNull;

public enum BuiltinType implements Type {
    // @formatter:off
    VOID    (TypeCategory.VOID,      org.objectweb.asm.Type.VOID_TYPE),
    I8      (TypeCategory.INTEGER,  org.objectweb.asm.Type.BYTE_TYPE),
    I16     (TypeCategory.INTEGER,  org.objectweb.asm.Type.SHORT_TYPE),
    I32     (TypeCategory.INTEGER,  org.objectweb.asm.Type.INT_TYPE),
    I64     (TypeCategory.INTEGER,  org.objectweb.asm.Type.LONG_TYPE),
    F32     (TypeCategory.FLOAT,    org.objectweb.asm.Type.FLOAT_TYPE),
    F64     (TypeCategory.FLOAT,    org.objectweb.asm.Type.DOUBLE_TYPE),
    CHAR    (TypeCategory.CHAR,     org.objectweb.asm.Type.CHAR_TYPE),
    BOOL    (TypeCategory.BOOL,     org.objectweb.asm.Type.BOOLEAN_TYPE),
    OBJECT  (TypeCategory.OBJECT,   org.objectweb.asm.Type.getObjectType("java/lang/Object")),
    STRING  (TypeCategory.STRING,   org.objectweb.asm.Type.getObjectType("java/lang/String"));
    // @formatter:on

    private final TypeCategory category;
    private final org.objectweb.asm.Type materialType;

    BuiltinType(final @NotNull TypeCategory category, final @NotNull org.objectweb.asm.Type materialType) {
        this.category = category;
        this.materialType = materialType;
    }

    @Override
    public @NotNull TypeCategory getCategory() {
        return category;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull AssemblerContext context) {
        return switch (this) {
            case VOID -> LiteralExpr.unit();
            case I8 -> LiteralExpr.of((byte) 0);
            case I16 -> LiteralExpr.of((short) 0);
            case I32 -> LiteralExpr.of(0);
            case I64 -> LiteralExpr.of(0L);
            case F32 -> LiteralExpr.of(0F);
            case F64 -> LiteralExpr.of(0.0);
            case CHAR -> LiteralExpr.of(' ');
            case BOOL -> LiteralExpr.of(false);
            case STRING -> LiteralExpr.of("");
            default -> throw new IllegalStateException(String.format("%s has no default value!", this));
        };
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context) {
        return materialType;
    }
}
