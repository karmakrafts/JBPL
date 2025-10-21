package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import org.jetbrains.annotations.NotNull;

public final class YeetStatement extends AbstractExprContainer implements Statement {
    public static final int TARGET_INDEX = 0;

    public YeetStatement(final @NotNull Expr target) {
        addExpression(target);
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        final var target = getTarget();
        if (target instanceof LiteralExpr literalExpr) {
            var type = (ClassType) literalExpr.value;
            context.removeClass(type.name());
        }
        else if (target instanceof FunctionSignatureExpr functionSignatureExpr) {
            final var result = functionSignatureExpr.evaluateAs(context, FunctionSignatureExpr.class);
            final var name = result.evaluateFunctionName(context);
            final var params = result.evaluateFunctionParameters(context).toArray(org.objectweb.asm.Type[]::new);
            final var returnType = result.evaluateFunctionReturnType(context);
            final var owner = result.evaluateFunctionOwner(context);

            context.removeFunction(owner.name(), name, org.objectweb.asm.Type.getMethodType(returnType, params));
        }
        else if (target instanceof FieldSignatureExpr fieldSignatureExpr) {
            final var name = fieldSignatureExpr.evaluateFieldName(context);
            final var owner = fieldSignatureExpr.evaluateFieldOwner(context);

            context.removeField(owner.name(), name);
        }
        else {
            throw new IllegalStateException(String.format("Unsupported target type for yeet: %s", target));
        }
    }

    public @NotNull Expr getTarget() {
        return getExpressions().get(TARGET_INDEX);
    }

    public void setTarget(final @NotNull Expr name) {
        getExpressions().set(TARGET_INDEX, name);
    }
}
