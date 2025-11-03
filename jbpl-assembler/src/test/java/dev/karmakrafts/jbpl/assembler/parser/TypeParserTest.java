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
        final var type = element.get().statement().expr().typeOfExpr().type();
        Assertions.assertNotNull(type);
        Assertions.assertEquals(expectedType, ExceptionUtils.rethrowUnchecked(() -> TypeParser.parse(type)));
    }

    @Test
    public void parseI8Type() {
        runTest(BuiltinType.I8, "typeof(i8)");
    }

    @Test
    public void parseI16Type() {
        runTest(BuiltinType.I16, "typeof(i16)");
    }

    @Test
    public void parseI32Type() {
        runTest(BuiltinType.I32, "typeof(i32)");
    }

    @Test
    public void parseI64Type() {
        runTest(BuiltinType.I64, "typeof(i64)");
    }

    @Test
    public void parseF32Type() {
        runTest(BuiltinType.F32, "typeof(f32)");
    }

    @Test
    public void parseF64Type() {
        runTest(BuiltinType.F64, "typeof(f64)");
    }

    @Test
    public void parseBoolType() {
        runTest(BuiltinType.BOOL, "typeof(bool)");
    }

    @Test
    public void parseCharType() {
        runTest(BuiltinType.CHAR, "typeof(char)");
    }

    @Test
    public void parseStringType() {
        runTest(BuiltinType.STRING, "typeof(string)");
    }

    @Test
    public void parseOpcodeType() {
        runTest(PreproType.OPCODE, "typeof(opcode)");
    }

    @Test
    public void parseTypeType() {
        runTest(PreproType.TYPE, "typeof(type)");
    }

    @Test
    public void parseInstructionType() {
        runTest(PreproType.INSTRUCTION, "typeof(instruction)");
    }

    @Test
    public void parseSelectorType() {
        runTest(PreproType.SELECTOR, "typeof(selector)");
    }

    @Test
    public void parseFieldSignatureType() {
        runTest(PreproType.FIELD_SIGNATURE, "typeof(signature(field))");
    }

    @Test
    public void parseFunctionSignatureType() {
        runTest(PreproType.FUNCTION_SIGNATURE, "typeof(signature(fun))");
    }

    @Test
    public void parseClassType() {
        runTest(new ClassType("com/example/Foo"), "typeof(<com/example/Foo>)");
    }

    @Test
    public void parsePreproClassType() {
        runTest(new PreproClassType("Testing"), "typeof(Testing)");
    }

    @Test
    public void parseIntersectionType() {
        runTest(new IntersectionType(List.of(BuiltinType.I32, BuiltinType.F32, BuiltinType.F64)),
            "typeof((i32 | f32 | f64))");
    }

    @Test
    public void parseArrayType() {
        runTest(BuiltinType.I32.array(), "typeof([i32])");
        runTest(BuiltinType.I32.array().array(), "typeof([[i32]])");
        runTest(BuiltinType.I32.array().array().array(), "typeof([[[i32]]])");
    }
}
