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

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameterList;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MethodCompletionProvider extends ClassTypeMemberCompletionProvider {
    private static @NotNull String getParameters(final @NotNull PsiParameterList list) { // @formatter:off
        // TODO: map types to JBPL types
        return Arrays.stream(list.getParameters())
            .map(param -> String.format("%s: %s", param.getName(), param.getType().getCanonicalText()))
            .collect(Collectors.joining(", "));
    } // @formatter:on

    @Override
    protected void addCompletions(final @NotNull PsiClass clazz,
                                  final @NotNull PsiElement element,
                                  final boolean isSpecialName,
                                  final @NotNull CompletionResultSet result) {
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
