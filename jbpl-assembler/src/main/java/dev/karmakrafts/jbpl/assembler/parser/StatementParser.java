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

import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.*;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
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
    public @NotNull List<Statement> visitTypeAliasStatement(final @NotNull TypeAliasStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var isPrivate = ctx.KW_PRIVATE() != null;
            final var name = ExprParser.parse(ctx.exprOrName());
            final var type = ExprParser.parse(ctx.exprOrType());
            return List.of(new TypeAliasStatement(name, type, isPrivate));
        });
    }

    @Override
    public @NotNull List<Statement> visitAssertStatement(final @NotNull AssertStatementContext ctx) {
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
                : ConstExpr.unit();
            // @formatter:on
            return List.of(new ReturnStatement(value));
        });
    }

    @Override
    public @NotNull List<Statement> visitVersionStatement(final @NotNull VersionStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> List.of(new VersionStatement(ExprParser.parse(ctx.expr()))));
    }

    @Override
    public @NotNull List<Statement> visitContinueStatement(final @NotNull ContinueStatementContext ctx) {
        return List.of(new ContinueStatement());
    }

    @Override
    public @NotNull List<Statement> visitBreakStatement(final @NotNull BreakStatementContext ctx) {
        return List.of(new BreakStatement());
    }

    @Override
    public List<Statement> visitYeetStatement(final @NotNull YeetStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var classType = ctx.classType();
            if (classType != null) {
                return List.of(new YeetStatement(ConstExpr.of(TypeParser.parse(classType),
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
            final var name = ExprParser.parse(ctx.exprOrName());
            return List.of(new LabelStatement(name));
        });
    }

    @Override
    public @NotNull List<Statement> visitLocal(final @NotNull LocalContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = ExprParser.parse(ctx.exprOrName());
            // @formatter:off
            final var index = ctx.expr() != null
                ? ExprParser.parse(ctx.expr())
                : ConstExpr.unit();
            // @formatter:on
            return List.of(new LocalStatement(name, index));
        });
    }

    @Override
    public @NotNull List<Statement> visitDefine(final @NotNull DefineContext ctx) {
        final var modifiers = ctx.defineModifier();
        final var isFinal = modifiers.stream().anyMatch(mod -> mod.KW_FINAL() != null);
        final var isPrivate = modifiers.stream().anyMatch(mod -> mod.KW_PRIVATE() != null);
        return ExceptionUtils.rethrowUnchecked(() -> List.of(new DefineStatement(ExprParser.parse(ctx.exprOrName()),
            ExprParser.parse(ctx.exprOrType()),
            ExprParser.parse(ctx.expr()),
            isFinal,
            isPrivate)));
    }

    @Override
    public List<Statement> visitForStatement(final @NotNull ForStatementContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var statement = new ForStatement(ExprParser.parse(ctx.exprOrName()), ExprParser.parse(ctx.expr()));
            // @formatter:off
            statement.addElements(ctx.bodyElement().stream()
                .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
                .toList());
            // @formatter:on
            return List.of(statement);
        });
    }
}
