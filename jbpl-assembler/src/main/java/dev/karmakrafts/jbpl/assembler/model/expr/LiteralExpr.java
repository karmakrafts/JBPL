package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class LiteralExpr extends AbstractElement implements Expr {
    public Type type;
    public Object value;

    public LiteralExpr(final @NotNull Type type, final @Nullable Object value) {
        this.type = type;
        this.value = value;
    }

    public static @NotNull LiteralExpr unit() {
        return unit(TokenRange.SYNTHETIC);
    }

    public static @NotNull LiteralExpr unit(final @NotNull TokenRange tokenRange) {
        final var expr = new LiteralExpr(BuiltinType.VOID, null);
        expr.setTokenRange(tokenRange);
        return expr;
    }

    public static @NotNull LiteralExpr of(final @NotNull Object value) {
        return of(value, TokenRange.SYNTHETIC);
    }

    public static @NotNull LiteralExpr of(final @NotNull Object value, final @NotNull TokenRange tokenRange) {
        final var type = TypeMapper.map(value.getClass(), true);
        final var expr = new LiteralExpr(type, value);
        expr.setTokenRange(tokenRange);
        return expr;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return type;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        context.pushValue(this); // Literals push themselves on the stack
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
