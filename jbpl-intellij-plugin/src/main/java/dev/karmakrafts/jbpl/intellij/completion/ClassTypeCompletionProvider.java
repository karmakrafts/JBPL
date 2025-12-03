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

package dev.karmakrafts.jbpl.intellij.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public final class ClassTypeCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static final IntSet TRIGGER_TOKENS = IntSet.of(JBPLLexer.KW_TYPE,
        JBPLLexer.KW_INJECT,
        JBPLLexer.KW_FUN,
        JBPLLexer.KW_FIELD,
        JBPLLexer.KW_YEET,
        JBPLLexer.KW_SIGNATURE,
        JBPLLexer.INSN_NEW,
        JBPLLexer.INSN_ANEWARRAY,
        JBPLLexer.INSN_CHECKCAST,
        JBPLLexer.INSN_INSTANCEOF);
    private static final IntSet ALLOWED_TOKENS = IntSet.of(JBPLLexer.IDENT, JBPLLexer.SLASH, JBPLLexer.L_ABRACKET);

    private static @NotNull String getClassTypeText(final @NotNull PsiClass clazz) {
        if (clazz.isInterface()) {
            return "interface";
        }
        else if (clazz.isEnum()) {
            return "enum";
        }
        else if (clazz.isRecord()) {
            return "record";
        }
        else if (clazz.isAnnotationType()) {
            return "annotation";
        }
        return "class";
    }

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final @NotNull ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        final var element = parameters.getPosition();
        var prevElement = PsiTreeUtil.prevVisibleLeaf(Objects.requireNonNullElse(parameters.getOriginalPosition(),
            element));
        if (prevElement == null) {
            return;
        }
        var tokenType = PsiUtils.getTokenType(prevElement);
        var lBracketFound = false;
        final var nameSegments = new ArrayList<String>();
        while (ALLOWED_TOKENS.contains(tokenType)) {
            switch (tokenType) {
                case JBPLLexer.L_ABRACKET -> lBracketFound = true;
                case JBPLLexer.IDENT -> nameSegments.add(prevElement.getText());
            }
            prevElement = PsiTreeUtil.prevVisibleLeaf(prevElement);
            if (prevElement == null) {
                return; // We didn't find the keyword as the root of context
            }
            tokenType = PsiUtils.getTokenType(prevElement);
            if (TRIGGER_TOKENS.contains(tokenType) && lBracketFound) {
                Collections.reverse(nameSegments);
                final var packageName = String.join(".", nameSegments);
                final var prefixMatcher = result.getPrefixMatcher();
                final var project = element.getProject();
                final var facade = JavaPsiFacade.getInstance(project);
                final var pkg = facade.findPackage(packageName);
                if (pkg == null) {
                    break;
                }
                // @formatter:off
                result.addAllElements(Arrays.stream(pkg.getSubPackages())
                    .filter(subPkg -> subPkg.getName() != null && prefixMatcher.prefixMatches(subPkg.getName()))
                    .map(subPkg -> LookupElementBuilder.create(subPkg)
                        .withPresentableText(subPkg.getName())
                        .withTypeText("package")
                        .withIcon(subPkg.getIcon(0)))
                    .toList());
                result.addAllElements(Arrays.stream(pkg.getClasses())
                    .filter(clazz -> clazz.getName() != null && prefixMatcher.prefixMatches(clazz.getName()))
                    .map(clazz -> LookupElementBuilder.create(clazz)
                        .withPresentableText(clazz.getName())
                        .withTypeText(getClassTypeText(clazz))
                        .withIcon(clazz.getIcon(0)))
                    .toList());
                // @formatter:on
                break;
            }
        }
    }
}
