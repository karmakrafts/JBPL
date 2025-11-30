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

package dev.karmakrafts.jbpl.assembler.eval;

import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.scope.Scope;
import dev.karmakrafts.jbpl.assembler.scope.ScopeResolver;
import dev.karmakrafts.jbpl.assembler.util.Copyable;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public final class StackFrame implements Copyable<StackFrame> {
    public final Scope scope;
    public final ScopeResolver scopeResolver;
    public final Stack<Expr> valueStack = new Stack<>(); // Used for caller<->callee passing
    public final HashMap<String, Expr> namedLocalValues = new HashMap<>(); // Named arguments to the current macro
    public final HashMap<String, Type> namedLocalTypes = new HashMap<>(); // Named type arguments to the current macro
    public final HashMap<String, IntrinsicDefine> intrinsicDefines = new HashMap<>();
    public final HashMap<IntrinsicMacroSignature, IntrinsicMacro> intrinsicMacros = new HashMap<>();

    public final HashMap<String, LocalStatement> locals = new HashMap<>();
    private final HashMap<String, Integer> localIndices = new HashMap<>();
    private final HashMap<String, LabelNode> labelNodes = new HashMap<>();
    public int localFrameOffset = 0; // This may be adjusted by the patched method in the future
    private int localIndex = 0;

    public StackFrame(final @NotNull Scope scope) {
        this.scope = scope;
        scopeResolver = new ScopeResolver(scope);
    }

    public void resetLocalDefines() {
        if (!(scope.owner() instanceof ElementContainer container)) {
            return;
        }
        container.accept(LocalDefineResetVisitor.INSTANCE);
    }

    public @NotNull LabelNode getOrCreateLabelNode(final @NotNull String name) {
        return labelNodes.computeIfAbsent(name, n -> new LabelNode());
    }

    public @NotNull Optional<String> getLabelName(final @NotNull LabelNode label) { // @formatter:off
        return labelNodes.entrySet().stream()
            .filter(entry -> entry.getValue() == label)
            .map(Entry::getKey)
            .findFirst();
    } // @formatter:on

    public int getOrAssignLocalIndex(final @NotNull String name,
                                     final @NotNull EvaluationContext context) throws EvaluationException {
        var index = localIndices.get(name);
        if (index == null) {
            final var associatedLocal = locals.get(name);
            if (associatedLocal != null) {
                final var indexExpr = associatedLocal.getIndex();
                if (indexExpr.isUnit()) {
                    index = indexExpr.evaluateAs(context, Integer.class);
                }
                else {
                    index = localFrameOffset + localIndex++;
                }
            }
            else {
                index = localFrameOffset + localIndex++;
            }
            localIndices.put(name, index);
        }
        return index;
    }

    @Override
    public StackFrame copy() {
        final var frame = new StackFrame(scope);
        frame.valueStack.addAll(valueStack.stream().map(Expr::copy).toList());
        // @formatter:off
        frame.namedLocalValues.putAll(namedLocalValues.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), entry.getValue().copy()))
            .collect(Collectors.toMap(Pair::left, Pair::right)));
        frame.namedLocalTypes.putAll(namedLocalTypes);
        frame.locals.putAll(locals.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), entry.getValue().copy()))
            .collect(Collectors.toMap(Pair::left, Pair::right)));
        // @formatter:on
        frame.labelNodes.putAll(labelNodes);
        return frame;
    }
}
