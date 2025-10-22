package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return PreproType.FUNCTION_SIGNATURE;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        super.evaluate(context);
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        final var owner = getFunctionOwner().evaluateAsConst(context);
        final var name = getFunctionName().evaluateAsConst(context);
        final var returnType = getFunctionReturnType().evaluateAsConst(context);
        final var paramTypes = getFunctionParameters().stream().map(expr -> expr.evaluateAsConst(context)).toList();
        final var signature = new FunctionSignatureExpr(owner, name, returnType);
        signature.setParent(getParent());
        signature.setTokenRange(getTokenRange());
        signature.addFunctionParameters(paramTypes);
        return LiteralExpr.of(signature);
    }

    @Override
    public @NotNull String evaluateAsConstDescriptor(final @NotNull AssemblerContext context) {
        final var returnType = getFunctionReturnType().evaluateAsConst(context, Type.class).materialize(context);
        // @formatter:off
        final var paramTypes = getFunctionParameters().stream()
            .map(type -> type.evaluateAsConst(context, Type.class).materialize(context))
            .toArray(org.objectweb.asm.Type[]::new);
        // @formatter:on
        return org.objectweb.asm.Type.getMethodDescriptor(returnType, paramTypes);
    }

    public @NotNull List<Expr> getFunctionParameters() {
        return getExpressions().subList(PARAMETERS_INDEX, PARAMETERS_INDEX + parameterIndex);
    }

    public void addFunctionParameter(final @NotNull Expr parameterType) {
        getExpressions().add(PARAMETERS_INDEX + parameterIndex++, parameterType);
    }

    public void addFunctionParameters(final @NotNull Collection<? extends Expr> parameterTypes) {
        getExpressions().addAll(PARAMETERS_INDEX + parameterIndex, parameterTypes);
        parameterIndex += parameterTypes.size();
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
