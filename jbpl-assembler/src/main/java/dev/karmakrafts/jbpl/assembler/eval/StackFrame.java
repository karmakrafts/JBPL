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

import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.scope.Scope;
import dev.karmakrafts.jbpl.assembler.scope.ScopeResolver;
import dev.karmakrafts.jbpl.assembler.util.Copyable;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.Stack;
import java.util.stream.Collectors;

public final class StackFrame implements Copyable<StackFrame> {
    public final Scope scope;
    public final ScopeResolver scopeResolver;
    public final Stack<Expr> valueStack = new Stack<>(); // Used for caller<->callee passing
    public final HashMap<String, Expr> injectedValues = new HashMap<>(); // Named arguments of the current macro
    public final InsnList instructionBuffer = new InsnList();
    public final HashMap<String, LocalStatement> locals = new HashMap<>();
    private final HashMap<String, Integer> localIndices = new HashMap<>();
    private final HashMap<String, LabelNode> labelNodes = new HashMap<>();
    public int localFrameOffset = 0; // This may be adjusted by the patched method in the future
    private int localIndex = 0;

    public StackFrame(final @NotNull Scope scope) {
        this.scope = scope;
        scopeResolver = new ScopeResolver(scope);
    }

    public @NotNull LabelNode getOrCreateLabelNode(final @NotNull String name) {
        return labelNodes.computeIfAbsent(name, n -> new LabelNode());
    }

    public int getOrAssignLocalIndex(final @NotNull String name,
                                     final @NotNull EvaluationContext context) throws EvaluationException {
        var index = localIndices.get(name);
        if (index == null) {
            final var associatedLocal = locals.get(name);
            if (associatedLocal != null) {
                final var indexExpr = associatedLocal.getIndex();
                if (indexExpr != LiteralExpr.UNIT) {
                    index = indexExpr.evaluateAsConst(context, Integer.class);
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
        frame.injectedValues.putAll(injectedValues.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), entry.getValue().copy()))
            .collect(Collectors.toMap(Pair::left, Pair::right)));
        // @formatter:on
        frame.instructionBuffer.add(instructionBuffer);
        // @formatter:off
        frame.locals.putAll(locals.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), entry.getValue().copy()))
            .collect(Collectors.toMap(Pair::left, Pair::right)));
        // @formatter:on
        frame.labelNodes.putAll(labelNodes);
        return frame;
    }
}
