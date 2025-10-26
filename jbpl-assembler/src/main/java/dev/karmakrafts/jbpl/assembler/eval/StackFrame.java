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
import dev.karmakrafts.jbpl.assembler.model.statement.LocalStatement;
import dev.karmakrafts.jbpl.assembler.scope.Scope;
import dev.karmakrafts.jbpl.assembler.scope.ScopeResolver;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.Stack;

public final class StackFrame {
    public final Scope scope;
    public final ScopeResolver scopeResolver;
    public final Stack<Expr> valueStack = new Stack<>(); // Used for caller<->callee passing
    public final HashMap<String, Expr> arguments = new HashMap<>(); // Named arguments of the current macro
    public final InsnList instructionBuffer = new InsnList();
    public final HashMap<String, LocalStatement> locals = new HashMap<>();
    private final HashMap<String, LabelNode> labelNodes = new HashMap<>();

    public StackFrame(final @NotNull Scope scope) {
        this.scope = scope;
        scopeResolver = new ScopeResolver(scope);
    }

    public @NotNull LabelNode getOrCreateLabelNode(final @NotNull String name) {
        return labelNodes.computeIfAbsent(name, n -> new LabelNode());
    }
}
