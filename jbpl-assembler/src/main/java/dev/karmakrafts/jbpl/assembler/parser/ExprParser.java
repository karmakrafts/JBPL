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

package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.expr.IfExpr.ElseBranch;
import dev.karmakrafts.jbpl.assembler.model.expr.IfExpr.ElseIfBranch;
import dev.karmakrafts.jbpl.assembler.model.type.PreproClassType;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import dev.karmakrafts.jbpl.assembler.util.ParserUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExprParser extends JBPLParserBaseVisitor<List<Expr>> {
    public static final ExprParser INSTANCE = new ExprParser();

    private ExprParser() {
    }

    public static @NotNull Expr parse(final @NotNull ParserRuleContext ctx) throws ParserException {
        // @formatter:off
        final var expr = ctx.accept(INSTANCE).stream()
            .findFirst()
            .orElseThrow(() -> new ParserException("Could not parse expression", null));
        // @formatter:on
        expr.setTokenRange(TokenRange.fromContext(ctx));
        return expr;
    }

    private static @NotNull List<Element> parseIfBody(final @NotNull IfBodyContext ctx) { // @formatter:off
        return ctx.bodyElement().stream()
            .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
            .toList();
    } // @formatter:on

    private static @NotNull WhenExpr.Branch parseWhenBranch(final @NotNull WhenBranchContext ctx) {
        final var condition = ExceptionUtils.rethrowUnchecked(() -> parse(ctx.expr()));
        final var body = ctx.whenBranchBody();
        if (body.L_BRACE() != null) { // This is a scoped body
            final var branch = new WhenExpr.ScopedBranch(condition);
            // @formatter:off
            branch.addElements(body.bodyElement().stream()
                .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
                .toList());
            // @formatter:on
            return branch;
        }
        final var branch = new WhenExpr.ScopelessBranch(condition);
        branch.addElement(ExceptionUtils.rethrowUnchecked(() -> ElementParser.parse(body.bodyElement(0))));
        return branch;
    }

    private static @NotNull WhenExpr.Branch parseDefaultWhenBranch(final @NotNull DefaultWhenBranchContext ctx) {
        final var body = ctx.whenBranchBody();
        if (body.L_BRACE() != null) { // This is a scoped body
            final var branch = new WhenExpr.ScopedDefaultBranch();
            // @formatter:off
            branch.addElements(body.bodyElement().stream()
                .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
                .toList());
            // @formatter:on
            return branch;
        }
        final var branch = new WhenExpr.ScopelessDefaultBranch();
        branch.addElement(ExceptionUtils.rethrowUnchecked(() -> ElementParser.parse(body.bodyElement(0))));
        return branch;
    }

    private static @NotNull List<Expr> parseBinaryExpr(final @NotNull ExprContext ctx,
                                                       final @NotNull BinaryExpr.Op op) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var expressions = ctx.expr();
            final var lhs = parse(expressions.get(0));
            final var rhs = parse(expressions.get(1));
            return List.of(new BinaryExpr(lhs, rhs, op));
        });
    }

    private static @NotNull List<Expr> parseUnaryExpr(final @NotNull ExprContext ctx, final @NotNull UnaryExpr.Op op) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var expr = parse(ctx.expr().stream().findFirst().orElseThrow());
            return List.of(new UnaryExpr(expr, op));
        });
    }

    private static @NotNull List<Expr> parsePrePostUnaryExpr(final @NotNull ExprContext ctx,
                                                             final @NotNull UnaryExpr.Op preOp,
                                                             final @NotNull UnaryExpr.Op postOp) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var firstChild = ctx.getChild(0);
            final var op = firstChild instanceof TerminalNode ? preOp : postOp;
            final var expr = parse(ctx.expr().stream().findFirst().orElseThrow());
            return List.of(new UnaryExpr(expr, op));
        });
    }

    private static @NotNull List<Expr> parseBinaryExprWithUnaryVariant(final @NotNull ExprContext ctx,
                                                                       final @NotNull BinaryExpr.Op binaryOp,
                                                                       final @NotNull UnaryExpr.Op unaryOp) {
        final var expressions = ctx.expr();
        if (expressions.size() == 1) {
            return parseUnaryExpr(ctx, unaryOp);
        }
        return parseBinaryExpr(ctx, binaryOp);
    }

    private static @NotNull List<Expr> parseRangeExpr(final @NotNull ExprContext ctx,
                                                      final boolean isInclusive) throws ParserException {
        final var start = parse(ctx.expr(0));
        final var end = parse(ctx.expr(1));
        return List.of(new RangeExpr(start, end, isInclusive));
    }

    private static @NotNull List<Expr> parseReference(final @NotNull ReferenceContext ctx,
                                                      final @NotNull Expr receiver) {
        final var ref = new ReferenceExpr(ConstExpr.of(ctx.getText(), TokenRange.fromContext(ctx)));
        ref.setReceiver(receiver);
        return List.of(ref);
    }

    private static @NotNull List<Expr> parseMacroCall(final @NotNull MacroCallContext ctx,
                                                      final @NotNull Expr receiver) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = parse(ctx.exprOrName());
            final var call = new MacroCallExpr(name);
            call.setReceiver(receiver);
            call.addArguments(ParserUtils.parseArguments(ctx.argument()));
            return List.of(call);
        });
    }

    private static @NotNull Expr parseStringSegment(final @NotNull StringSegmentContext ctx) {
        final var text = ctx.M_CONST_STR_TEXT();
        if (text != null) {
            return ConstExpr.of(text.getText(), TokenRange.fromTerminalNode(text));
        }
        return ExceptionUtils.rethrowUnchecked(() -> parse(ctx.expr()));
    }

    private static @NotNull ConstExpr mergeStringLiterals(final @NotNull ArrayDeque<ConstExpr> queue) {
        final var builder = new StringBuilder();
        final var ranges = new ArrayList<TokenRange>();
        while (!queue.isEmpty()) {
            final var segment = queue.pop();
            ranges.add(segment.getTokenRange());
            builder.append(segment.getConstValue());
        }
        return ConstExpr.of(builder.toString(), TokenRange.union(ranges));
    }

    private static @NotNull Pair<Expr, Expr> parseFunctionSignatureParameter(final @NotNull FunctionSignatureParameterContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = Optional.ofNullable(ctx.exprOrName()).map(ExceptionUtils.unsafeFunction(ExprParser::parse)).orElse(
                null);
            final var type = parse(ctx.exprOrType());
            return new Pair<>(name, type);
        });
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
    public @NotNull List<Expr> visitDefaultExpr(final @NotNull DefaultExprContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(new DefaultExpr(parse(ctx.expr()))));
    }

    @Override
    public @NotNull List<Expr> visitArrayExpr(final @NotNull ArrayExprContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            // @formatter:off
            final var array = ctx.exprOrType() != null
                ? new ArrayExpr(parse(ctx.exprOrType()))
                : new ArrayExpr();
            // @formatter:on
            array.addValues(ctx.expr().stream().map(ExceptionUtils.unsafeFunction(ExprParser::parse)).toList());
            return List.of(array);
        });
    }

    @Override
    public @NotNull List<Expr> visitTypeOfExpr(final @NotNull TypeOfExprContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(new TypeOfExpr(parse(ctx.expr()))));
    }

    @Override
    public @NotNull List<Expr> visitTypeLiteral(final @NotNull TypeLiteralContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(ConstExpr.of(TypeParser.parse(ctx.type()),
            TokenRange.fromContext(ctx))));
    }

    @Override
    public @NotNull List<Expr> visitWhenExpr(final @NotNull WhenExprContext ctx) {
        final var value = ExceptionUtils.rethrowUnchecked(() -> parse(ctx.expr()));
        final var whenExpr = new WhenExpr(value);
        whenExpr.addBranches(ctx.whenBranch().stream().map(ExprParser::parseWhenBranch).toList());
        final var defaultBranch = ctx.defaultWhenBranch();
        if (defaultBranch != null) {
            whenExpr.addBranch(parseDefaultWhenBranch(defaultBranch));
        }
        return List.of(whenExpr);
    }

    @Override
    public @NotNull List<Expr> visitOpcodeOfExpr(final @NotNull OpcodeOfExprContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(new OpcodeOfExpr(parse(ctx.expr()))));
    }

    @Override
    public @NotNull List<Expr> visitOpcodeLiteral(final @NotNull OpcodeLiteralContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(ConstExpr.of(ParserUtils.parseOpcode(ctx.opcode()),
            TokenRange.fromContext(ctx))));
    }

    @Override
    public @NotNull List<Expr> visitPreproClassInstantiation(final @NotNull PreproClassInstantiationContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var type = new PreproClassType(ctx.IDENT().getText());
            final var instantiation = new PreproClassExpr(type);
            instantiation.addArguments(ParserUtils.parseArguments(ctx.argument()));
            return List.of(instantiation);
        });
    }

    @Override
    public List<Expr> visitIfExpr(final @NotNull IfExprContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var condition = parse(ctx.expr());
            final var expr = new IfExpr(condition);
            // @formatter:off
            expr.addElements(ctx.bodyElement() != null
                ? List.of(ElementParser.parse(ctx.bodyElement()))
                : parseIfBody(ctx.ifBody()));
            // @formatter:on
            final var elseIfBranches = ctx.elseIfBranch();
            for (final var branchNode : elseIfBranches) {
                final var branchCondition = parse(branchNode.expr());
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
        });
    }

    @Override
    public @NotNull List<Expr> visitExpr(final @NotNull ExprContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var wrappedExpr = ctx.wrappedExpr();
            if (ctx.DOT() != null) {
                // This is a member reference which takes precedence over top level refs
                // @formatter:off
                final var receiver = parse(ctx.expr().stream()
                    .findFirst()
                    .orElseThrow(() -> new ParserException("Could not parse reference receiver", null)));
                // @formatter:on
                final var call = ctx.macroCall();
                if (call != null) {
                    // TODO: implement interpolated macro calls
                    return parseMacroCall(call, receiver);
                }
                if (wrappedExpr != null) {
                    // This is an interpolated reference we need to unwrap/resolve at runtime
                    final var ref = new ReferenceExpr(parse(wrappedExpr));
                    ref.setReceiver(receiver);
                    return List.of(ref);
                }
                return parseReference(ctx.reference(), receiver);
            } // @formatter:off
            else if (wrappedExpr != null)           return List.of(new ReferenceExpr(parse(wrappedExpr))); // Top-level interpolated refs
            else if (ctx.L_SQBRACKET() != null)     return List.of(new ArrayAccessExpr(parse(ctx.expr(0)), parse(ctx.expr(1))));
            else if (ctx.KW_IN() != null)           return List.of(new InExpr(parse(ctx.expr(0)), parse(ctx.expr(1))));
            else if (ctx.KW_IS() != null)           return List.of(new IsExpr(parse(ctx.expr(0)), parse(ctx.exprOrType())));
            else if (ctx.KW_AS() != null)           return List.of(new AsExpr(parse(ctx.expr(0)), parse(ctx.exprOrType())));
            else if (ctx.DOTDOT() != null)          return parseRangeExpr(ctx, true);
            else if (ctx.EXCL_RANGE() != null)      return parseRangeExpr(ctx, false);
            else if (ctx.EQEQ() != null)            return parseBinaryExpr(ctx, BinaryExpr.Op.EQ);
            else if (ctx.NEQ() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.NE);
            else if (ctx.L_ABRACKET() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.LT);
            else if (ctx.LEQ() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.LE);
            else if (ctx.R_ABRACKET() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.GT);
            else if (ctx.GEQ() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.GE);
            else if (ctx.SPACESHIP() != null)       return parseBinaryExpr(ctx, BinaryExpr.Op.CMP);
            else if (ctx.PLUS() != null)            return parseBinaryExprWithUnaryVariant(ctx, BinaryExpr.Op.ADD, UnaryExpr.Op.PLUS);
            else if (ctx.MINUS() != null)           return parseBinaryExprWithUnaryVariant(ctx, BinaryExpr.Op.SUB, UnaryExpr.Op.MINUS);
            else if (ctx.ASTERISK() != null)        return parseBinaryExpr(ctx, BinaryExpr.Op.MUL);
            else if (ctx.SLASH() != null)           return parseBinaryExpr(ctx, BinaryExpr.Op.DIV);
            else if (ctx.REM() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.REM);
            else if (ctx.LSH() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.LSH);
            else if (ctx.RSH() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.RSH);
            else if (ctx.CARET() != null)           return parseBinaryExpr(ctx, BinaryExpr.Op.XOR);
            else if (ctx.AMPAMP() != null)          return parseBinaryExpr(ctx, BinaryExpr.Op.SC_AND);
            else if (ctx.AMP() != null)             return parseBinaryExpr(ctx, BinaryExpr.Op.AND);
            else if (ctx.PIPEPIPE() != null)        return parseBinaryExpr(ctx, BinaryExpr.Op.SC_OR);
            else if (ctx.PIPE() != null)            return parseBinaryExpr(ctx, BinaryExpr.Op.OR);
            else if (ctx.TILDE() != null)           return parseUnaryExpr(ctx, UnaryExpr.Op.INVERSE);
            else if (ctx.EXCL() != null)            return parseUnaryExpr(ctx, UnaryExpr.Op.NOT);
            else if (ctx.PLUS_ASSIGN() != null)     return parseBinaryExpr(ctx, BinaryExpr.Op.PLUS_ASSIGN);
            else if (ctx.MINUS_ASSIGN() != null)    return parseBinaryExpr(ctx, BinaryExpr.Op.MINUS_ASSIGN);
            else if (ctx.TIMES_ASSIGN() != null)    return parseBinaryExpr(ctx, BinaryExpr.Op.TIMES_ASSIGN);
            else if (ctx.DIV_ASSIGN() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.DIV_ASSIGN);
            else if (ctx.REM_ASSIGN() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.REM_ASSIGN);
            else if (ctx.LSH_ASSIGN() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.LSH_ASSIGN);
            else if (ctx.RSH_ASSIGN() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.RSH_ASSIGN);
            else if (ctx.URSH_ASSIGN() != null)     return parseBinaryExpr(ctx, BinaryExpr.Op.URSH_ASSIGN);
            else if (ctx.AND_ASSIGN() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.AND_ASSIGN);
            else if (ctx.OR_ASSIGN() != null)       return parseBinaryExpr(ctx, BinaryExpr.Op.OR_ASSIGN);
            else if (ctx.XOR_ASSIGN() != null)      return parseBinaryExpr(ctx, BinaryExpr.Op.XOR_ASSIGN);
            else if (ctx.EQ() != null)              return parseBinaryExpr(ctx, BinaryExpr.Op.ASSIGN);
            else if (ctx.INC() != null)             return parsePrePostUnaryExpr(ctx, UnaryExpr.Op.PRE_INC, UnaryExpr.Op.POST_INC);
            else if (ctx.DEC() != null)             return parsePrePostUnaryExpr(ctx, UnaryExpr.Op.PRE_DEC, UnaryExpr.Op.POST_DEC);
            // @formatter:on
            return super.visitExpr(ctx);
        });
    }

    @Override
    public @NotNull List<Expr> visitExprOrClassType(final @NotNull ExprOrClassTypeContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var type = ctx.classType();
            if (type != null) {
                return List.of(ConstExpr.of(TypeParser.parse(type), TokenRange.fromContext(ctx)));
            }
            return List.of(parse(ctx.wrappedExpr()));
        });
    }

    @Override
    public @NotNull List<Expr> visitExprOrName(final @NotNull ExprOrNameContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = ctx.nameSegment();
            if (name != null) {
                return List.of(ConstExpr.of(name.getText(), TokenRange.fromContext(ctx)));
            }
            return List.of(parse(ctx.wrappedExpr()));
        });
    }

    @Override
    public @NotNull List<Expr> visitExprOrType(final @NotNull ExprOrTypeContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var type = ctx.type();
            if (type != null) {
                return List.of(ConstExpr.of(TypeParser.parse(type), TokenRange.fromContext(ctx)));
            }
            return List.of(parse(ctx.wrappedExpr()));
        });
    }

    // Simply unwrap the expression
    @Override
    public @NotNull List<Expr> visitWrappedExpr(final @NotNull WrappedExprContext ctx) {
        return List.of(ExceptionUtils.rethrowUnchecked(() -> parse(ctx.expr())));
    }

    @Override
    public @NotNull List<Expr> visitSizeOfExpr(final @NotNull SizeOfExprContext ctx) {
        return List.of(new SizeOfExpr(ExceptionUtils.rethrowUnchecked(() -> parse(ctx.expr()))));
    }

    @Override
    public @NotNull List<Expr> visitReference(final @NotNull ReferenceContext ctx) {
        return parseReference(ctx, ConstExpr.unit());
    }

    @Override
    public @NotNull List<Expr> visitMacroCall(final @NotNull MacroCallContext ctx) {
        return parseMacroCall(ctx, ConstExpr.unit());
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
            return List.of(ConstExpr.of(value.charAt(0), TokenRange.fromContext(ctx)));
        }
        return super.visitLiteral(ctx);
    }

    @Override
    public @NotNull List<Expr> visitStringLiteral(final @NotNull StringLiteralContext ctx) {
        // @formatter:off
        final var segments = ctx.stringSegment().stream()
            .map(ExprParser::parseStringSegment)
            .toList();
        // @formatter:on
        // Handle empty strings
        if (segments.isEmpty()) {
            return List.of(ConstExpr.of("", TokenRange.fromContext(ctx)));
        }
        // Merge all adjacent text segments
        final var mergedSegments = new ArrayList<Expr>();
        final var mergeQueue = new ArrayDeque<ConstExpr>();
        for (final var segment : segments) {
            if (segment instanceof ConstExpr literalExpr) {
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
        if (mergedSegments.size() == 1 && mergedSegments.get(0) instanceof ConstExpr literalExpr) {
            return List.of(literalExpr);
        }
        final var lerpExpr = new StringLerpExpr();
        lerpExpr.setTokenRange(TokenRange.union(mergedSegments.stream().map(Expr::getTokenRange).toList()));
        lerpExpr.addExpressions(mergedSegments);
        return List.of(lerpExpr);
    }

    @Override
    public @NotNull List<Expr> visitInstructionLiteral(final @NotNull InstructionLiteralContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(ConstExpr.of(InstructionParser.parse(ctx.instruction()),
            TokenRange.fromContext(ctx))));
    }

    @Override
    public @NotNull List<Expr> visitBoolLiteral(final @NotNull BoolLiteralContext ctx) {
        return List.of(ConstExpr.of(ctx.KW_TRUE() != null, TokenRange.fromContext(ctx)));
    }

    @Override
    public @NotNull List<Expr> visitIntLiteral(final @NotNull IntLiteralContext ctx) {
        // @formatter:off
        if (ctx.KW_I64() != null)       return ParserUtils.parseIntWithPrefix(ctx, Long::parseLong);
        else if (ctx.KW_I16() != null)  return ParserUtils.parseIntWithPrefix(ctx, Short::parseShort);
        else if (ctx.KW_I8() != null)   return ParserUtils.parseIntWithPrefix(ctx, Byte::parseByte);
        // @formatter:on
        // Without suffix or with i32, we assume int as the default case
        return ParserUtils.parseIntWithPrefix(ctx, Integer::parseInt);
    }

    @Override
    public @NotNull List<Expr> visitFloatLiteral(final @NotNull FloatLiteralContext ctx) {
        var value = ctx.LITERAL_FLOAT_LIKE();
        if (value == null) {
            value = ctx.LITERAL_INT();
        }
        final var tokenRange = TokenRange.fromContext(ctx);
        if (ctx.KW_F32() != null) {
            return List.of(ConstExpr.of(Float.parseFloat(value.getText()), tokenRange));
        }
        return List.of(ConstExpr.of(Double.parseDouble(value.getText()), tokenRange));
    }

    @Override
    public @NotNull List<Expr> visitFieldSignature(final @NotNull FieldSignatureContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var owner = ParserUtils.parseSignatureOwner(ctx.signatureOwner());
            final var name = parse(ctx.exprOrName());
            final var type = parse(ctx.exprOrType());
            return List.of(new FieldSignatureExpr(owner, name, type));
        });
    }

    @Override
    public @NotNull List<Expr> visitFunctionSignature(final @NotNull FunctionSignatureContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var owner = ParserUtils.parseSignatureOwner(ctx.signatureOwner());
            final var name = ParserUtils.parseFunctionName(ctx.functionName());
            final var returnType = parse(ctx.exprOrType());
            final var signature = new FunctionSignatureExpr(owner, name, returnType);
            // @formatter:off
            signature.addExpressions(ctx.functionSignatureParameter().stream()
                .map(ExprParser::parseFunctionSignatureParameter)
                .map(Pair::right)
                .toList());
            // @formatter:on
            return List.of(signature);
        });
    }
}
