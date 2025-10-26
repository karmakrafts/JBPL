package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MacroDecl extends AbstractElementContainer implements Declaration, ScopeOwner, NamedElement {
    private final LinkedHashMap<Expr, Expr> parameterTypes = new LinkedHashMap<>();
    private Expr name;
    private Expr returnType;

    public MacroDecl(final @NotNull Expr name, final @NotNull Expr returnType) {
        name.setParent(this);
        this.name = name;
        returnType.setParent(this);
        this.returnType = returnType;
    }

    public @NotNull Expr getName() {
        return name;
    }

    public void setName(final @NotNull Expr name) {
        if (this.name != null) {
            this.name.setParent(null);
        }
        name.setParent(this);
        this.name = name;
    }

    public @NotNull Expr getReturnType() {
        return returnType;
    }

    public void setReturnType(final @NotNull Expr returnType) {
        if (this.returnType != null) {
            this.returnType.setParent(null);
        }
        this.returnType = returnType;
    }

    public void clearParameters() {
        for (final var entry : parameterTypes.entrySet()) {
            entry.getKey().setParent(null);
            entry.getValue().setParent(null);
        }
        parameterTypes.clear();
    }

    public void addParameter(final @NotNull Expr name, final @NotNull Expr type) {
        name.setParent(this);
        type.setParent(this);
        parameterTypes.put(name, type);
    }

    public void addParameters(final @NotNull Map<Expr, Expr> parameters) {
        for (final var entry : parameters.entrySet()) {
            entry.getKey().setParent(this);
            entry.getValue().setParent(this);
        }
        parameterTypes.putAll(parameters);
    }

    public @NotNull Map<Expr, Expr> getParameters() {
        return parameterTypes;
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return name.evaluateAsConst(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        // Pop as many args as we have param types from callee frame
        final var argumentValues = context.popValues(parameterTypes.size());
        final var arguments = new HashMap<String, Expr>();
        var index = 0;
        for (final var entry : parameterTypes.entrySet()) {
            final var name = entry.getKey().evaluateAsConst(context, String.class);
            arguments.put(name, argumentValues.get(index++));
        }
        context.peekFrame().arguments.putAll(arguments); // Make current macro args available to child elements
        super.evaluate(context); // Evaluate body
    }

    @Override
    public boolean isEvaluatedDirectly() {
        return false; // When evaluating the file, this is not evaluated directly, but only indirectly through calls
    }

    @Override
    public boolean mergeFrameDataOnFrameExit() {
        return true; // Macro calls always merge their local frame data into the parent frame
    }

    @Override
    public @NotNull MacroDecl copy() {
        return copyParentAndSourceTo(new MacroDecl(getName().copy(), getReturnType().copy()));
    }
}
