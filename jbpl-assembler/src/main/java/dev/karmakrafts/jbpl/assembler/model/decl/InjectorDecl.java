package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.ReturnTarget;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import org.jetbrains.annotations.NotNull;

public final class InjectorDecl extends AbstractStatementContainer implements Declaration, ScopeOwner, ReturnTarget {
    private FunctionSignatureExpr target;
    private Expr selector;

    public InjectorDecl(final @NotNull FunctionSignatureExpr target, final @NotNull Expr selector) {
        target.setParent(this);
        this.target = target;
        selector.setParent(this);
        this.selector = selector;
    }

    public @NotNull Expr getSelector() {
        return selector;
    }

    public void setSelector(final @NotNull Expr selector) {
        selector.setParent(this);
        this.selector = selector;
    }

    public @NotNull FunctionSignatureExpr getTarget() {
        return target;
    }

    public void setTarget(final @NotNull FunctionSignatureExpr target) {
        target.setParent(this);
        this.target = target;
    }

    @Override
    public void evaluate(@NotNull AssemblerContext context) {

    }
}
