package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public sealed interface Type permits ArrayType, BuiltinType, ClassType, IntersectionType, PreproClassType, PreproType {
    @NotNull TypeCategory getCategory();

    @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException;

    @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException;

    default boolean isAssignableFrom(final @NotNull Type other) {
        return equals(other);
    }

    default @NotNull ArrayType array() {
        return new ArrayType(this);
    }
}
