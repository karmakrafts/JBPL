package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FunctionSignatureExpr extends AbstractExprContainer implements SignatureExpr {
    public static final int OWNER_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int RETURN_TYPE_INDEX = 2;
    public static final int PARAMETERS_INDEX = 3;

    private int parameterIndex = 0;

    public FunctionSignatureExpr(final @NotNull Expr owner, final @NotNull Expr name, final @NotNull Expr returnType) {
        addExpression(owner);
        addExpression(name);
        addExpression(returnType);
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return this;
    }

    public @NotNull ClassType evaluateFunctionOwner(final @NotNull AssemblerContext context) {
        return getFunctionOwner().evaluateAsLiteral(context, ClassType.class);
    }

    public @NotNull String evaluateFunctionName(final @NotNull AssemblerContext context) {
        return getFunctionName().evaluateAsLiteral(context, String.class);
    }

    public @NotNull org.objectweb.asm.Type evaluateFunctionReturnType(final @NotNull AssemblerContext context) {
        return getFunctionReturnType().evaluateAsLiteral(context, Type.class).materialize(context);
    }

    public @NotNull List<org.objectweb.asm.Type> evaluateFunctionParameters(final @NotNull AssemblerContext context) { // @formatter:off
        return getFunctionParameters().stream()
            .map(type -> type.evaluateAsLiteral(context, Type.class).materialize(context))
            .toList();
    } // @formatter:on

    public @NotNull String evaluateAsDescriptor(final @NotNull AssemblerContext context) {
        final var returnType = evaluateFunctionReturnType(context);
        final var paramTypes = evaluateFunctionParameters(context).toArray(org.objectweb.asm.Type[]::new);
        return org.objectweb.asm.Type.getMethodDescriptor(returnType, paramTypes);
    }

    public @NotNull List<Expr> getFunctionParameters() {
        return getExpressions().subList(PARAMETERS_INDEX, PARAMETERS_INDEX + parameterIndex);
    }

    public void addFunctionParameter(final @NotNull Expr parameterType) {
        getExpressions().add(PARAMETERS_INDEX + parameterIndex++, parameterType);
    }

    public @NotNull Expr getFunctionParameter(final int index) {
        return getExpressions().get(PARAMETERS_INDEX + index);
    }

    public @NotNull Expr getFunctionOwner() {
        return getExpressions().get(OWNER_INDEX);
    }

    public @NotNull Expr getFunctionName() {
        return getExpressions().get(NAME_INDEX);
    }

    public @NotNull Expr getFunctionReturnType() {
        return getExpressions().get(RETURN_TYPE_INDEX);
    }
}
