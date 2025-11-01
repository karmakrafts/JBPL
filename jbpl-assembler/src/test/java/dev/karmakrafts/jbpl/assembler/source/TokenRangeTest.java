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

package dev.karmakrafts.jbpl.assembler.source;

import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.List;

@TestInstance(Lifecycle.PER_METHOD)
public final class TokenRangeTest {
    @Test
    public void fromToken() {
        final var token = new CommonToken(JBPLLexer.IDENT, "Testing");
        token.setTokenIndex(0);
        final var range = TokenRange.fromToken(token);
        Assertions.assertEquals(0, range.start());
        Assertions.assertEquals(0, range.end());
    }

    @Test
    public void fromContext() {
        final var context = new ParserRuleContext();
        final var token1 = new CommonToken(JBPLLexer.IDENT, "Testing");
        token1.setTokenIndex(0);
        context.addChild(new TerminalNodeImpl(token1));
        context.start = token1;
        final var token2 = new CommonToken(JBPLLexer.LITERAL_INT, "12345");
        token2.setTokenIndex(1);
        context.addChild(new TerminalNodeImpl(token2));
        context.stop = token2;
        final var range = TokenRange.fromContext(context);
        Assertions.assertEquals(0, range.start());
        Assertions.assertEquals(1, range.end());
    }

    @Test
    public void contains() {
        final var range1 = new TokenRange(4, 6);
        final var range2 = new TokenRange(0, 10);
        Assertions.assertTrue(range2.contains(range1));
        Assertions.assertFalse(range1.contains(range2));
    }

    @Test
    public void unionAdjacent() {
        final var range1 = new TokenRange(0, 2);
        final var range2 = new TokenRange(3, 5);
        final var range3 = TokenRange.union(List.of(range1, range2));
        Assertions.assertEquals(0, range3.start());
        Assertions.assertEquals(5, range3.end());
    }

    @Test
    public void unionWithGap() {
        final var range1 = new TokenRange(0, 2);
        final var range2 = new TokenRange(4, 5);
        final var range3 = TokenRange.union(List.of(range1, range2));
        Assertions.assertEquals(0, range3.start());
        Assertions.assertEquals(5, range3.end());
    }
}
