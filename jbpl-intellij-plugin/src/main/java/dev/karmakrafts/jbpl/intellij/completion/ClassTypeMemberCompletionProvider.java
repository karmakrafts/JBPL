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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.psi.ClassTypeNode;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

public abstract class ClassTypeMemberCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final @NotNull ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        final var element = parameters.getPosition();
        var prevElement = PsiTreeUtil.prevVisibleLeaf(element);
        if (prevElement == null) {
            return;
        }
        var isSpecialName = false;
        if (PsiUtils.getTokenType(prevElement) == JBPLLexer.L_ABRACKET) {
            // This may be a special function name, so we have to skip over the <
            prevElement = PsiTreeUtil.prevVisibleLeaf(prevElement);
            isSpecialName = true;
            if (prevElement == null) {
                return;
            }
        }
        if (PsiUtils.getTokenType(prevElement) != JBPLLexer.DOT) {
            return;
        }
        prevElement = PsiTreeUtil.prevVisibleLeaf(prevElement);
        if (prevElement == null || PsiUtils.getTokenType(prevElement) != JBPLLexer.R_ABRACKET) {
            return;
        }
        final var parent = (ClassTypeNode) PsiTreeUtil.findFirstParent(prevElement, ClassTypeNode.class::isInstance);
        if (parent == null) {
            return;
        }
        final var project = element.getProject();
        final var facade = JavaPsiFacade.getInstance(project);
        final var scope = GlobalSearchScope.allScope(project);
        var clazz = facade.findClass(parent.getName(), scope);
        if (clazz == null) {
            return; // We didn't find any class with that name, so we can't add any completions
        }
        addCompletions(clazz, element, isSpecialName, result);
    }

    protected abstract void addCompletions(final @NotNull PsiClass clazz,
                                           final @NotNull PsiElement element,
                                           final boolean isSpecialName,
                                           final @NotNull CompletionResultSet result);
}
