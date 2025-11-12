package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class OpcodeOfExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;

    public OpcodeOfExpr(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return PreproType.OPCODE;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.pushValue(LiteralExpr.of(getValue().evaluateAs(context, Instruction.class).getOpcode(context),
            getTokenRange()));
    }

    @Override
    public @NotNull OpcodeOfExpr copy() {
        return copyParentAndSourceTo(new OpcodeOfExpr(getValue().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("opcodeof(%s)", getValue());
    }
}
