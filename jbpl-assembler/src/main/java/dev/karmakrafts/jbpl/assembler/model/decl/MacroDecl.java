package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public final class MacroDecl extends AbstractElementContainer implements Declaration, ScopeOwner {
    public final LinkedHashMap<Expr, Expr> parameterTypes = new LinkedHashMap<>();
    public Expr name;
    public Expr returnType;

    public MacroDecl(final @NotNull Expr name, final @NotNull Expr returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
    }

    @Override
    public boolean mergeFrameInstructionsOnFrameExit() {
        return true; // Macro calls always their local frame data into the parent frame
    }
}
