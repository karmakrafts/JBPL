package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
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

    public @NotNull String evaluateFieldName(final @NotNull AssemblerContext context) {
        return getFieldName().evaluateAsLiteral(context, String.class);
    }

    public @NotNull ClassType evaluateFieldOwner(final @NotNull AssemblerContext context) {
        return getFieldOwner().evaluateAsLiteral(context, ClassType.class);
    }

    public @NotNull Type evaluateFieldType(final @NotNull AssemblerContext context) {
        return getFieldOwner().evaluateAsLiteral(context, Type.class);
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
