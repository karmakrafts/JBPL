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

package dev.karmakrafts.jbpl.assembler.util;

import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.parser.ExprParser;
import dev.karmakrafts.jbpl.assembler.parser.ParserException;
import dev.karmakrafts.jbpl.assembler.parser.TypeParser;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public final class ParserUtils {
    private ParserUtils() {
    }

    public static @NotNull String unescape(final @NotNull String text) {
        final var builder = new StringBuilder();
        var isEscape = false;
        for (var i = 0; i < text.length(); i++) {
            final var c = text.charAt(i);
            if (isEscape) {
                builder.append(switch (c) {
                    case '"' -> "\"";
                    case '\'' -> "'";
                    case 'n' -> "\n";
                    case 't' -> "\t";
                    case 'b' -> "\b";
                    case 'r' -> "\r";
                    case '0' -> "\0";
                    case '\\' -> "\\";
                    case '$' -> "$";
                    default -> throw new IllegalStateException("Illegal escape sequence");
                });
                isEscape = false;
                continue;
            }
            if (c == '\\') {
                isEscape = true;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static @NotNull Object parseIntWithPrefix(final @NotNull String value,
                                                     final @NotNull BiFunction<String, Integer, Object> parseFunction) {
        if (value.startsWith("0x") || value.startsWith("0X")) {
            return parseFunction.apply(value.substring(2), 16);
        }
        else if (value.startsWith("0b") || value.startsWith("0B")) {
            return parseFunction.apply(value.substring(2), 2);
        }
        else if (value.startsWith("0o") || value.startsWith("0O")) {
            return parseFunction.apply(value.substring(2), 8);
        }
        return parseFunction.apply(value, 10);
    }

    public static @NotNull List<Expr> parseIntWithPrefix(final @NotNull IntLiteralContext ctx,
                                                         final @NotNull BiFunction<String, Integer, Object> parseFunction) {
        final var value = ctx.LITERAL_INT().getText();
        return List.of(ConstExpr.of(parseIntWithPrefix(value, parseFunction), TokenRange.fromContext(ctx)));
    }

    public static @NotNull Pair<@Nullable Expr, Expr> parseTypeArgument(final @NotNull TypeArgumentContext ctx) throws ParserException {
        // TODO: implement support for interpolation in generic bounds
        final var namedCtx = ctx.namedTypeArgument();
        if (namedCtx != null) {
            return new Pair<>(ExprParser.parse(namedCtx.exprOrName()),
                ConstExpr.of(TypeParser.parse(namedCtx.type()), TokenRange.fromContext(namedCtx.type())));
        }
        return new Pair<>(null, ConstExpr.of(TypeParser.parse(ctx.type()), TokenRange.fromContext(ctx.type())));
    }

    public static @NotNull Pair<@Nullable Expr, Expr> parseArgument(final @NotNull ArgumentContext ctx) throws ParserException {
        final var namedCtx = ctx.namedArgument();
        if (namedCtx != null) {
            return new Pair<>(ExprParser.parse(namedCtx.exprOrName()), ExprParser.parse(namedCtx.expr()));
        }
        return new Pair<>(null, ExprParser.parse(ctx.expr()));
    }

    public static @NotNull List<Pair<@Nullable Expr, Expr>> parseTypeArguments(final @NotNull List<TypeArgumentContext> args) { // @formatter:off
        return args.stream()
            .map(ExceptionUtils.unsafeFunction(ParserUtils::parseTypeArgument))
            .toList();
    } // @formatter:on

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
            ? ConstExpr.of(specialName.getText(), TokenRange.fromContext(specialName))
            : ExprParser.parse(ctx.exprOrName());
        // @formatter:on
    }

    public static @NotNull Expr parseSignatureOwner(final @NotNull SignatureOwnerContext ctx) throws ParserException {
        final var expr = ctx.wrappedExpr();
        // @formatter:off
        return expr != null
            ? ExprParser.parse(expr)
            : ConstExpr.of(ExceptionUtils.rethrowUnchecked(() -> TypeParser.parse(ctx.classType())), TokenRange.fromContext(ctx.classType()));
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

    public static @NotNull List<Pair<Expr, Expr>> parseParameters(final @NotNull List<ParameterContext> params) throws ParserException {
        final var paramPairs = new ArrayList<Pair<Expr, Expr>>();
        for (final var param : params) {
            final var name = ExprParser.parse(param.exprOrName());
            final var type = ExprParser.parse(param.exprOrType());
            paramPairs.add(new Pair<>(name, type));
        }
        return paramPairs;
    }

    public static @NotNull List<Pair<Expr, Expr>> parseTypeParameters(final @NotNull List<TypeParameterContext> params) throws ParserException {
        final var paramPairs = new ArrayList<Pair<Expr, Expr>>();
        for (final var param : params) {
            final var name = ExprParser.parse(param.exprOrName());
            final var type = ExprParser.parse(param.exprOrType());
            paramPairs.add(new Pair<>(name, type));
        }
        return paramPairs;
    }
}
