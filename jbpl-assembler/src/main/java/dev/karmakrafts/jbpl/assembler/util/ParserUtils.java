package dev.karmakrafts.jbpl.assembler.util;

import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.parser.ExprParser;
import dev.karmakrafts.jbpl.assembler.parser.TypeParser;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ParserUtils {
    private ParserUtils() {
    }

    public static @NotNull Optional<AccessModifier> parseAccessModifier(final @NotNull AccessModifierContext ctx) {
        // @formatter:off
        return Arrays.stream(AccessModifier.values())
            .filter(mod -> mod.name().equalsIgnoreCase(ctx.getText()))
            .findFirst();
        // @formatter:on
    }

    public static @NotNull Expr parseFunctionName(final @NotNull FunctionNameContext ctx) {
        final var specialName = ctx.specialFunctionName();
        // @formatter:off
        return specialName != null
            ? LiteralExpr.of(specialName.getText())
            : parseRefOrName(ctx.refOrName());
        // @formatter:on
    }

    public static @NotNull Expr parseSignatureOwner(final @NotNull SignatureOwnerContext ctx) {
        final var ref = ctx.reference();
        // @formatter:off
        return ref != null
            ? ExprParser.parse(ref)
            : LiteralExpr.of(TypeParser.parse(ctx.classType()));
        // @formatter:on
    }

    public static @NotNull Expr parseRefOrName(final @NotNull JBPLParser.RefOrNameContext ctx) {
        final var ref = ctx.reference();
        // @formatter:off
        return ref != null
            ? ExprParser.parse(ref)
            : LiteralExpr.of(ctx.nameSegment().getText());
        // @formatter:on
    }

    public static @NotNull Optional<Opcode> parseOpcode(final @NotNull ParserRuleContext ctx) {
        final var text = ctx.getText();
        // @formatter:off
        return Arrays.stream(Opcode.values())
            .filter(op -> op.name().equalsIgnoreCase(text))
            .findFirst();
        // @formatter:on
    }

    public static @NotNull Optional<Opcode> parseOpcode(final @NotNull TerminalNode node) {
        final var text = node.getText();
        // @formatter:off
        return Arrays.stream(Opcode.values())
            .filter(op -> op.name().equalsIgnoreCase(text))
            .findFirst();
        // @formatter:on
    }

    public static @NotNull Expr parseRefOrType(final @NotNull RefOrTypeContext ctx) {
        final var typeRef = ctx.reference();
        // @formatter:off
        return typeRef != null
            ? ExprParser.parse(typeRef)
            : LiteralExpr.of(TypeParser.parse(ctx.type()));
        // @formatter:on
    }

    public static @NotNull Map<Expr, Expr> parseParameters(final @NotNull List<ParameterContext> params) {
        final var mappedParameters = new LinkedHashMap<Expr, Expr>();
        for (final var param : params) {
            final var name = parseRefOrName(param.refOrName());
            final var type = parseRefOrType(param.refOrType());
            mappedParameters.put(name, type);
        }
        return mappedParameters;
    }
}
