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

package dev.karmakrafts.jbpl.assembler.scope;

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;
import java.util.function.Consumer;

public abstract class ScopeAwareElementVisitor implements ElementVisitor {
    private static final Consumer<Scope> IDENTITY_CALLBACK = owner -> {
    };
    protected final Stack<Scope> scopeStack = new Stack<>();
    protected Consumer<Scope> scopeEnter = IDENTITY_CALLBACK;
    protected Consumer<Scope> scopeLeave = IDENTITY_CALLBACK;

    public void restoreFrom(final @NotNull Stack<Scope> scopeStack) {
        this.scopeStack.addAll(scopeStack);
    }

    protected @NotNull Scope getScope() {
        return scopeStack.peek();
    }

    public void onScopeEnter(final @NotNull Consumer<Scope> callback) {
        if (scopeEnter == IDENTITY_CALLBACK) {
            scopeEnter = callback;
            return;
        }
        final var oldHandle = scopeEnter;
        scopeEnter = scope -> {
            oldHandle.accept(scope);
            callback.accept(scope);
        };
    }

    public void onScopeLeave(final @NotNull Consumer<Scope> callback) {
        if (scopeLeave == IDENTITY_CALLBACK) {
            scopeLeave = callback;
            return;
        }
        final var oldHandle = scopeLeave;
        scopeLeave = scope -> {
            oldHandle.accept(scope);
            callback.accept(scope);
        };
    }

    private void pushScope(final @NotNull ScopeOwner scopeOwner) {
        final var parentScope = scopeStack.isEmpty() ? null : scopeStack.peek();
        final var scope = new Scope(parentScope, scopeOwner);
        scopeStack.push(scope);
        scopeEnter.accept(scope);
    }

    private void popScope() {
        scopeLeave.accept(scopeStack.pop());
    }

    @Override
    public @NotNull Element visitElement(final @NotNull Element element) {
        var hasScope = false;
        if (element instanceof ScopeOwner scopeOwner) {
            pushScope(scopeOwner);
            hasScope = true;
        }
        final var result = ElementVisitor.super.visitElement(element);
        if (hasScope) {
            popScope();
        }
        return result;
    }
}
