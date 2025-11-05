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

package dev.karmakrafts.jbpl.intellij;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JBPLBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[] { // @formatter:off
        makePair(JBPLLexer.M_CONST_STR_LERP_BEGIN, JBPLLexer.R_BRACE, true),
        makePair(JBPLLexer.L_BRACE, JBPLLexer.R_BRACE, true),
        makePair(JBPLLexer.L_PAREN, JBPLLexer.R_PAREN, false),
        makePair(JBPLLexer.L_SQBRACKET, JBPLLexer.R_SQBRACKET, false)
    }; // @formatter:on

    private static @NotNull BracePair makePair(final int lType, final int rType, final boolean structural) {
        return new BracePair(JBPLParserDefinition.getTokenType(lType),
            JBPLParserDefinition.getTokenType(rType),
            structural);
    }

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(final @NotNull IElementType lType,
                                                   final @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(final @NotNull PsiFile file, final int openingOffset) {
        return openingOffset;
    }
}
