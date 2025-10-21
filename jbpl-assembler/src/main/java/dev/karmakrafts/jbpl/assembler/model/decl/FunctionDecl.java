package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public final class FunctionDecl extends AbstractStatementContainer implements Declaration, ScopeOwner {
    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);
    private FunctionSignatureExpr signature;

    public FunctionDecl(final @NotNull FunctionSignatureExpr signature) {
        this.signature = signature;
        signature.setParent(this);
    }

    public @NotNull FunctionSignatureExpr getSignature() {
        return signature;
    }

    public void setSignature(final @NotNull FunctionSignatureExpr signature) {
        signature.setParent(this);
        this.signature = signature;
    }

    @Override
    public void evaluate(@NotNull AssemblerContext context) {

    }
}
