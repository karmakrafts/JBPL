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

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.*;
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

    public @NotNull AssemblyFile visitFileInScope(final @NotNull AssemblyFile file) {
        return file;
    }

    @Override
    public @NotNull AssemblyFile visitFile(final @NotNull AssemblyFile file) {
        pushScope(file);
        final var transformedFile = visitFileInScope(ElementVisitor.super.visitFile(file));
        popScope();
        return transformedFile;
    }

    public @NotNull Declaration visitFunctionInScope(final @NotNull FunctionDecl functionDecl) {
        return functionDecl;
    }

    @Override
    public @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        pushScope(functionDecl);
        var transformedFunction = ElementVisitor.super.visitFunction(functionDecl);
        if (transformedFunction instanceof FunctionDecl transformedDecl) {
            transformedFunction = visitFunctionInScope(transformedDecl);
        }
        popScope();
        return transformedFunction;
    }

    public @NotNull Declaration visitMacroInScope(final @NotNull MacroDecl macroDecl) {
        return macroDecl;
    }

    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        pushScope(macroDecl);
        var transformedMacro = ElementVisitor.super.visitMacro(macroDecl);
        if (transformedMacro instanceof MacroDecl transformedDecl) {
            transformedMacro = visitMacroInScope(transformedDecl);
        }
        popScope();
        return transformedMacro;
    }

    public @NotNull Declaration visitInjectorInScope(final @NotNull InjectorDecl injectorDecl) {
        return injectorDecl;
    }

    @Override
    public @NotNull Declaration visitInjector(final @NotNull InjectorDecl injectorDecl) {
        pushScope(injectorDecl);
        var transformedInjector = ElementVisitor.super.visitInjector(injectorDecl);
        if (transformedInjector instanceof InjectorDecl transformedDecl) {
            transformedInjector = visitInjectorInScope(transformedDecl);
        }
        popScope();
        return transformedInjector;
    }

    public @NotNull Declaration visitSelectorInScope(final @NotNull SelectorDecl selectorDecl) {
        return selectorDecl;
    }

    @Override
    public @NotNull Declaration visitSelector(@NotNull SelectorDecl selectorDecl) {
        pushScope(selectorDecl);
        var transformedSelector = ElementVisitor.super.visitSelector(selectorDecl);
        if (transformedSelector instanceof SelectorDecl transformedDecl) {
            transformedSelector = visitSelectorInScope(transformedDecl);
        }
        popScope();
        return transformedSelector;
    }
}
