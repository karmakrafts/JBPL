package dev.karmakrafts.jbpl.assembler;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.*;
import dev.karmakrafts.jbpl.assembler.model.element.ElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public abstract class ScopeAwareElementVisitor implements ElementVisitor {
    protected final Stack<Scope> scopeStack = new Stack<>();

    protected @NotNull Scope getScope() {
        return scopeStack.peek();
    }

    public @NotNull AssemblyFile visitFileInScope(final @NotNull AssemblyFile file) {
        return file;
    }

    @Override
    public @NotNull AssemblyFile visitFile(final @NotNull AssemblyFile file) {
        scopeStack.push(new Scope(null, file));
        final var transformedFile = visitFileInScope(ElementVisitor.super.visitFile(file));
        scopeStack.pop();
        return transformedFile;
    }

    public @NotNull Declaration visitSelectorInScope(final @NotNull SelectorDecl selectorDecl) {
        return selectorDecl;
    }

    @Override
    public @NotNull Declaration visitSelector(final @NotNull SelectorDecl selectorDecl) {
        scopeStack.push(new Scope(getScope(), selectorDecl));
        var transformedSelector = ElementVisitor.super.visitSelector(selectorDecl);
        if (transformedSelector instanceof SelectorDecl decl) {
            transformedSelector = visitSelectorInScope(decl);
        }
        scopeStack.pop();
        return transformedSelector;
    }

    public @NotNull Declaration visitInjectorInScope(final @NotNull InjectorDecl injectorDecl) {
        return injectorDecl;
    }

    @Override
    public @NotNull Declaration visitInjector(final @NotNull InjectorDecl injectorDecl) {
        scopeStack.push(new Scope(getScope(), injectorDecl));
        var transformedInjector = ElementVisitor.super.visitInjector(injectorDecl);
        if (transformedInjector instanceof InjectorDecl decl) {
            transformedInjector = visitInjectorInScope(decl);
        }
        scopeStack.pop();
        return transformedInjector;
    }

    public @NotNull Declaration visitFunctionInScope(final @NotNull FunctionDecl functionDecl) {
        return functionDecl;
    }

    @Override
    public @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        scopeStack.push(new Scope(getScope(), functionDecl));
        var transformedFunction = ElementVisitor.super.visitFunction(functionDecl);
        if (transformedFunction instanceof FunctionDecl decl) {
            transformedFunction = visitFunctionInScope(decl);
        }
        scopeStack.pop();
        return transformedFunction;
    }

    public @NotNull Declaration visitMacroInScope(final @NotNull MacroDecl macroDecl) {
        return macroDecl;
    }

    @Override
    public @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        scopeStack.push(new Scope(getScope(), macroDecl));
        var transformedMacro = ElementVisitor.super.visitMacro(macroDecl);
        if (transformedMacro instanceof MacroDecl decl) {
            transformedMacro = visitMacroInScope(decl);
        }
        scopeStack.pop();
        return transformedMacro;
    }
}
