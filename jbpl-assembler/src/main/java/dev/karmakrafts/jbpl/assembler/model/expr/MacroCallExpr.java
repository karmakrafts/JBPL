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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.eval.IntrinsicMacroArguments;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.scope.ScopeResolver;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class MacroCallExpr extends AbstractCallExpr implements Expr {
    public static final int NAME_INDEX = RECEIVER_INDEX + 1;

    public MacroCallExpr(final @NotNull Expr name) {
        super();
        addExpression(name);
    }

    private static @NotNull List<Pair<@Nullable String, ConstExpr>> evaluateNamedValues(final @NotNull List<Pair<@Nullable Expr, Expr>> values,
                                                                                        final @NotNull EvaluationContext context) {
        // @formatter:off
        return values.stream()
            .map(ExceptionUtils.unsafeFunction(pair -> {
                final var name = pair.left();
                final var value = pair.right().evaluateAsConst(context);
                if(name != null) {
                    return new Pair<>(name.evaluateAs(context, String.class), value);
                }
                return new Pair<>((String)null, value);
            }))
            .toList();
        // @formatter:on
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    private @NotNull MacroDecl getMacro(final @NotNull String name,
                                        final @NotNull EvaluationContext context) throws EvaluationException {
        final var scope = context.getScope();
        var macro = context.resolveByName(MacroDecl.class, name);
        if (macro == null) { // Second attempt is for resolving by scope receiver
            final var receiver = getReceiver();
            if (!(receiver instanceof ScopeReceiverExpr scopeReceiverExpr)) {
                final var message = String.format("Cannot resolve define %s by scope receiver %s", name, receiver);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
            final var resolver = new ScopeResolver(scopeReceiverExpr.scope);
            macro = resolver.resolve(MacroDecl.class,
                ExceptionUtils.unsafePredicate(m -> m.getName(context).equals(name)));
        }
        if (macro == null) {
            throw new EvaluationException(String.format("Could not find macro '%s' in current scope %s", name, scope),
                SourceDiagnostic.from(this),
                context.createStackTrace());
        }
        return macro;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var name = getName().evaluateAs(context, String.class);
        return getMacro(name, context).getReturnType().evaluateAs(context, Type.class).resolveIfNeeded(context);
    }

    private @NotNull Map<String, Expr> resolveTypeArguments(final @NotNull EvaluationContext context,
                                                            final @NotNull Map<String, Type> resolvedParameters,
                                                            final @NotNull String macroName) throws EvaluationException {
        final var resolvedArgs = evaluateNamedValues(getTypeArguments(), context);
        final var arguments = new HashMap<String, Expr>();
        final var parameters = new ArrayList<>(resolvedParameters.entrySet());
        var currentArgIndex = 0;
        for (final var resolvedArg : resolvedArgs) {
            final var name = resolvedArg.left();
            final var value = resolvedArg.right();
            final var valueType = value.getType(context);
            if (name != null) {
                // @formatter:off
                final var parameter = parameters.stream()
                    .filter(entry -> entry.getKey().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new EvaluationException(
                        String.format("No parameter named '%s' in macro %s", name, macroName),
                        SourceDiagnostic.from(this), context.createStackTrace()
                    ));
                // @formatter:on
                final var paramType = parameter.getValue();
                if (!paramType.isAssignableFrom(valueType, context)) {
                    throw new EvaluationException(String.format(
                        "Mismatched argument type %s for parameter %s: %s in call to macro %s",
                        valueType,
                        name,
                        paramType,
                        macroName), SourceDiagnostic.from(this, value), context.createStackTrace());
                }
                arguments.put(name, value);
                currentArgIndex = parameters.indexOf(parameter) + 1;
                continue;
            }
            final var parameter = parameters.get(currentArgIndex);
            final var paramType = parameter.getValue();
            if (!paramType.isAssignableFrom(valueType, context)) {
                throw new EvaluationException(String.format(
                    "Mismatched argument type %s for parameter %s: %s in call to macro %s",
                    valueType,
                    parameter.getKey(),
                    paramType,
                    macroName), SourceDiagnostic.from(this, value), context.createStackTrace());
            }
            arguments.put(parameter.getKey(), value);
            currentArgIndex++;
        }
        return arguments;
    }

    private @NotNull Map<String, Expr> resolveArguments(final @NotNull EvaluationContext context,
                                                        final @NotNull Map<String, Type> resolvedParameters,
                                                        final @NotNull String macroName) throws EvaluationException {
        final var resolvedArgs = evaluateNamedValues(getArguments(), context);
        final var arguments = new HashMap<String, Expr>();
        final var parameters = new ArrayList<>(resolvedParameters.entrySet());
        var currentArgIndex = 0;
        for (final var resolvedArg : resolvedArgs) {
            final var name = resolvedArg.left();
            final var value = resolvedArg.right();
            final var valueType = value.getType(context);
            if (name != null) {
                // @formatter:off
                final var parameter = parameters.stream()
                    .filter(entry -> entry.getKey().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new EvaluationException(
                        String.format("No parameter named '%s' in macro %s", name, macroName),
                        SourceDiagnostic.from(this), context.createStackTrace()
                    ));
                // @formatter:on
                final var paramType = parameter.getValue();
                if (!paramType.isAssignableFrom(valueType, context)) {
                    throw new EvaluationException(String.format(
                        "Mismatched argument type %s for parameter %s: %s in call to macro %s",
                        valueType,
                        name,
                        paramType,
                        macroName), SourceDiagnostic.from(this, value), context.createStackTrace());
                }
                arguments.put(name, value);
                currentArgIndex = parameters.indexOf(parameter) + 1;
                continue;
            }
            final var parameter = parameters.get(currentArgIndex);
            final var paramType = parameter.getValue();
            if (!paramType.isAssignableFrom(valueType, context)) {
                throw new EvaluationException(String.format(
                    "Mismatched argument type %s for parameter %s: %s in call to macro %s",
                    valueType,
                    parameter.getKey(),
                    paramType,
                    macroName), SourceDiagnostic.from(this, value), context.createStackTrace());
            }
            arguments.put(parameter.getKey(), value);
            currentArgIndex++;
        }
        return arguments;
    }

    private @NotNull List<Expr> remapArguments(final @NotNull EvaluationContext context,
                                               final @NotNull String macroName,
                                               final @NotNull Map<String, Type> params) throws EvaluationException {
        final var arguments = resolveArguments(context, params, macroName);
        final var sequentialArguments = new ArrayList<Expr>();
        for (final var paramName : params.keySet()) {
            sequentialArguments.add(arguments.get(paramName));
        }
        return sequentialArguments;
    }

    private @NotNull List<Expr> remapTypeArguments(final @NotNull EvaluationContext context,
                                                   final @NotNull String macroName,
                                                   final @NotNull Map<String, Type> params) throws EvaluationException {
        return List.of(); // TODO: implement this
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var name = getName().evaluateAs(context, String.class);
        // @formatter:off
        final var intrinsicMacro = context.peekFrame().intrinsicMacros.entrySet().stream()
            .filter(entry -> entry.getKey().name().equals(name))
            .map(Entry::getValue)
            .findFirst();
        // @formatter:on
        if (intrinsicMacro.isPresent()) { // Intrinsic macros always shadow everything else
            final var macro = intrinsicMacro.get();
            final var typeArguments = remapTypeArguments(context, name, macro.signature().typeParameters());
            final var arguments = remapArguments(context, name, macro.signature().parameters());
            macro.callback().accept(context, new IntrinsicMacroArguments(typeArguments, arguments));
            return;
        }
        final var macro = getMacro(name, context);
        final var typeArguments = remapTypeArguments(context, name, macro.resolveTypeParameters(context));
        final var arguments = remapArguments(context, name, macro.resolveParameters(context));
        context.pushFrame(macro); // Create new stack frame for macro body
        context.peekFrame().resetLocalDefines(); // Reset all local defines within the macro before invoking anything
        context.pushValues(typeArguments); // Push type arguments into callee stack frame
        context.pushValues(arguments); // Push arguments into callee stack frame
        macro.evaluate(context);
        context.popFrame(); // Frame data will be merged to retain result from callee frame
    }

    @Override
    public @NotNull MacroCallExpr copy() {
        final var call = copyParentAndSourceTo(new MacroCallExpr(getName().copy()));
        call.setReceiver(getReceiver().copy());
        call.addArguments(getArguments().stream().map(Pair::copy).toList());
        return call;
    }

    @Override
    public @NotNull String toString() { // @formatter:off
        return String.format("%s^(%s)", getName(), getArguments().stream()
            .map(pair -> pair.right().toString())
            .collect(Collectors.joining(", ")));
    } // @formatter:on
}
