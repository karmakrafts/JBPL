package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class LocalStatement extends AbstractElement implements Statement {
    public Expr name;

    public LocalStatement(final @NotNull Expr name) {
        this.name = name;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        // Make this local available in the current frame during further evaluation
        final var name = this.name.evaluateAsConst(context, String.class);
        context.peekFrame().locals.put(name, this);
    }
}
