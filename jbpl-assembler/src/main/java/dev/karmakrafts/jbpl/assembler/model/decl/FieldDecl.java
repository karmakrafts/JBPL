package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.EmptyExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public final class FieldDecl extends AbstractExprContainer implements Declaration {
    public static final int INITIALIZER_INDEX = 0;

    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);
    private FieldSignatureExpr signature;

    public FieldDecl(final @NotNull FieldSignatureExpr signature) {
        signature.setParent(this);
        this.signature = signature;
        addExpression(new EmptyExpr()); // Initializer
    }

    public @NotNull Expr getInitializer() {
        return (Expr) elements.get(INITIALIZER_INDEX);
    }

    public void setInitializer(final @NotNull Expr initializer) {
        elements.set(INITIALIZER_INDEX, initializer);
    }

    public @NotNull FieldSignatureExpr getSignature() {
        return signature;
    }

    public void setSignature(final @NotNull FieldSignatureExpr signature) {
        signature.setParent(this);
        this.signature = signature;
    }
}
