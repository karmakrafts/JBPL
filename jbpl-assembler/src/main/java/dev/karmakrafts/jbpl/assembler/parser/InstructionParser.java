package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.*;
import dev.karmakrafts.jbpl.assembler.util.ParserUtils;
import dev.karmakrafts.jbpl.frontend.JBPLParser.*;
import dev.karmakrafts.jbpl.frontend.JBPLParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class InstructionParser extends JBPLParserBaseVisitor<List<Instruction>> {
    public static final InstructionParser INSTANCE = new InstructionParser();

    private InstructionParser() {
    }

    public static @NotNull Instruction parse(final @NotNull ParserRuleContext ctx) {
        final var instruction = ctx.accept(INSTANCE).stream().findFirst().orElseThrow();
        instruction.setTokenRange(TokenRange.fromContext(ctx));
        return instruction;
    }

    @Override
    public @NotNull List<Instruction> visitOplessInstruction(final @NotNull OplessInstructionContext ctx) {
        // @formatter:off
        return ParserUtils.parseOpcode(ctx)
            .map(op -> List.<Instruction>of(new OplessInstruction(op)))
            .orElseThrow();
        // @formatter:on
    }

    @Override
    public List<Instruction> visitLoad(final @NotNull LoadContext ctx) {
        final var opcode = ParserUtils.parseOpcode(ctx.INSN_LOAD()).orElseThrow();
        final var localIndexNode = ctx.intLiteral();
        if (localIndexNode != null) {
            final var localIndex = ExprParser.parse(localIndexNode);
            return List.of(new StackInstruction(opcode, localIndex));
        }
        final var localName = LiteralExpr.of(ctx.IDENT().getText());
        return List.of(new StackInstruction(opcode, localName));
    }

    @Override
    public List<Instruction> visitStore(final @NotNull StoreContext ctx) {
        final var opcode = ParserUtils.parseOpcode(ctx.INSN_STORE()).orElseThrow();
        final var localIndexNode = ctx.intLiteral();
        if (localIndexNode != null) {
            final var localIndex = ExprParser.parse(localIndexNode);
            return List.of(new StackInstruction(opcode, localIndex));
        }
        final var localName = LiteralExpr.of(ctx.IDENT().getText());
        return List.of(new StackInstruction(opcode, localName));
    }

    @Override
    public List<Instruction> visitFieldLoad(final @NotNull FieldLoadContext ctx) {
        final var opcode = ParserUtils.parseOpcode(ctx.INSN_GET()).orElseThrow();
        final var signature = ExprParser.parse(ctx.fieldSignature());
        return List.of(new FieldInstruction(opcode, signature));
    }

    @Override
    public List<Instruction> visitFieldStore(final @NotNull FieldStoreContext ctx) {
        final var opcode = ParserUtils.parseOpcode(ctx.INSN_PUT()).orElseThrow();
        final var signature = ExprParser.parse(ctx.fieldSignature());
        return List.of(new FieldInstruction(opcode, signature));
    }

    @Override
    public List<Instruction> visitLdc(LdcContext ctx) {
        final var value = ExprParser.parse(ctx.literal());
        return List.of(new LoadConstantInstruction(value));
    }

    @Override
    public List<Instruction> visitInvoke(final @NotNull InvokeContext ctx) {
        final var opcode = ParserUtils.parseOpcode(ctx.INSN_INVOKE()).orElseThrow();
        final var signature = ExprParser.parse(ctx.functionSignature());
        return List.of(new InvokeInstruction(opcode, signature));
    }

    @Override
    public List<Instruction> visitInvokedynamic(final @NotNull InvokedynamicContext ctx) {
        var signatureIndex = 0;
        final var instantiatedSignature = ExprParser.parse(ctx.functionSignature(signatureIndex++)); // First signature is instantiated type
        var samSignature = instantiatedSignature;
        if (ctx.KW_BY() != null) { // If this is present, we have an explicit SAM type
            samSignature = ExprParser.parse(ctx.functionSignature(signatureIndex));
        }
        final var bsmInstruction = ExprParser.parse(ctx.invoke(0)); // First instruction is BSM
        final var targetInstruction = ExprParser.parse(ctx.invoke(1)); // Second instruction is target
        final var instruction = new InvokeDynamicInstruction(instantiatedSignature,
            samSignature,
            bsmInstruction,
            targetInstruction);
        // Add zero or more additional constant parameters to the end of the argument list passed inside the ()
        instruction.addArguments(ctx.expr().stream().map(ExprParser::parse).toList());
        return List.of();
    }
}
