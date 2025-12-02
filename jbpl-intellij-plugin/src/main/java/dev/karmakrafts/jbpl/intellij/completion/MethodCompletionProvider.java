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
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.psi.ClassTypeNode;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MethodCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static @NotNull String getParameters(final @NotNull PsiParameterList list) { // @formatter:off
        // TODO: map types to JBPL types
        return Arrays.stream(list.getParameters())
            .map(param -> String.format("%s: %s", param.getName(), param.getType().getCanonicalText()))
            .collect(Collectors.joining(", "));
    } // @formatter:on

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
        // Handle special names accordingly
        if (isSpecialName) { // @formatter:off
            // <clinit> is always visible for every class
            result.addElement(LookupElementBuilder.create("clinit")
                .withPresentableText("<clinit>")
                .withTypeText("method")
                .withIcon(Icons.STATIC_METHOD));
            // <init> methods are resolved by constructor overloads
            result.addAllElements(Arrays.stream(clazz.getConstructors())
                .map(method -> LookupElementBuilder.create("init")
                    .withPresentableText(String.format("<init>(%s)", getParameters(method.getParameterList())))
                    .withTypeText("method")
                    .withIcon(method.getIcon(0)))
                .toList());
            return;
        } // @formatter:on
        // Otherwise we need to lookup methods by their name prefix
        final var prefixMatcher = result.getPrefixMatcher();
        // @formatter:off
        result.addAllElements(Arrays.stream(clazz.getMethods())
            .filter(method -> !method.isConstructor() && prefixMatcher.prefixMatches(method.getName()))
            .map(method -> LookupElementBuilder.create(method)
                .withPresentableText(String.format("%s(%s)", method.getName(), getParameters(method.getParameterList())))
                .withTypeText("method")
                .withIcon(method.getIcon(0)))
            .toList());
        // @formatter:on
    }
}
