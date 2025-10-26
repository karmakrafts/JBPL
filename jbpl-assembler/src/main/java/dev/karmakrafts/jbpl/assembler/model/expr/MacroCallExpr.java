package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class MacroCallExpr extends AbstractCallExpr implements Expr {
    public String name;

    public MacroCallExpr(final @NotNull Expr receiver, final @NotNull String name) {
        super(receiver);
        this.name = name;
    }

    private @NotNull MacroDecl getMacro(final @NotNull EvaluationContext context) {
        final var scope = context.getScope();
        final var macro = context.resolveByName(MacroDecl.class, name);
        if (macro == null) {
            throw new IllegalStateException(String.format("Could not find macro '%s' in current scope %s",
                name,
                scope));
        }
        return macro;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return getMacro(context).getReturnType().evaluateAsConst(context, Type.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var macro = getMacro(context);
        context.pushFrame(macro); // Create new stack frame for macro body
        context.pushValues(getArguments()); // Push arguments into callee stack frame
        macro.evaluate(context);
        context.popFrame(); // Frame data will be merged to retain result from callee frame
    }

    @Override
    public @NotNull MacroCallExpr copy() {
        return copyParentAndSourceTo(new MacroCallExpr(getReceiver().copy(), name));
    }
}
