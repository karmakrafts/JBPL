package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.Order;
import dev.karmakrafts.jbpl.assembler.model.decl.*;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Instruction;
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

    public static @NotNull Declaration parse(final @NotNull ParserRuleContext ctx) {
        final var declaration = ctx.accept(INSTANCE).stream().findFirst().orElseThrow();
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
    public @NotNull List<Declaration> visitBlock(final @NotNull BlockContext ctx) {
        final var name = ctx.IDENT().getText();
        final var block = new BlockDecl(name);
        // @formatter:off
        block.addStatements(ctx.statement().stream()
            .map(StatementParser::parse)
            .toList());
        // @formatter:on
        return List.of(block);
    }

    @Override
    public @NotNull List<Declaration> visitInjector(final @NotNull InjectorContext ctx) {
        final var signature = (FunctionSignatureExpr) ExprParser.parse(ctx.functionSignature());
        final var name = ParserUtils.parseRefOrName(ctx.refOrName());
        final var injector = new InjectorDecl(signature, name);
        // @formatter:off
        injector.addStatements(ctx.statement().stream()
            .map(StatementParser::parse)
            .toList());
        // @formatter:on
        return List.of(injector);
    }

    private @NotNull SelectorDecl.Condition parseSelectorCondition(final @NotNull SelectionStatementContext ctx) {
        final var order = ctx.KW_BEFORE() != null ? Order.BEFORE : Order.AFTER;
        final var instructionCtx = ctx.instruction();
        if (instructionCtx != null) {
            final var statement = StatementParser.parse(instructionCtx);
            if (!(statement instanceof Instruction instruction)) {
                throw new IllegalStateException("Selector condition is not an instruction");
            }
            final var condition = new SelectorDecl.InstructionCondition(order, instruction);
            condition.setTokenRange(TokenRange.fromContext(ctx));
            return condition;
        }
        final var opcode = ParserUtils.parseOpcode(ctx.opcodeExpr().opcode()).orElseThrow();
        final var condition = new SelectorDecl.OpcodeCondition(order, opcode);
        condition.setTokenRange(TokenRange.fromContext(ctx));
        return condition;
    }

    @Override
    public @NotNull List<Declaration> visitSelector(final @NotNull SelectorContext ctx) {
        final var name = ctx.IDENT().getText();
        final var selector = new SelectorDecl(name);
        // @formatter:off
        selector.conditions.addAll(ctx.selectionStatement().stream()
            .map(this::parseSelectorCondition)
            .toList());
        selector.offset = ctx.selectionOffset().stream()
            .findFirst()
            .map(ExprParser::parse)
            .orElseGet(() -> LiteralExpr.of(0));
        // @formatter:on
        return List.of(selector);
    }

    @Override
    public @NotNull List<Declaration> visitMacro(final @NotNull MacroContext ctx) {
        final var name = ctx.IDENT().getText();
        final var macro = new MacroDecl(name);
        // @formatter:off
        macro.addElements(ctx.bodyElement().stream()
            .map(ElementParser::parse)
            .toList());
        // @formatter:on
        macro.parameterTypes.putAll(ParserUtils.parseParameters(ctx.parameter()));
        return List.of(macro);
    }

    @Override
    public @NotNull List<Declaration> visitPreproClass(final @NotNull PreproClassContext ctx) {
        final var name = ctx.IDENT().getText();
        final var clazz = new PreproClassDecl(name);
        clazz.addFields(ParserUtils.parseParameters(ctx.parameter()));
        return List.of(clazz);
    }

    @Override
    public @NotNull List<Declaration> visitFunction(final @NotNull FunctionContext ctx) {
        final var signature = (FunctionSignatureExpr) ExprParser.parse(ctx.functionSignature());
        final var function = new FunctionDecl(signature);
        // @formatter:off
        function.accessModifiers.addAll(ctx.accessModifier().stream()
            .map(ParserUtils::parseAccessModifier)
            .map(Optional::orElseThrow)
            .toList());
        function.addStatements(ctx.statement().stream()
            .map(StatementParser::parse)
            .toList());
        // @formatter:on
        return List.of(function);
    }

    @Override
    public @NotNull List<Declaration> visitField(final @NotNull FieldContext ctx) {
        final var signature = (FieldSignatureExpr) ExprParser.parse(ctx.fieldSignature());
        final var field = new FieldDecl(signature);
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
    }
}
