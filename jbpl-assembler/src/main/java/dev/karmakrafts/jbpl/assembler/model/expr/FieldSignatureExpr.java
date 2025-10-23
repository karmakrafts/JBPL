package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class FieldSignatureExpr extends AbstractExprContainer implements SignatureExpr {
    public static final int OWNER_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int TYPE_INDEX = 2;

    public FieldSignatureExpr(final @NotNull Expr owner, final @NotNull Expr name, final @NotNull Expr type) {
        addExpression(owner);
        addExpression(name);
        addExpression(type);
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return PreproType.FIELD_SIGNATURE;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        final var owner = getFieldOwner().evaluateAsConst(context);
        final var name = getFieldName().evaluateAsConst(context);
        final var type = getFieldType().evaluateAsConst(context);
        final var signature = new FieldSignatureExpr(owner, name, type);
        signature.setParent(getParent());
        signature.setTokenRange(getTokenRange());
        context.pushValue(LiteralExpr.of(signature));
    }

    @Override
    public @NotNull String evaluateAsConstDescriptor(final @NotNull AssemblerContext context) throws EvaluationException {
        return getFieldType().evaluateAsConst(context, Type.class).materialize(context).getDescriptor();
    }

    public @NotNull Expr getFieldOwner() {
        return getExpressions().get(OWNER_INDEX);
    }

    public @NotNull Expr getFieldName() {
        return getExpressions().get(NAME_INDEX);
    }

    public @NotNull Expr getFieldType() {
        return getExpressions().get(TYPE_INDEX);
    }
}
