package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.type.PreproClassTypeRef;
import org.jetbrains.annotations.NotNull;

public final class ClassInstantiationExpr extends AbstractCallExpr implements Expr {
    public ClassInstantiationExpr(final @NotNull PreproClassTypeRef type) {
        super(null); // Class instantiations don't have a receiver
    }
}
