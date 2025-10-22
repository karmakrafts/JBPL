package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.model.statement.*;
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

    public static @NotNull Statement parse(final @NotNull ParserRuleContext ctx) {
        final var statement = ctx.accept(INSTANCE).stream().findFirst().orElseThrow();
        statement.setTokenRange(TokenRange.fromContext(ctx));
        return statement;
    }

    @Override
    public @NotNull List<Statement> visitExpr(final @NotNull ExprContext ctx) {
        return List.of(ExprParser.parse(ctx));
    }

    @Override
    public @NotNull List<Statement> visitInstruction(final @NotNull InstructionContext ctx) {
        return List.of(InstructionParser.parse(ctx));
    }

    @Override
    public @NotNull List<Statement> visitReturnStatement(final @NotNull ReturnStatementContext ctx) {
        final var valueNode = ctx.expr();
        final var value = valueNode != null ? ExprParser.parse(valueNode) : LiteralExpr.unit();
        return List.of(new ReturnStatement(value));
    }

    @Override
    public List<Statement> visitVersionStatement(final @NotNull VersionStatementContext ctx) {
        final var version = ExprParser.parse(ctx.expr());
        return List.of(new VersionStatement(version));
    }

    @Override
    public List<Statement> visitYeetStatement(final @NotNull YeetStatementContext ctx) {
        final var classType = ctx.classType();
        if (classType != null) {
            return List.of(new YeetStatement(LiteralExpr.of(TypeParser.parse(classType))));
        }
        final var fieldSignature = ctx.fieldSignature();
        if (fieldSignature != null) {
            return List.of(new YeetStatement(ExprParser.parse(fieldSignature)));
        }
        return List.of(new YeetStatement(ExprParser.parse(ctx.functionSignature())));
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
        final var name = ParserUtils.parseRefOrName(ctx.refOrName());
        return List.of(new LabelStatement(name));
    }

    @Override
    public @NotNull List<Statement> visitLocal(final @NotNull LocalContext ctx) {
        final var name = ParserUtils.parseRefOrName(ctx.refOrName());
        return List.of(new LocalStatement(name));
    }

    @Override
    public @NotNull List<Statement> visitDefine(final @NotNull DefineContext ctx) {
        final var name = ctx.IDENT().getText();
        final var type = TypeParser.parse(ctx.type());
        final var value = ExprParser.parse(ctx.expr());
        return List.of(new DefineStatement(name, type, value));
    }
}
