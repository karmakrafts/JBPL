package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.type.*;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class TypeParser extends JBPLParserBaseVisitor<List<Type>> {
    public static final TypeParser INSTANCE = new TypeParser();

    private TypeParser() {
    }

    public static @NotNull Type parse(final @NotNull ParserRuleContext ctx) throws ParserException { // @formatter:off
        return ctx.accept(INSTANCE).stream()
            .findFirst()
            .orElseThrow(() -> new ParserException("Could not parse type", null));
    } // @formatter:on

    @Override
    protected @NotNull List<Type> defaultResult() {
        return new ArrayList<>();
    }

    @Override
    protected @NotNull List<Type> aggregateResult(final @NotNull List<Type> aggregate,
                                                  final @NotNull List<Type> nextResult) {
        aggregate.addAll(nextResult);
        return aggregate;
    }

    @Override
    public @NotNull List<Type> visitSignatureType(final @NotNull SignatureTypeContext ctx) {
        if (ctx.KW_FIELD() != null) {
            return List.of(PreproType.FIELD_SIGNATURE);
        }
        return List.of(PreproType.FUNCTION_SIGNATURE);
    }

    @Override
    public @NotNull List<Type> visitIntersectionType(final @NotNull IntersectionTypeContext ctx) {
        // @formatter:off
        final var alternatives = ctx.type()
            .stream()
            .flatMap(type -> visitType(type).stream())
            .toList();
        // @formatter:on
        return List.of(new IntersectionType(alternatives));
    }

    @Override
    public @NotNull List<Type> visitType(final @NotNull TypeContext ctx) {
        final var signature = ctx.signatureType();
        if (signature != null) {
            // @formatter:off
            return List.of(signature.KW_FIELD() != null
                ? PreproType.FIELD_SIGNATURE
                : PreproType.FUNCTION_SIGNATURE);
            // @formatter:on
        }
        // @formatter:off
        if (ctx.KW_TYPE() != null)             return List.of(PreproType.TYPE);
        else if (ctx.KW_SELECTOR() != null)    return List.of(PreproType.SELECTOR);
        else if (ctx.KW_OPCODE() != null)      return List.of(PreproType.OPCODE);
        else if (ctx.KW_INSTRUCTION() != null) return List.of(PreproType.INSTRUCTION);
        else if (ctx.KW_STRING() != null)      return List.of(BuiltinType.STRING);
        else if (ctx.IDENT() != null)          return List.of(new PreproClassType(ctx.IDENT().getText()));
        else if (ctx.KW_I8() != null)          return List.of(BuiltinType.I8);
        else if (ctx.KW_I16() != null)         return List.of(BuiltinType.I16);
        else if (ctx.KW_I32() != null)         return List.of(BuiltinType.I32);
        else if (ctx.KW_I64() != null)         return List.of(BuiltinType.I64);
        else if (ctx.KW_F32() != null)         return List.of(BuiltinType.F32);
        else if (ctx.KW_F64() != null)         return List.of(BuiltinType.F64);
        else if (ctx.KW_VOID() != null)        return List.of(BuiltinType.VOID);
        else if (ctx.KW_BOOL() != null)        return List.of(BuiltinType.BOOL);
        else if (ctx.KW_CHAR() != null)        return List.of(BuiltinType.CHAR);
        else                                   return super.visitType(ctx);
        // @formatter:on
    }

    @Override
    public @NotNull List<Type> visitArrayType(final @NotNull ArrayTypeContext ctx) {
        return List.of(ExceptionUtils.rethrowUnchecked(() -> parse(ctx.type()).array()));
    }

    @Override
    public @NotNull List<Type> visitClassType(final @NotNull ClassTypeContext ctx) {
        // @formatter:off
        final var name = ctx.nameSegment().stream()
            .map(NameSegmentContext::getText)
            .collect(Collectors.joining("/"));
        // @formatter:on
        return List.of(new ClassType(name));
    }
}
