package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReferenceExpr extends AbstractReceiverExpr implements Expr, ExprContainer {
    public String name;

    public ReferenceExpr(final @NotNull String name) {
        super();
        this.name = name;
    }

    private @Nullable Expr findArgument(final @NotNull EvaluationContext context) {
        return context.peekFrame().arguments.get(name);
    }

    private @NotNull DefineStatement getDefine(final @NotNull EvaluationContext context) throws EvaluationException {
        final var scope = context.getScope();
        final var define = context.resolveByName(DefineStatement.class, name);
        if (define == null) {
            final var message = String.format("Could not find define '%s' in scope %s", name, scope);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        return define;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var argument = findArgument(context);
        if (argument != null) {
            return argument.getType(context);
        }
        return getDefine(context).getType().evaluateAsConst(context, Type.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var argument = findArgument(context);
        // If we find a named argument in the current frame that matches the name of this ref
        if (argument != null) {
            context.pushValue(argument.evaluateAsConst(context));
            return;
        }
        // Otherwise resolve as define
        getDefine(context).evaluate(context);
    }

    @Override
    public @NotNull ReferenceExpr copy() {
        final var reference = copyParentAndSourceTo(new ReferenceExpr(name));
        reference.setReceiver(getReceiver().copy());
        return reference;
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
