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

import dev.karmakrafts.jbpl.assembler.model.decl.*;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.assembler.util.ParserUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DeclarationParser extends JBPLParserBaseVisitor<List<Declaration>> {
    public static final DeclarationParser INSTANCE = new DeclarationParser();

    private DeclarationParser() {
    }

    public static @NotNull Declaration parse(final @NotNull ParserRuleContext ctx) throws ParserException {
        // @formatter:off
        final var declaration = ctx.accept(INSTANCE).stream()
            .findFirst()
            .orElseThrow(() -> new ParserException("Could not parse declaration", null));
        // @formatter:on
        declaration.setTokenRange(TokenRange.fromContext(ctx));
        return declaration;
    }

    @Override
    protected @NotNull List<Declaration> defaultResult() {
        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<Declaration> aggregateResult(final @NotNull List<Declaration> aggregate,
                                                         final @NotNull List<Declaration> nextResult) {
        aggregate.addAll(nextResult);
        return aggregate;
    }

    @Override
    public @NotNull List<Declaration> visitInjector(final @NotNull InjectorContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var injector = new InjectorDecl();
            injector.setTarget(ExprParser.parse(ctx.functionSignature()));
            final var selector = ctx.exprOrName();
            if (selector != null) {
                injector.setSelector(ExprParser.parse(selector));
            }
            // @formatter:off
            injector.addStatements(ctx.statement().stream()
                .map(ExceptionUtils.unsafeFunction(StatementParser::parse))
                .toList());
            // @formatter:on
            return List.of(injector);
        });
    }

    @Override
    public @NotNull List<Declaration> visitMacro(final @NotNull MacroContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var signature = ctx.macroSignature();
            final var name = ExprParser.parse(signature.exprOrName());
            // @formatter:off
            final var returnType = signature.exprOrType() != null
                ? ExprParser.parse(signature.exprOrType())
                : ConstExpr.unit();
            // @formatter:on
            final var isPrivate = ctx.KW_PRIVATE() != null;
            final var macro = new MacroDecl(name, returnType, isPrivate);
            // @formatter:off
            macro.addElements(ctx.bodyElement().stream()
                .map(ExceptionUtils.unsafeFunction(ElementParser::parse))
                .toList());
            // @formatter:on
            macro.addParameters(ParserUtils.parseParameters(signature.parameter()));
            return List.of(macro);
        });
    }

    @Override
    public @NotNull List<Declaration> visitPreproClass(final @NotNull PreproClassContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var name = ExprParser.parse(ctx.exprOrName());
            final var clazz = new PreproClassDecl(name);
            clazz.addFields(ParserUtils.parseParameters(ctx.parameter()));
            return List.of(clazz);
        });
    }

    @Override
    public @NotNull List<Declaration> visitFunction(final @NotNull FunctionContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var function = new FunctionDecl();
            function.setSignature(ExprParser.parse(ctx.functionSignature()));
            // @formatter:off
            function.accessModifiers.addAll(ctx.accessModifier().stream()
                .map(ParserUtils::parseAccessModifier)
                .map(Optional::orElseThrow)
                .toList());
            function.addStatements(ctx.statement().stream()
                .map(ExceptionUtils.unsafeFunction(StatementParser::parse))
                .toList());
            // @formatter:on
            return List.of(function);
        });
    }

    @Override
    public @NotNull List<Declaration> visitField(final @NotNull FieldContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var field = new FieldDecl();
            field.setSignature(ExprParser.parse(ctx.fieldSignature()));
            final var initializer = ctx.expr();
            if (initializer != null) {
                field.setInitializer(ExprParser.parse(initializer));
            }
            // @formatter:off
            field.accessModifiers.addAll(ctx.accessModifier().stream()
                .map(ParserUtils::parseAccessModifier)
                .map(Optional::orElseThrow)
                .toList());
            // @formatter:on
            return List.of(field);
        });
    }
}
