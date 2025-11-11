package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.*;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.ParserUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class StatementParser extends JBPLParserBaseVisitor<List<Statement>> {
    public static final StatementParser INSTANCE = new StatementParser();

    private StatementParser() {
    }

    public static @NotNull Statement parse(final @NotNull ParserRuleContext ctx) throws ParserException {
        // @formatter:off
        final var statement = ctx.accept(INSTANCE).stream()
            .findFirst()
            .orElseThrow(() -> new ParserException("Could not parse statement", null));
        // @formatter:on
        statement.setTokenRange(TokenRange.fromContext(ctx));
        return statement;
    }

    @Override
    public List<Statement> visitAssertStatement(final @NotNull AssertStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(new AssertStatement(ExprParser.parse(ctx.expr()))));
    }

    @Override
    public @NotNull List<Statement> visitExpr(final @NotNull ExprContext ctx) {
        return List.of(ExceptionUtils.rethrowUnchecked(() -> ExprParser.parse(ctx)));
    }

    @Override
    public @NotNull List<Statement> visitInstruction(final @NotNull InstructionContext ctx) {
        return List.of(ExceptionUtils.rethrowUnchecked(() -> InstructionParser.parse(ctx)));
    }

    @Override
    public @NotNull List<Statement> visitReturnStatement(final @NotNull ReturnStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var valueNode = ctx.expr();
            // @formatter:off
            final var value = valueNode != null
                ? ExprParser.parse(valueNode)
                : LiteralExpr.UNIT;
            // @formatter:on
            return List.of(new ReturnStatement(value));
        });
    }

    @Override
    public List<Statement> visitInfoStatement(final @NotNull InfoStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var statement = new InfoStatement();
            statement.setValue(ExprParser.parse(ctx.expr()));
            return List.of(statement);
        });
    }

    @Override
    public List<Statement> visitErrorStatement(ErrorStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var value = ExprParser.parse(ctx.expr());
            return List.of(new ErrorStatement(value));
        });
    }

    @Override
    public List<Statement> visitVersionStatement(final @NotNull VersionStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var version = ExprParser.parse(ctx.expr());
            return List.of(new VersionStatement(version));
        });
    }

    @Override
    public List<Statement> visitYeetStatement(final @NotNull YeetStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var classType = ctx.classType();
            if (classType != null) {
                return List.of(new YeetStatement(LiteralExpr.of(TypeParser.parse(classType),
                    TokenRange.fromContext(classType))));
            }
            final var fieldSignature = ctx.fieldSignature();
            if (fieldSignature != null) {
                return List.of(new YeetStatement(ExprParser.parse(fieldSignature)));
            }
            return List.of(new YeetStatement(ExprParser.parse(ctx.functionSignature())));
        });
    }

    @Override
    public @NotNull List<Statement> visitInclude(final @NotNull IncludeContext ctx) {
        // @formatter:off
        return List.of(new IncludeStatement(ctx.simpleStringLiteral().M_CONST_STR_TEXT()
            .stream()
            .map(TerminalNode::getText)
            .collect(Collectors.joining(""))));
        // @formatter:on
    }

    @Override
    public @NotNull List<Statement> visitLabel(final @NotNull LabelContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = ParserUtils.parseExprOrName(ctx.exprOrName());
            return List.of(new LabelStatement(name));
        });
    }

    @Override
    public @NotNull List<Statement> visitLocal(final @NotNull LocalContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = ParserUtils.parseExprOrName(ctx.exprOrName());
            // @formatter:off
            final var index = ctx.expr() != null
                ? ExprParser.parse(ctx.expr())
                : LiteralExpr.UNIT;
            // @formatter:on
            return List.of(new LocalStatement(name, index));
        });
    }

    @Override
    public @NotNull List<Statement> visitDefine(final @NotNull DefineContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = ParserUtils.parseExprOrName(ctx.exprOrName());
            final var type = ParserUtils.parseExprOrType(ctx.exprOrType());
            final var value = ExprParser.parse(ctx.expr());
            return List.of(new DefineStatement(name, type, value));
        });
    }

    @Override
    public List<Statement> visitForLoop(final @NotNull ForLoopContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var variableName = ParserUtils.parseExprOrName(ctx.exprOrName());
            final var value = ExprParser.parse(ctx.expr());
            final var statement = new ForStatement(variableName, value);
            // @formatter:off
            statement.addElements(ctx.bodyElement().stream()
                .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
                .toList());
            // @formatter:on
            return List.of(statement);
        });
    }
}
