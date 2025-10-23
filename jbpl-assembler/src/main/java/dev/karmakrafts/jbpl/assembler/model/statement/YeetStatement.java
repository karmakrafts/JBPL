package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

public final class YeetStatement extends AbstractExprContainer implements Statement {
    public static final int TARGET_INDEX = 0;

    public YeetStatement(final @NotNull Expr target) {
        addExpression(target);
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        final var target = getTarget();
        if (target instanceof LiteralExpr literalExpr) {
            var type = (ClassType) literalExpr.value;
            context.removeClass(type.name());
        }
        else if (target instanceof FunctionSignatureExpr functionSignatureExpr) {
            final var result = functionSignatureExpr.evaluateAsConst(context, FunctionSignatureExpr.class);
            final var name = result.getFunctionName().evaluateAsConst(context, String.class);
            final var owner = result.getFunctionOwner().evaluateAsConst(context, ClassType.class);
            final var returnType = result.getFunctionReturnType().evaluateAsConst(context, Type.class);
            // @formatter:off
            final var paramTypes = result.getFunctionParameters().stream()
                .map(ExceptionUtils.propagateUnchecked(type -> type.evaluateAsConst(context, Type.class)))
                .toArray(Type[]::new);
            // @formatter:on
            context.removeFunction(owner.name(), name, returnType, paramTypes);
        }
        else if (target instanceof FieldSignatureExpr fieldSignatureExpr) {
            final var name = fieldSignatureExpr.getFieldName().evaluateAsConst(context, String.class);
            final var owner = fieldSignatureExpr.getFieldOwner().evaluateAsConst(context, ClassType.class);
            context.removeField(owner.name(), name);
        }
        else {
            throw new EvaluationException(String.format("Unsupported target type for yeet: %s", target), this);
        }
    }

    public @NotNull Expr getTarget() {
        return getExpressions().get(TARGET_INDEX);
    }

    public void setTarget(final @NotNull Expr name) {
        getExpressions().set(TARGET_INDEX, name);
    }
}
