package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.ReturnTarget;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public final class MacroDecl extends AbstractElementContainer implements Declaration, ScopeOwner, ReturnTarget {
    public final LinkedHashMap<Expr, Expr> parameterTypes = new LinkedHashMap<>();
    public Expr returnType;
    public String name;

    public MacroDecl(final @NotNull String name, final @NotNull Expr returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public @NotNull Type evaluateReturnType(final @NotNull AssemblerContext context) {
        return returnType.evaluateAsConst(context, Type.class);
    }

    public @NotNull LinkedHashMap<String, Type> evaluateParameterTypes(final @NotNull AssemblerContext context) {
        final var types = new LinkedHashMap<String, Type>();
        for (final var entry : parameterTypes.entrySet()) {
            final var name = entry.getKey().evaluateAsConst(context, String.class);
            final var type = entry.getValue().evaluateAsConst(context, Type.class);
            types.put(name, type);
        }
        return types;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {

    }

    @Override
    public boolean mergeLocalFrameDataOnFrameExit() {
        return true; // Macro calls always their local frame data into the parent frame
    }
}
