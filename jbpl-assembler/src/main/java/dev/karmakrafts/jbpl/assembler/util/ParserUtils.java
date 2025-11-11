package dev.karmakrafts.jbpl.assembler.util;

import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.parser.ExprParser;
import dev.karmakrafts.jbpl.assembler.parser.ParserException;
import dev.karmakrafts.jbpl.assembler.parser.TypeParser;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class ParserUtils {
    private ParserUtils() {
    }

    public static @NotNull Pair<@Nullable Expr, Expr> parseArgument(final @NotNull ArgumentContext ctx) throws ParserException {
        final var namedCtx = ctx.namedArgument();
        if (namedCtx != null) {
            return new Pair<>(ParserUtils.parseExprOrName(namedCtx.exprOrName()), ExprParser.parse(namedCtx.expr()));
        }
        return new Pair<>(null, ExprParser.parse(ctx.expr()));
    }

    public static @NotNull List<Pair<@Nullable Expr, Expr>> parseArguments(final @NotNull List<ArgumentContext> args) { // @formatter:off
        return args.stream()
            .map(ExceptionUtils.unsafeFunction(ParserUtils::parseArgument))
            .toList();
    } // @formatter:on

    public static @NotNull Optional<AccessModifier> parseAccessModifier(final @NotNull AccessModifierContext ctx) {
        // @formatter:off
        return Arrays.stream(AccessModifier.values())
            .filter(mod -> mod.name().equalsIgnoreCase(ctx.getText()))
            .findFirst();
        // @formatter:on
    }

    public static @NotNull Expr parseFunctionName(final @NotNull FunctionNameContext ctx) throws ParserException {
        final var specialName = ctx.specialFunctionName();
        // @formatter:off
        return specialName != null
            ? LiteralExpr.of(specialName.getText())
            : parseExprOrName(ctx.exprOrName());
        // @formatter:on
    }

    public static @NotNull Expr parseSignatureOwner(final @NotNull SignatureOwnerContext ctx) throws ParserException {
        final var expr = ctx.wrappedExpr();
        // @formatter:off
        return expr != null
            ? ExprParser.parse(expr)
            : LiteralExpr.of(ExceptionUtils.rethrowUnchecked(() -> TypeParser.parse(ctx.classType())));
        // @formatter:on
    }

    public static @NotNull Expr parseExprOrName(final @NotNull JBPLParser.ExprOrNameContext ctx) throws ParserException {
        final var expr = ctx.wrappedExpr();
        // @formatter:off
        return expr != null
            ? ExprParser.parse(expr)
            : LiteralExpr.of(ctx.nameSegment().getText());
        // @formatter:on
    }

    public static @NotNull Opcode parseOpcode(final @NotNull ParserRuleContext ctx) throws ParserException {
        final var text = ctx.getText();
        // @formatter:off
        return Arrays.stream(Opcode.values())
            .filter(op -> op.name().equalsIgnoreCase(text))
            .findFirst()
            .orElseThrow(() -> new ParserException(String.format("Could not parse opcode '%s'", ctx.getText()), null));
        // @formatter:on
    }

    public static @NotNull Optional<Opcode> maybeParseOpcode(final @NotNull ParserRuleContext ctx) {
        final var text = ctx.getText();
        // @formatter:off
        return Arrays.stream(Opcode.values())
            .filter(op -> op.name().equalsIgnoreCase(text))
            .findFirst();
        // @formatter:on
    }

    public static @NotNull Opcode parseOpcode(final @NotNull TerminalNode node) throws ParserException {
        final var text = node.getText();
        // @formatter:off
        return Arrays.stream(Opcode.values())
            .filter(op -> op.name().equalsIgnoreCase(text))
            .findFirst()
            .orElseThrow(() -> new ParserException(String.format("Could not parse opcode '%s'", node.getText()), null));
        // @formatter:on
    }

    public static @NotNull Expr parseExprOrType(final @NotNull ExprOrTypeContext ctx) throws ParserException {
        final var expr = ctx.wrappedExpr();
        // @formatter:off
        return expr != null
            ? ExprParser.parse(expr)
            : LiteralExpr.of(ExceptionUtils.rethrowUnchecked(() -> TypeParser.parse(ctx.type())));
        // @formatter:on
    }

    public static @NotNull List<Pair<Expr, Expr>> parseParameters(final @NotNull List<ParameterContext> params) throws ParserException {
        final var paramPairs = new ArrayList<Pair<Expr, Expr>>();
        for (final var param : params) {
            final var name = parseExprOrName(param.exprOrName());
            final var type = parseExprOrType(param.exprOrType());
            paramPairs.add(new Pair<>(name, type));
        }
        return paramPairs;
    }
}
