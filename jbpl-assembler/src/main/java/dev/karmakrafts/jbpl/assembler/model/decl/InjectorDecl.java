package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;

public final class InjectorDecl extends AbstractStatementContainer implements Declaration, ScopeOwner {
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
        if (this.selector != null) {
            this.selector.setParent(null);
        }
        selector.setParent(this);
        this.selector = selector;
    }

    public @NotNull FunctionSignatureExpr getTarget() {
        return target;
    }

    public void setTarget(final @NotNull FunctionSignatureExpr target) {
        if (this.target != null) {
            this.target.setParent(null);
        }
        target.setParent(this);
        this.target = target;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
        // TODO: implement this
    }

    @Override
    public @NotNull InjectorDecl copy() {
        final var injector = copyParentAndSourceTo(new InjectorDecl(target.copy(), selector.copy()));
        injector.addStatements(getStatements().stream().map(Statement::copy).toList());
        return injector;
    }
}
