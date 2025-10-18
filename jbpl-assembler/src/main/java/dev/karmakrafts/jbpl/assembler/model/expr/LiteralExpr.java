package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// TODO: how do we properly handle token ranges for literals?
public final class LiteralExpr extends AbstractElement implements Expr {
    public Type type;
    public Object value;

    private LiteralExpr(final @NotNull Type type, final @NotNull Object value) {
        this.type = type;
        this.value = value;
    }

    public static LiteralExpr of(final @NotNull Type type) {
        return new LiteralExpr(PreproType.TYPE, type);
    }

    public static LiteralExpr of(final @NotNull Opcode opcode) {
        return new LiteralExpr(PreproType.OPCODE, opcode);
    }

    public static LiteralExpr of(final @NotNull Instruction instruction) {
        return new LiteralExpr(PreproType.INSTRUCTION, instruction);
    }

    public static LiteralExpr of(final @NotNull String value) {
        return new LiteralExpr(BuiltinType.STRING, value);
    }

    public static LiteralExpr of(final byte value) {
        return new LiteralExpr(BuiltinType.I8, value);
    }

    public static LiteralExpr of(final short value) {
        return new LiteralExpr(BuiltinType.I16, value);
    }

    public static LiteralExpr of(final int value) {
        return new LiteralExpr(BuiltinType.I32, value);
    }

    public static LiteralExpr of(final long value) {
        return new LiteralExpr(BuiltinType.I64, value);
    }

    public static LiteralExpr of(final float value) {
        return new LiteralExpr(BuiltinType.F32, value);
    }

    public static LiteralExpr of(final double value) {
        return new LiteralExpr(BuiltinType.F64, value);
    }

    public static LiteralExpr of(final char value) {
        return new LiteralExpr(BuiltinType.CHAR, value);
    }

    public static LiteralExpr of(final boolean value) {
        return new LiteralExpr(BuiltinType.BOOL, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return type;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return this; // Literals are always evaluated to themselves
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (!(obj instanceof LiteralExpr literalExpr)) {
            return false;
        }
        return type.equals(literalExpr.type) && value.equals(literalExpr.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public @NotNull String toString() {
        return String.format("LiteralExpr[type=%s,value=%s]", type, value);
    }
}
