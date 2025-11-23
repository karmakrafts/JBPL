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
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.ProcessingContext;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.psi.ClassTypeNode;
import dev.karmakrafts.jbpl.intellij.psi.NameSegmentNode;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class IdentCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static @NotNull Predicate<? super PsiNamedElement> elementName(final @NotNull PrefixMatcher prefixMatcher) {
        return element -> {
            final var name = element.getName();
            if (name == null) {
                return false;
            }
            return prefixMatcher.prefixMatches(name);
        };
    }

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final @NotNull ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        var element = parameters.getPosition();
        // Resolve raw identifiers to their segment parent if present
        if (element.getNode().getElementType() instanceof TokenIElementType tokenType && tokenType.getANTLRTokenType() == JBPLLexer.IDENT) {
            final var segment = element.getParent();
            if (segment != null) {
                element = segment;
            }
        }
        if (!(element.getParent() instanceof ClassTypeNode classTypeNode)) {
            return;
        }
        final var children = List.of(classTypeNode.getChildren());
        final var index = children.indexOf(element);
        final var project = element.getProject();
        final var facade = JavaPsiFacade.getInstance(project);
        // @formatter:off
        final var packageName = children.subList(0, index).stream()
            .filter(NameSegmentNode.class::isInstance)
            .map(NameSegmentNode.class::cast)
            .map(NameSegmentNode::getName)
            .collect(Collectors.joining("."));
        // @formatter:on
        final var pkg = facade.findPackage(packageName);
        if (pkg == null) {
            return;
        }
        final var matcher = result.getPrefixMatcher();
        // @formatter:off
        result.addAllElements(Arrays.stream(pkg.getClasses())
            .filter(elementName(matcher))
            .map(clazz -> LookupElementBuilder.create(clazz)
                .withPresentableText(Objects.requireNonNull(clazz.getName()))
                .withTypeText("class")
                .withIcon(clazz.getIcon(0)))
            .toList());
        result.addAllElements(Arrays.stream(pkg.getSubPackages())
            .filter(elementName(matcher))
            .map(subPackage -> LookupElementBuilder.create(subPackage)
                .withPresentableText(Objects.requireNonNull(subPackage.getName()))
                .withTypeText("package")
                .withIcon(subPackage.getIcon(0)))
            .toList());
        // @formatter:on
    }
}
