package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MacroCallExpr extends AbstractCallExpr implements Expr {
    public String name;

    public MacroCallExpr(final @NotNull Expr receiver, final @NotNull String name) {
        super(receiver);
        this.name = name;
    }

    private @NotNull MacroDecl getMacro(final @NotNull EvaluationContext context) throws EvaluationException {
        final var scope = context.getScope();
        final var macro = context.resolveByName(MacroDecl.class, name);
        if (macro == null) {
            throw new EvaluationException(String.format("Could not find macro '%s' in current scope %s", name, scope),
                SourceDiagnostic.from(this));
        }
        return macro;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return getMacro(context).getReturnType().evaluateAsConst(context, Type.class);
    }

    private @NotNull List<Pair<@Nullable String, LiteralExpr>> evaluateArguments(final @NotNull EvaluationContext context) throws EvaluationException {
        // @formatter:off
        return getArguments().stream()
            .map(ExceptionUtils.unsafeFunction(pair -> {
                final var name = pair.left();
                final var value = pair.right().evaluateAsConst(context);
                if(name != null) {
                    return new Pair<>(name.evaluateAsConst(context, String.class), value);
                }
                return new Pair<>((String)null, value);
            }))
            .toList();
        // @formatter:on
    }

    private @NotNull Map<String, Expr> resolveArguments(final @NotNull EvaluationContext context,
                                                        final @NotNull Map<String, Type> resolvedParameters,
                                                        final @NotNull MacroDecl macro) throws EvaluationException {
        final var resolvedArgs = evaluateArguments(context);
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
                        String.format("No parameter named '%s' in macro %s", name, ExceptionUtils.rethrowUnchecked(() -> macro.getName(context))),
                        SourceDiagnostic.from(this) // TODO: Improve this to highlight the actual parameter
                    ));
                // @formatter:on
                final var paramType = parameter.getValue();
                if (!paramType.isAssignableFrom(valueType)) {
                    throw new EvaluationException(String.format(
                        "Mismatched argument type %s for parameter %s: %s in call to macro %s",
                        valueType,
                        name,
                        paramType,
                        macro.getName(context)), SourceDiagnostic.from(this, value));
                }
                arguments.put(name, value);
                currentArgIndex = parameters.indexOf(parameter) + 1;
                continue;
            }
            final var parameter = parameters.get(currentArgIndex);
            final var paramType = parameter.getValue();
            if (!paramType.isAssignableFrom(valueType)) {
                throw new EvaluationException(String.format(
                    "Mismatched argument type %s for parameter %s: %s in call to macro %s",
                    valueType,
                    parameter.getKey(),
                    paramType,
                    macro.getName(context)), SourceDiagnostic.from(this, value));
            }
            arguments.put(parameter.getKey(), value);
            currentArgIndex++;
        }
        return arguments;
    }

    private @NotNull List<Expr> remapArguments(final @NotNull EvaluationContext context,
                                               final @NotNull MacroDecl macro) throws EvaluationException {
        final var params = macro.resolveParameters(context);
        final var arguments = resolveArguments(context, params, macro);
        final var sequentialArguments = new ArrayList<Expr>();
        for (final var paramName : params.keySet()) {
            sequentialArguments.add(arguments.get(paramName));
        }
        return sequentialArguments;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var macro = getMacro(context);
        final var arguments = remapArguments(context, macro);
        context.pushFrame(macro); // Create new stack frame for macro body
        context.pushValues(arguments); // Push arguments into callee stack frame
        macro.evaluate(context);
        context.popFrame(); // Frame data will be merged to retain result from callee frame
    }

    @Override
    public @NotNull MacroCallExpr copy() {
        final var call = copyParentAndSourceTo(new MacroCallExpr(getReceiver().copy(), name));
        call.addArguments(getArguments().stream().map(Pair::copy).toList());
        return call;
    }

    @Override
    public @NotNull String toString() { // @formatter:off
        return String.format("%s^(%s)", name, getArguments().stream()
            .map(pair -> pair.right().toString())
            .collect(Collectors.joining(", ")));
    } // @formatter:on
}
