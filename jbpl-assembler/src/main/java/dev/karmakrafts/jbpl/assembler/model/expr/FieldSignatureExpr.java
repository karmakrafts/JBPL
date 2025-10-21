package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
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
    public void evaluate(final @NotNull AssemblerContext context) {
        super.evaluate(context);
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        return LiteralExpr.of(this);
    }

    @Override
    public @NotNull String evaluateAsConstDescriptor(final @NotNull AssemblerContext context) {
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
