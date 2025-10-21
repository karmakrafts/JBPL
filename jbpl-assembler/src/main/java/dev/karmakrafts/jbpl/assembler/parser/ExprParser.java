package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.model.type.PreproClassType;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import dev.karmakrafts.jbpl.assembler.util.ParserUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExprParser extends JBPLParserBaseVisitor<List<Expr>> {
    public static final ExprParser INSTANCE = new ExprParser();

    private ExprParser() {
    }

    public static @NotNull Expr parse(final @NotNull ParserRuleContext ctx) {
        final var expr = ctx.accept(INSTANCE).stream().findFirst().orElseThrow();
        expr.setTokenRange(TokenRange.fromContext(ctx));
        return expr;
    }

    @Override
    protected @NotNull List<Expr> defaultResult() {
        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<Expr> aggregateResult(final @NotNull List<Expr> aggregate,
                                                  final @NotNull List<Expr> nextResult) {
        aggregate.addAll(nextResult);
        return aggregate;
    }

    @Override
    public @NotNull List<Expr> visitTypeOfExpr(final @NotNull TypeOfExprContext ctx) {
        return List.of(new TypeOfExpr(parse(ctx.expr())));
    }

    @Override
    public @NotNull List<Expr> visitOpcodeOfExpr(final @NotNull OpcodeOfExprContext ctx) {
        final var opcodeNode = ctx.opcode();
        if (opcodeNode != null) {
            final var opcode = ParserUtils.parseOpcode(ctx.opcode()).orElseThrow();
            return List.of(new OpcodeOfExpr(LiteralExpr.of(opcode)));
        }
        return List.of(new OpcodeOfExpr(ExprParser.parse(ctx.expr())));
    }

    @Override
    public @NotNull List<Expr> visitPreproClassInstantiation(final @NotNull PreproClassInstantiationContext ctx) {
        final var type = new PreproClassType(ctx.IDENT().getText());
        final var instantiation = new PreproClassExpr(type);
        // @formatter:off
        instantiation.addArguments(ctx.expr().stream()
            .map(ExprParser::parse)
            .toList());
        // @formatter:on
        return List.of(instantiation);
    }

    private @NotNull List<Expr> parseBinaryExpr(final @NotNull ExprContext ctx, final @NotNull BinaryExpr.Op op) {
        final var expressions = ctx.expr();
        final var lhs = parse(expressions.get(0));
        final var rhs = parse(expressions.get(1));
        return List.of(new BinaryExpr(lhs, rhs, op));
    }

    private @NotNull List<Expr> parseUnaryExpr(final @NotNull ExprContext ctx, final @NotNull UnaryExpr.Op op) {
        final var expr = parse(ctx.expr().stream().findFirst().orElseThrow());
        return List.of(new UnaryExpr(expr, op));
    }

    private @NotNull List<Expr> parseBinaryExprWithUnaryVariant(final @NotNull ExprContext ctx,
                                                                final @NotNull BinaryExpr.Op binaryOp,
                                                                final @NotNull UnaryExpr.Op unaryOp) {
        final var expressions = ctx.expr();
        if (expressions.size() == 1) {
            return parseUnaryExpr(expressions.get(0), unaryOp);
        }
        return parseBinaryExpr(ctx, binaryOp);
    }

    @Override
    public @NotNull List<Expr> visitExpr(final @NotNull ExprContext ctx) {
        final var type = ctx.type();
        if (ctx.DOT() != null) {
            // This is a member reference which takes precedence over top level refs
            final var receiver = parse(ctx.expr().stream().findFirst().orElseThrow());
            final var call = ctx.macroCall();
            if (call != null) {
                return parseMacroCall(call, receiver);
            }
            return parseReference(ctx.reference(), receiver);
        }
        else if (ctx.KW_IS() != null) {
            final var expr = parse(ctx.expr().stream().findFirst().orElseThrow());
            return List.of(new IsExpr(expr, TypeParser.parse(type)));
        } // @formatter:off
        else if (type != null)              return List.of(LiteralExpr.of(TypeParser.parse(type)));
        else if (ctx.EQEQ() != null)        return parseBinaryExpr(ctx, BinaryExpr.Op.EQ);
        else if (ctx.NEQ() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.NE);
        else if (ctx.L_ABRACKET() != null)  return parseBinaryExpr(ctx, BinaryExpr.Op.LT);
        else if (ctx.LEQ() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.LE);
        else if (ctx.R_ABRACKET() != null)  return parseBinaryExpr(ctx, BinaryExpr.Op.GT);
        else if (ctx.GEQ() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.GE);
        else if (ctx.PLUS() != null)        return parseBinaryExprWithUnaryVariant(ctx, BinaryExpr.Op.ADD, UnaryExpr.Op.PLUS);
        else if (ctx.MINUS() != null)       return parseBinaryExprWithUnaryVariant(ctx, BinaryExpr.Op.SUB, UnaryExpr.Op.MINUS);
        else if (ctx.ASTERISK() != null)    return parseBinaryExpr(ctx, BinaryExpr.Op.MUL);
        else if (ctx.SLASH() != null)       return parseBinaryExpr(ctx, BinaryExpr.Op.DIV);
        else if (ctx.REM() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.REM);
        else if (ctx.LSH() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.LSH);
        else if (ctx.RSH() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.RSH);
        else if (ctx.CARET() != null)       return parseBinaryExpr(ctx, BinaryExpr.Op.XOR);
        else if (ctx.AMPAMP() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.SC_AND);
        else if (ctx.AMP() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.AND);
        else if (ctx.PIPEPIPE() != null)    return parseBinaryExpr(ctx, BinaryExpr.Op.SC_OR);
        else if (ctx.PIPE() != null)        return parseBinaryExpr(ctx, BinaryExpr.Op.OR);
        else if (ctx.TILDE() != null)       return parseUnaryExpr(ctx, UnaryExpr.Op.INVERSE);
        else if (ctx.EXCL() != null)        return parseUnaryExpr(ctx, UnaryExpr.Op.NOT);
        // @formatter:on
        return super.visitExpr(ctx);
    }

    private @NotNull List<Expr> parseReference(final @NotNull ReferenceContext ctx, final @NotNull Expr receiver) {
        final var name = ctx.IDENT().getText();
        return List.of(new ReferenceExpr(receiver, name));
    }

    @Override
    public @NotNull List<Expr> visitReference(final @NotNull ReferenceContext ctx) {
        // TODO: This only handles top level references
        return parseReference(ctx, LiteralExpr.unit());
    }

    private @NotNull List<Expr> parseMacroCall(final @NotNull MacroCallContext ctx, final @NotNull Expr receiver) {
        final var name = ctx.IDENT().getText();
        final var call = new MacroCallExpr(receiver, name);
        // @formatter:off
        call.addArguments(ctx.expr().stream()
            .map(ExprParser::parse)
            .toList());
        // @formatter:on
        return List.of(call);
    }

    @Override
    public @NotNull List<Expr> visitMacroCall(final @NotNull MacroCallContext ctx) {
        // TODO: This only handles top level references
        return parseMacroCall(ctx, LiteralExpr.unit());
    }

    @Override
    public @NotNull List<Expr> visitLiteral(final @NotNull LiteralContext ctx) {
        final var literalChar = ctx.LITERAL_CHAR();
        if (literalChar != null) {
            var value = literalChar.getText();
            value = value.substring(1, value.length() - 1); // Strip the single quotes
            if (value.contains("\\")) { // Handle escaped characters
                value = switch (value) {
                    case "\\n" -> "\n";
                    case "\\t" -> "\t";
                    case "\\r" -> "\r";
                    case "\\0" -> "\0";
                    default -> throw new IllegalStateException("Unsupported character escape sequence");
                };
            }
            return List.of(LiteralExpr.of(value.charAt(0)));
        }
        return super.visitLiteral(ctx);
    }

    private @NotNull Expr parseStringSegment(final @NotNull StringSegmentContext ctx) {
        final var text = ctx.M_CONST_STR_TEXT();
        if (text != null) {
            return LiteralExpr.of(text.getText());
        }
        return parse(ctx.expr());
    }

    private @NotNull LiteralExpr mergeStringLiterals(final @NotNull ArrayDeque<LiteralExpr> queue) {
        final var builder = new StringBuilder();
        while (!queue.isEmpty()) {
            builder.append(queue.pop().value);
        }
        return LiteralExpr.of(builder.toString());
    }

    @Override
    public @NotNull List<Expr> visitStringLiteral(final @NotNull StringLiteralContext ctx) {
        // @formatter:off
        final var segments = ctx.stringSegment().stream()
            .map(this::parseStringSegment)
            .toList();
        // @formatter:on
        // Handle empty strings
        if (segments.isEmpty()) {
            return List.of(LiteralExpr.of(""));
        }
        // Merge all adjacent text segments
        final var mergedSegments = new ArrayList<Expr>();
        final var mergeQueue = new ArrayDeque<LiteralExpr>();
        for (final var segment : segments) {
            if (segment instanceof LiteralExpr literalExpr) {
                mergeQueue.push(literalExpr);
                continue;
            }
            if (!mergeQueue.isEmpty()) {
                mergedSegments.add(mergeStringLiterals(mergeQueue));
            }
            mergedSegments.add(segment);
        }
        // If there's any residual segments to be merged, merge them
        if (!mergeQueue.isEmpty()) {
            mergedSegments.add(mergeStringLiterals(mergeQueue));
        }
        // Return the literal itself if we only have a single text element
        if (mergedSegments.size() == 1 && mergedSegments.get(0) instanceof LiteralExpr literalExpr) {
            return List.of(literalExpr);
        }
        final var lerpExpr = new StringLerpExpr();
        lerpExpr.addExpressions(mergedSegments);
        return List.of(lerpExpr);
    }

    @Override
    public @NotNull List<Expr> visitBoolLiteral(final @NotNull BoolLiteralContext ctx) {
        return List.of(LiteralExpr.of(ctx.KW_TRUE() != null));
    }

    @Override
    public @NotNull List<Expr> visitIntLiteral(final @NotNull IntLiteralContext ctx) {
        final var value = ctx.LITERAL_INT().getText();
        // @formatter:off
        if (ctx.KW_I64() != null)       return List.of(LiteralExpr.of(Long.parseLong(value)));
        else if (ctx.KW_I16() != null)  return List.of(LiteralExpr.of(Short.parseShort(value)));
        else if (ctx.KW_I8() != null)   return List.of(LiteralExpr.of(Byte.parseByte(value)));
        // @formatter:on
        // Without suffix or with i32, we assume int as the default case
        return List.of(LiteralExpr.of(Integer.parseInt(value)));
    }

    @Override
    public @NotNull List<Expr> visitFloatLiteral(final @NotNull FloatLiteralContext ctx) {
        var value = ctx.LITERAL_FLOAT_LIKE();
        if (value == null) {
            value = ctx.LITERAL_INT();
        }
        if (ctx.KW_F32() != null) {
            return List.of(LiteralExpr.of(Float.parseFloat(value.getText())));
        }
        return List.of(LiteralExpr.of(Double.parseDouble(value.getText())));
    }

    private @NotNull Pair<Expr, Expr> parseFunctionSignatureParameter(final @NotNull FunctionSignatureParameterContext ctx) {
        final var name = Optional.ofNullable(ctx.refOrName()).map(ParserUtils::parseRefOrName).orElse(null);
        final var type = ParserUtils.parseRefOrType(ctx.refOrType());
        return new Pair<>(name, type);
    }

    @Override
    public @NotNull List<Expr> visitFieldSignature(final @NotNull FieldSignatureContext ctx) {
        final var owner = ParserUtils.parseSignatureOwner(ctx.signatureOwner());
        final var name = ParserUtils.parseRefOrName(ctx.refOrName());
        final var type = ParserUtils.parseRefOrType(ctx.refOrType());
        return List.of(new FieldSignatureExpr(owner, name, type));
    }

    @Override
    public @NotNull List<Expr> visitFunctionSignature(final @NotNull FunctionSignatureContext ctx) {
        final var owner = ParserUtils.parseSignatureOwner(ctx.signatureOwner());
        final var name = ParserUtils.parseFunctionName(ctx.functionName());
        final var returnType = ParserUtils.parseRefOrType(ctx.refOrType());
        final var signature = new FunctionSignatureExpr(owner, name, returnType);
        // @formatter:off
        signature.addExpressions(ctx.functionSignatureParameter().stream()
            .map(this::parseFunctionSignatureParameter)
            .map(Pair::right) // TODO: For these expressions, we discard names for now
            .toList());
        // @formatter:on
        return List.of(signature);
    }

    @Override
    public @NotNull List<Expr> visitSelectorReference(final @NotNull SelectorReferenceContext ctx) {
        return List.of(new SelectorReferenceExpr(ctx.IDENT().getText()));
    }
}
