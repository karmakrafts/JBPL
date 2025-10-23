package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import org.jetbrains.annotations.NotNull;

public final class CompoundExpr extends AbstractElementContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) throws EvaluationException {
        return TypeCommonizer.getCommonType(this, context).orElseThrow();
    }
}
