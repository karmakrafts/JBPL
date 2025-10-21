package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import org.jetbrains.annotations.NotNull;

public interface SignatureExpr extends Expr {
    @NotNull String evaluateAsConstDescriptor(final @NotNull AssemblerContext context);
}
