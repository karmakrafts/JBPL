package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import org.jetbrains.annotations.NotNull;

public final class EmptyDecl extends AbstractElement implements Declaration {
    private EmptyDecl() {
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {

    }

    @Override
    public @NotNull EmptyDecl copy() {
        return copyParentAndSourceTo(new EmptyDecl());
    }
}
