package dev.karmakrafts.jbpl.assembler.parser;

import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public final class ExprParserTest extends AbstractParserTest {
    private void runLiteralTest(final @NotNull Object expectedValue, final @NotNull String actualValue) {
        final var result = parse(actualValue);
        result.shouldSucceed();
        final var element = Objects.requireNonNull(result.context()).bodyElement().stream().findFirst();
        Assertions.assertTrue(element.isPresent());
        final var expr = element.get().statement().expr();
        Assertions.assertNotNull(expr);
        final var parseExpr = ExprParser.parse(expr);
        if (!(parseExpr instanceof LiteralExpr literalExpr)) {
            Assertions.fail();
            return;
        }
        Assertions.assertEquals(expectedValue, literalExpr.value);
    }

    @Test
    public void parseI8Literal() {
        runLiteralTest((byte) 0, "0i8");
        runLiteralTest((byte) 1, "1i8");
    }

    @Test
    public void parseI16Literal() {
        runLiteralTest((short) 0, "0i16");
        runLiteralTest((short) 1, "1i16");
    }

    @Test
    public void parseI32Literal() {
        runLiteralTest(0, "0i32");
        runLiteralTest(1, "1i32");
    }

    @Test
    public void parseI64Literal() {
        runLiteralTest(0L, "0i64");
        runLiteralTest(1L, "1i64");
    }

    @Test
    public void parseF32Literal() {
        runLiteralTest(0F, "0f32");
        runLiteralTest(1F, "1f32");
    }

    @Test
    public void parseF64Literal() {
        runLiteralTest(0.0, "0f64");
        runLiteralTest(1.0, "1f64");
    }

    @Test
    public void parseBoolLiteral() {
        runLiteralTest(false, "false");
        runLiteralTest(true, "true");
    }

    @Test
    public void parseCharLiteral() {
        runLiteralTest(' ', "' '");
        runLiteralTest('X', "'X'");
        runLiteralTest('\n', "'\\n'");
    }

    @Test
    public void parseStringLiteral() {
        runLiteralTest("", "\"\"");
        runLiteralTest(" ", "\" \"");
        runLiteralTest("\n", "\"\n\"");
        runLiteralTest("Hello, World!", "\"Hello, World!\"");
    }

    @Test
    public void parseFieldSignature() {
        final var result = parse("<com/example/Test>.myField: i32");
        result.shouldSucceed();
        final var file = result.context();
        Assertions.assertNotNull(file);
        final var element = file.bodyElement().stream().findFirst();
        Assertions.assertTrue(element.isPresent());
        final var signature = element.get().statement().expr().signatureExpr();
        Assertions.assertNotNull(signature);
        final var expectedField = new FieldSignatureExpr(LiteralExpr.of(new ClassType("com/example/Test")),
            LiteralExpr.of("myField"),
            LiteralExpr.of(BuiltinType.I32));
        Assertions.assertEquals(expectedField, ExprParser.parse(signature));
    }

    @Test
    public void parseFunctionSignature() {
        final var result = parse("<com/example/Test>.myFunction(f32): i32");
        result.shouldSucceed();
        final var file = result.context();
        Assertions.assertNotNull(file);
        final var element = file.bodyElement().stream().findFirst();
        Assertions.assertTrue(element.isPresent());
        final var signature = element.get().statement().expr().signatureExpr();
        Assertions.assertNotNull(signature);
        final var expectedField = new FunctionSignatureExpr(LiteralExpr.of(new ClassType("com/example/Test")),
            LiteralExpr.of("myFunction"),
            LiteralExpr.of(BuiltinType.I32));
        expectedField.addExpression(LiteralExpr.of(BuiltinType.F32));
        final var expr = ExprParser.parse(signature);
        Assertions.assertEquals(expectedField, expr);
    }
}
