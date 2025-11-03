package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.instruction.*;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
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

    public static @NotNull Instruction parse(final @NotNull ParserRuleContext ctx) throws ParserException {
        // @formatter:off
        final var instruction = ctx.accept(INSTANCE).stream()
            .findFirst()
            .orElseThrow(() -> new ParserException("Could not parse instruction", null));
        // @formatter:on
        instruction.setTokenRange(TokenRange.fromContext(ctx));
        return instruction;
    }

    @Override
    public @NotNull List<Instruction> visitOplessInstruction(final @NotNull OplessInstructionContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            // @formatter:off
            return ParserUtils.maybeParseOpcode(ctx)
                .map(op -> List.<Instruction>of(new OplessInstruction(op)))
                .orElseThrow(() -> new ParserException("Could not parse opless instruction '%s'", null));
            // @formatter:on
        });
    }

    @Override
    public List<Instruction> visitLoad(final @NotNull LoadContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.INSN_LOAD());
            final var localIndexNode = ctx.intLiteral();
            if (localIndexNode != null) {
                final var localIndex = ExprParser.parse(localIndexNode);
                return List.of(new StackInstruction(opcode, localIndex));
            }
            final var localName = LiteralExpr.of(ctx.IDENT().getText());
            return List.of(new StackInstruction(opcode, localName));
        });
    }

    @Override
    public List<Instruction> visitStore(final @NotNull StoreContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.INSN_STORE());
            final var localIndexNode = ctx.intLiteral();
            if (localIndexNode != null) {
                final var localIndex = ExprParser.parse(localIndexNode);
                return List.of(new StackInstruction(opcode, localIndex));
            }
            final var localName = LiteralExpr.of(ctx.IDENT().getText());
            return List.of(new StackInstruction(opcode, localName));
        });
    }

    @Override
    public List<Instruction> visitFieldLoad(final @NotNull FieldLoadContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.INSN_GET());
            final var signature = ExprParser.parse(ctx.fieldSignature());
            return List.of(new FieldInstruction(opcode, signature));
        });
    }

    @Override
    public List<Instruction> visitFieldStore(final @NotNull FieldStoreContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.INSN_PUT());
            final var signature = ExprParser.parse(ctx.fieldSignature());
            return List.of(new FieldInstruction(opcode, signature));
        });
    }

    @Override
    public List<Instruction> visitLdc(final @NotNull LdcContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.INSN_LDC());
            final var value = ExprParser.parse(ctx.literal());
            return List.of(new LoadConstantInstruction(opcode, value));
        });
    }

    @Override
    public List<Instruction> visitInvoke(final @NotNull InvokeContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.INSN_INVOKE());
            final var signature = ExprParser.parse(ctx.functionSignature());
            return List.of(new InvokeInstruction(opcode, signature));
        });
    }

    @Override
    public List<Instruction> visitJump(final @NotNull JumpContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
            final var opcode = ParserUtils.parseOpcode(ctx.jumpInstruction());
            final var signature = ParserUtils.parseRefOrName(ctx.refOrName());
            return List.of(new JumpInstruction(opcode, signature));
        });
    }

    @Override
    public List<Instruction> visitInvokedynamic(final @NotNull InvokedynamicContext ctx) {
        return ExceptionUtils.rethrowUnchecked(() -> {
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
            instruction.addArguments(ctx.expr().stream().map(ExceptionUtils.unsafeFunction(ExprParser::parse)).toList());
            return List.of();
        });
    }
}
