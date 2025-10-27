package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MacroCallExpr extends AbstractCallExpr implements Expr {
    public String name;

    public MacroCallExpr(final @NotNull Expr receiver, final @NotNull String name) {
        super(receiver);
        this.name = name;
    }

    private @NotNull MacroDecl getMacro(final @NotNull EvaluationContext context) {
        final var scope = context.getScope();
        final var macro = context.resolveByName(MacroDecl.class, name);
        if (macro == null) {
            throw new IllegalStateException(String.format("Could not find macro '%s' in current scope %s",
                name,
                scope));
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
                                                        final @NotNull Map<String, Type> resolvedParameters) throws EvaluationException {
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
                    .orElseThrow(EvaluationException::new); // TODO: add better error messagge
                // @formatter:on
                final var paramType = parameter.getValue();
                // TODO: typechecking goes here
                arguments.put(name, value);
                currentArgIndex = parameters.indexOf(parameter) + 1;
                continue;
            }
            final var parameter = parameters.get(currentArgIndex);
            // TODO: type checking goes here
            arguments.put(parameter.getKey(), value);
            currentArgIndex++;
        }
        return arguments;
    }

    private @NotNull List<Expr> remapArguments(final @NotNull EvaluationContext context,
                                               final @NotNull MacroDecl macro) throws EvaluationException {
        final var params = macro.resolveParameters(context);
        final var arguments = resolveArguments(context, params);
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
}
