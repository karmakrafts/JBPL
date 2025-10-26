package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import org.jetbrains.annotations.NotNull;

public final class CompoundExpr extends AbstractElementContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return TypeCommonizer.getCommonType(getElements(), context).orElseThrow();
    }

    @Override
    public @NotNull CompoundExpr copy() {
        final var result = copyParentAndSourceTo(new CompoundExpr());
        result.addElements(getElements().stream().map(Element::copy).toList());
        return result;
    }
}
