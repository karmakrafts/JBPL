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

package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.Assembler;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.objectweb.asm.tree.ClassNode;

@TestInstance(Lifecycle.PER_METHOD)
public final class AssemblyFileTest {
    private final Assembler assembler = Assembler.createFromResources("");

    @Test
    public void getTokens() {
        final var context = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test5.jbpl",
            name -> new ClassNode()));
        final var tokens = context.file.getTokens();
        Assertions.assertEquals(JBPLLexer.NL, tokens.get(0).getType());
        Assertions.assertEquals(JBPLLexer.KW_DEFINE, tokens.get(1).getType());
        Assertions.assertEquals(JBPLLexer.WS, tokens.get(2).getType());
        Assertions.assertEquals(JBPLLexer.IDENT, tokens.get(3).getType());
        Assertions.assertEquals(JBPLLexer.COLON, tokens.get(4).getType());
        Assertions.assertEquals(JBPLLexer.WS, tokens.get(5).getType());
        Assertions.assertEquals(JBPLLexer.KW_I32, tokens.get(6).getType());
        Assertions.assertEquals(JBPLLexer.WS, tokens.get(7).getType());
        Assertions.assertEquals(JBPLLexer.EQ, tokens.get(8).getType());
        Assertions.assertEquals(JBPLLexer.WS, tokens.get(9).getType());
        Assertions.assertEquals(JBPLLexer.LITERAL_INT, tokens.get(10).getType());
    }

    @Test
    public void getSourceRangeOfFile() {
        final var context = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test5.jbpl",
            name -> new ClassNode()));
        final var file = context.file;
        final var sourceRange = file.getSourceRange(file.getTokenRange());
        Assertions.assertEquals(0, sourceRange.startLine());
        Assertions.assertEquals(0, sourceRange.startColumn());
        Assertions.assertEquals(1, sourceRange.endLine());
        Assertions.assertEquals(18, sourceRange.endColumn());
    }

    @Test
    public void getSourceRangeOfElement() {
        final var context = ExceptionUtils.rethrowUnchecked(() -> assembler.getOrParseAndLowerFile("test5.jbpl",
            name -> new ClassNode()));
        final var file = context.file;
        final var value = file.findElementInTree(LiteralExpr.class).orElseThrow();
        final var sourceRange = file.getSourceRange(value.getTokenRange());
        Assertions.assertEquals(1, sourceRange.startLine());
        Assertions.assertEquals(16, sourceRange.startColumn());
        Assertions.assertEquals(1, sourceRange.endLine());
        Assertions.assertEquals(17, sourceRange.endColumn());
    }
}
