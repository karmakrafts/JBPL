package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.DeclarationContext;
import dev.karmakrafts.jbpl.frontend.JBPLParser.StatementContext;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ElementParser extends JBPLParserBaseVisitor<List<? extends Element>> {
    public static final ElementParser INSTANCE = new ElementParser();

    private ElementParser() {
    }

    public static @NotNull Element parse(final @NotNull ParserRuleContext ctx) throws ParserException { // @formatter:off
        return ctx.accept(INSTANCE).stream()
            .findFirst()
            .orElseThrow(() -> new ParserException("Could not parse element", null));
    } // @formatter:on

    @Override
    public @NotNull List<? extends Element> visitDeclaration(final @NotNull DeclarationContext ctx) {
        return List.of(ExceptionUtils.rethrowUnchecked(() -> DeclarationParser.parse(ctx)));
    }

    @Override
    public @NotNull List<? extends Element> visitStatement(final @NotNull StatementContext ctx) {
        return List.of(ExceptionUtils.rethrowUnchecked(() -> StatementParser.parse(ctx)));
    }
}
