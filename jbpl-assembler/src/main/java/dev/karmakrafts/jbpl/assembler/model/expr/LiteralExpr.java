package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// TODO: how do we properly handle token ranges for literals?
public final class LiteralExpr extends AbstractElement implements Expr {
    public Type type;
    public Object value;

    public LiteralExpr(final @NotNull Type type, final @Nullable Object value) {
        this.type = type;
        this.value = value;
    }

    public static LiteralExpr unit() {
        return new LiteralExpr(BuiltinType.VOID, null);
    }

    public static @NotNull LiteralExpr of(final @NotNull Object value) {
        final var type = TypeMapper.map(value.getClass(), true); // We auto-unbox constant values
        return new LiteralExpr(type, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return type;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        if (type == BuiltinType.VOID) {
            throw new IllegalStateException(
                "Attempted to const evaluate unit expression, this is an implementation fault");
        }
        return this;
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
