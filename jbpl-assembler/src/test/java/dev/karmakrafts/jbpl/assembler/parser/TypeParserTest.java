package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.type.*;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public final class TypeParserTest extends AbstractParserTest {
    private void runTest(final @NotNull Type expectedType, final @NotNull String actualType) {
        final var result = parse(actualType);
        result.shouldSucceed();
        final var element = Objects.requireNonNull(result.context()).bodyElement().stream().findFirst();
        Assertions.assertTrue(element.isPresent());
        final var type = element.get().statement().expr().literal().typeLiteral().type();
        Assertions.assertNotNull(type);
        Assertions.assertEquals(expectedType, ExceptionUtils.rethrowUnchecked(() -> TypeParser.parse(type)));
    }

    @Test
    public void parseI8Type() {
        runTest(BuiltinType.I8, "type i8");
    }

    @Test
    public void parseI16Type() {
        runTest(BuiltinType.I16, "type i16");
    }

    @Test
    public void parseI32Type() {
        runTest(BuiltinType.I32, "type i32");
    }

    @Test
    public void parseI64Type() {
        runTest(BuiltinType.I64, "type i64");
    }

    @Test
    public void parseF32Type() {
        runTest(BuiltinType.F32, "type f32");
    }

    @Test
    public void parseF64Type() {
        runTest(BuiltinType.F64, "type f64");
    }

    @Test
    public void parseBoolType() {
        runTest(BuiltinType.BOOL, "type bool");
    }

    @Test
    public void parseCharType() {
        runTest(BuiltinType.CHAR, "type char");
    }

    @Test
    public void parseStringType() {
        runTest(BuiltinType.STRING, "type string");
    }

    @Test
    public void parseOpcodeType() {
        runTest(PreproType.OPCODE, "type opcode");
    }

    @Test
    public void parseTypeType() {
        runTest(PreproType.TYPE, "type type");
    }

    @Test
    public void parseInstructionType() {
        runTest(PreproType.INSTRUCTION, "type instruction");
    }

    @Test
    public void parseSelectorType() {
        runTest(PreproType.SELECTOR, "type selector");
    }

    @Test
    public void parseFieldSignatureType() {
        runTest(PreproType.FIELD_SIGNATURE, "type signature(field)");
    }

    @Test
    public void parseFunctionSignatureType() {
        runTest(PreproType.FUNCTION_SIGNATURE, "type signature(fun)");
    }

    @Test
    public void parseClassType() {
        runTest(new ClassType("com/example/Foo"), "type <com/example/Foo>");
    }

    @Test
    public void parsePreproClassType() {
        runTest(new PreproClassType("Testing"), "type Testing");
    }

    @Test
    public void parseIntersectionType() {
        runTest(new IntersectionType(List.of(BuiltinType.I32, BuiltinType.F32, BuiltinType.F64)),
            "type (i32 | f32 | f64)");
    }

    @Test
    public void parseArrayType() {
        runTest(BuiltinType.I32.array(), "type [i32]");
        runTest(BuiltinType.I32.array().array(), "type [[i32]]");
        runTest(BuiltinType.I32.array().array().array(), "type [[[i32]]]");
    }
}
