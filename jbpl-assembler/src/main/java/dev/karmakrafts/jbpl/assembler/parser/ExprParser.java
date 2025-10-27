package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.expr.IfExpr.ElseBranch;
import dev.karmakrafts.jbpl.assembler.model.expr.IfExpr.ElseIfBranch;
import dev.karmakrafts.jbpl.assembler.model.type.PreproClassType;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
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

    private static @NotNull List<Element> parseIfBody(final @NotNull IfBodyContext ctx) { // @formatter:off
        return ctx.bodyElement().stream()
            .map(ElementParser::parse)
            .toList();
    } // @formatter:on

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
    public @NotNull List<Expr> visitDefaultExpr(final @NotNull DefaultExprContext ctx) {
        final var type = ParserUtils.parseRefOrType(ctx.refOrType());
        return List.of(new DefaultExpr(type));
    }

    @Override
    public @NotNull List<Expr> visitArrayExpr(final @NotNull ArrayExprContext ctx) {
        // @formatter:off
        final var array = ctx.refOrType() != null
            ? new ArrayExpr(ParserUtils.parseRefOrType(ctx.refOrType()))
            : new ArrayExpr();
        // @formatter:on
        array.addValues(ctx.expr().stream().map(ExprParser::parse).toList());
        return List.of(array);
    }

    @Override
    public @NotNull List<Expr> visitTypeOfExpr(final @NotNull TypeOfExprContext ctx) {
        // @formatter:off
        final var type = ctx.type() != null
            ? LiteralExpr.of(TypeParser.parse(ctx.type()))
            : ExprParser.parse(ctx.expr());
        // @formatter:on
        return List.of(new TypeOfExpr(type));
    }

    @Override
    public @NotNull List<Expr> visitOpcodeOfExpr(final @NotNull OpcodeOfExprContext ctx) {
        final var opcodeNode = ctx.opcode();
        if (opcodeNode != null) {
            final var opcode = ParserUtils.parseOpcode(opcodeNode).orElseThrow();
            return List.of(new OpcodeOfExpr(LiteralExpr.of(opcode, TokenRange.fromContext(opcodeNode))));
        }
        return List.of(new OpcodeOfExpr(ExprParser.parse(ctx.expr())));
    }

    @Override
    public @NotNull List<Expr> visitPreproClassInstantiation(final @NotNull PreproClassInstantiationContext ctx) {
        final var type = new PreproClassType(ctx.IDENT().getText());
        final var instantiation = new PreproClassExpr(type);
        instantiation.addArguments(ParserUtils.parseArguments(ctx.argument()));
        return List.of(instantiation);
    }

    @Override
    public List<Expr> visitIfExpr(final @NotNull IfExprContext ctx) {
        final var condition = ExprParser.parse(ctx.expr());
        final var expr = new IfExpr(condition);
        // @formatter:off
        expr.addElements(ctx.bodyElement() != null
            ? List.of(ElementParser.parse(ctx.bodyElement()))
            : parseIfBody(ctx.ifBody()));
        // @formatter:on
        final var elseIfBranches = ctx.elseIfBranch();
        for (final var branchNode : elseIfBranches) {
            final var branchCondition = ExprParser.parse(branchNode.expr());
            final var branch = new ElseIfBranch(branchCondition);
            branch.setTokenRange(TokenRange.fromContext(branchNode));
            // @formatter:off
            branch.addElements(branchNode.bodyElement() != null
                ? List.of(ElementParser.parse(branchNode.bodyElement()))
                : parseIfBody(branchNode.ifBody()));
            // @formatter:on
            expr.addElseIfBranch(branch);
        }
        final var elseBranchNode = ctx.elseBranch();
        if (elseBranchNode != null) {
            final var branch = new ElseBranch();
            branch.setTokenRange(TokenRange.fromContext(elseBranchNode));
            // @formatter:off
            branch.addElements(elseBranchNode.bodyElement() != null
                ? List.of(ElementParser.parse(elseBranchNode.bodyElement()))
                : parseIfBody(elseBranchNode.ifBody()));
            // @formatter:on
            expr.setElseBranch(branch);
        }
        return List.of(expr);
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
            return parseUnaryExpr(ctx, unaryOp);
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
        else if (ctx.L_SQBRACKET() != null) {
            // We know this is a subscript operator
            final var reference = parse(ctx.expr(0));
            final var index = parse(ctx.expr(1));
            return List.of(new ArrayAccessExpr(reference, index));
        }
        else if (ctx.KW_IS() != null) {
            return List.of(new IsExpr(parse(ctx.expr(0)), TypeParser.parse(type)));
        }
        else if (ctx.KW_AS() != null) {
            final var value = parse(ctx.expr(0));
            final var targetType = LiteralExpr.of(TypeParser.parse(ctx.type())); // TODO: allow refs here
            return List.of(new AsExpr(value, targetType));
        } // @formatter:off
        else if (type != null)              return List.of(LiteralExpr.of(TypeParser.parse(type), TokenRange.fromContext(type)));
        else if (ctx.EQEQ() != null)        return parseBinaryExpr(ctx, BinaryExpr.Op.EQ);
        else if (ctx.NEQ() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.NE);
        else if (ctx.L_ABRACKET() != null)  return parseBinaryExpr(ctx, BinaryExpr.Op.LT);
        else if (ctx.LEQ() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.LE);
        else if (ctx.R_ABRACKET() != null)  return parseBinaryExpr(ctx, BinaryExpr.Op.GT);
        else if (ctx.GEQ() != null)         return parseBinaryExpr(ctx, BinaryExpr.Op.GE);
        else if (ctx.SPACESHIP() != null)   return parseBinaryExpr(ctx, BinaryExpr.Op.CMP);
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
        return parseReference(ctx, LiteralExpr.unit());
    }

    private @NotNull List<Expr> parseMacroCall(final @NotNull MacroCallContext ctx, final @NotNull Expr receiver) {
        final var name = ctx.IDENT().getText();
        final var call = new MacroCallExpr(receiver, name);
        call.addArguments(ParserUtils.parseArguments(ctx.argument()));
        return List.of(call);
    }

    @Override
    public @NotNull List<Expr> visitMacroCall(final @NotNull MacroCallContext ctx) {
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
            return List.of(LiteralExpr.of(value.charAt(0), TokenRange.fromTerminalNode(literalChar)));
        }
        return super.visitLiteral(ctx);
    }

    private @NotNull Expr parseStringSegment(final @NotNull StringSegmentContext ctx) {
        final var text = ctx.M_CONST_STR_TEXT();
        if (text != null) {
            return LiteralExpr.of(text.getText(), TokenRange.fromTerminalNode(text));
        }
        return parse(ctx.expr());
    }

    private @NotNull LiteralExpr mergeStringLiterals(final @NotNull ArrayDeque<LiteralExpr> queue) {
        final var builder = new StringBuilder();
        final var ranges = new ArrayList<TokenRange>();
        while (!queue.isEmpty()) {
            final var segment = queue.pop();
            ranges.add(segment.getTokenRange());
            builder.append(segment.value);
        }
        return LiteralExpr.of(builder.toString(), TokenRange.union(ranges));
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
            return List.of(LiteralExpr.of("", TokenRange.fromContext(ctx)));
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
        lerpExpr.setTokenRange(TokenRange.union(mergedSegments.stream().map(Expr::getTokenRange).toList()));
        lerpExpr.addExpressions(mergedSegments);
        return List.of(lerpExpr);
    }

    @Override
    public @NotNull List<Expr> visitBoolLiteral(final @NotNull BoolLiteralContext ctx) {
        return List.of(LiteralExpr.of(ctx.KW_TRUE() != null, TokenRange.fromContext(ctx)));
    }

    @Override
    public @NotNull List<Expr> visitIntLiteral(final @NotNull IntLiteralContext ctx) {
        final var value = ctx.LITERAL_INT().getText();
        final var tokenRange = TokenRange.fromContext(ctx);
        // @formatter:off
        if (ctx.KW_I64() != null)       return List.of(LiteralExpr.of(Long.parseLong(value), tokenRange));
        else if (ctx.KW_I16() != null)  return List.of(LiteralExpr.of(Short.parseShort(value), tokenRange));
        else if (ctx.KW_I8() != null)   return List.of(LiteralExpr.of(Byte.parseByte(value), tokenRange));
        // @formatter:on
        // Without suffix or with i32, we assume int as the default case
        return List.of(LiteralExpr.of(Integer.parseInt(value), tokenRange));
    }

    @Override
    public @NotNull List<Expr> visitFloatLiteral(final @NotNull FloatLiteralContext ctx) {
        var value = ctx.LITERAL_FLOAT_LIKE();
        if (value == null) {
            value = ctx.LITERAL_INT();
        }
        final var tokenRange = TokenRange.fromContext(ctx);
        if (ctx.KW_F32() != null) {
            return List.of(LiteralExpr.of(Float.parseFloat(value.getText()), tokenRange));
        }
        return List.of(LiteralExpr.of(Double.parseDouble(value.getText()), tokenRange));
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
