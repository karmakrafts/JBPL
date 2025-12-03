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

package dev.karmakrafts.jbpl.intellij.util;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.JBPLanguage;
import dev.karmakrafts.jbpl.intellij.psi.JBPLPsiLeafNode;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

public final class PsiUtils {
    private PsiUtils() {
    }

    public static @NotNull List<PsiFile> getFilesRecursively(final @NotNull PsiDirectory dir) {
        final var dirStack = new Stack<PsiDirectory>();
        dirStack.push(dir);
        final var directories = new ArrayList<PsiFile>();
        while (!dirStack.isEmpty()) {
            final var subDir = dirStack.pop();
            final var children = List.of(subDir.getFiles());
            directories.addAll(children);
            dirStack.addAll(List.of(subDir.getSubdirectories()));
        }
        return directories;
    }

    public static @NotNull List<PsiDirectory> getSubDirectoriesRecursively(final @NotNull PsiDirectory dir) {
        final var dirStack = new Stack<PsiDirectory>();
        dirStack.push(dir);
        final var directories = new ArrayList<PsiDirectory>();
        while (!dirStack.isEmpty()) {
            final var subDir = dirStack.pop();
            final var children = List.of(subDir.getSubdirectories());
            directories.addAll(children);
            dirStack.addAll(children);
        }
        return directories;
    }

    public static int getTokenType(final @NotNull PsiElement element) {
        if (!(element.getNode().getElementType() instanceof TokenIElementType tokenType)) {
            return -1;
        }
        return tokenType.getANTLRTokenType();
    }

    public static boolean hasToken(final @NotNull PsiElement element, final int type) {
        final var token = find(element, String.format("/*/%s", JBPLLexer.VOCABULARY.getLiteralName(type)));
        if (token.isPresent()) {
            return true;
        }
        if (!(element instanceof JBPLPsiLeafNode node)) {
            return false;
        }
        if (!(node.getNode().getElementType() instanceof TokenIElementType elementType)) {
            return false;
        }
        return elementType.getANTLRTokenType() == type;
    }

    public static @NotNull String toSingleLine(final @NotNull PsiElement element) {
        return element.getText().replaceAll("[\\n\\s]+", " ").trim();
    }

    public static @NotNull Stream<? extends PsiElement> findAll(final @NotNull PsiElement element,
                                                                final @NotNull String path) {
        return XPath.findAll(JBPLanguage.INSTANCE, element, path).stream();
    }

    @SuppressWarnings("unchecked")
    public static @NotNull Optional<PsiElement> find(final @NotNull PsiElement element, final @NotNull String path) {
        return (Optional<PsiElement>) XPath.findAll(JBPLanguage.INSTANCE, element, path).stream().findFirst();
    }

    public static <E extends PsiElement> @NotNull Optional<E> find(final @NotNull PsiElement element,
                                                                   final @NotNull String path,
                                                                   final @NotNull Class<E> type) {
        // @formatter:off
        return XPath.findAll(JBPLanguage.INSTANCE, element, path).stream()
            .filter(type::isInstance)
            .map(type::cast)
            .findFirst();
        // @formatter:on
    }
}
