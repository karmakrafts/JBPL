/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class MacroDecl extends AbstractElementContainer implements Declaration, ScopeOwner, NamedElement {
    private final ArrayList<Pair<Expr, Expr>> parameters = new ArrayList<>();
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
        for (final var pair : parameters) {
            pair.left().setParent(null);
            pair.right().setParent(null);
        }
        parameters.clear();
    }

    public void addParameter(final @NotNull Expr name, final @NotNull Expr type) {
        name.setParent(this);
        type.setParent(this);
        parameters.add(new Pair<>(name, type));
    }

    public void addParameters(final @NotNull Collection<Pair<Expr, Expr>> parameters) {
        for (final var entry : parameters) {
            entry.left().setParent(this);
            entry.right().setParent(this);
        }
        this.parameters.addAll(parameters);
    }

    public @NotNull List<Pair<Expr, Expr>> getParameters() {
        return parameters;
    }

    public @NotNull Map<String, Type> resolveParameters(final @NotNull EvaluationContext context) throws EvaluationException {
        final var params = getParameters();
        if (params.isEmpty()) {
            return Map.of();
        }
        final var resolvedParams = new LinkedHashMap<String, Type>(16, 0.75F, true);
        for (final var pair : params) {
            final var name = pair.left().evaluateAs(context, String.class);
            final var type = pair.right().evaluateAs(context, Type.class);
            resolvedParams.put(name, type);
        }
        return resolvedParams;
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return name.evaluateAs(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        // Pop as many args as we have param types from callee frame
        final var argumentValues = context.popValues(parameters.size());
        final var paramNames = resolveParameters(context).keySet();
        final var arguments = new HashMap<String, Expr>();
        var index = 0;
        for (final var name : paramNames) {
            arguments.put(name, argumentValues.get(index++));
        }
        context.peekFrame().namedLocalValues.putAll(arguments); // Make current macro args available to child elements
        final var elements = getElements();
        for (final var element : elements) {
            element.evaluate(context);
            if (context.clearReturnMask()) { // Macro scope always clears all status flags
                break; // Break loop if we returned
            }
        }
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
        final var macro = copyParentAndSourceTo(new MacroDecl(getName().copy(), getReturnType().copy()));
        macro.addParameters(getParameters().stream().map(Pair::copy).toList());
        macro.addElements(getElements().stream().map(Element::copy).toList());
        return macro;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
