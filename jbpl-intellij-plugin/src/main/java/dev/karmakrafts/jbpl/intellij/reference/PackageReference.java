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

package dev.karmakrafts.jbpl.intellij.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import dev.karmakrafts.jbpl.intellij.psi.ClassTypeNode;
import dev.karmakrafts.jbpl.intellij.psi.NameSegmentNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class PackageReference extends PsiReferenceBase<NameSegmentNode> {
    public PackageReference(final @NotNull NameSegmentNode element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    @Override
    public @Nullable PsiElement resolve() {
        // Resolve package FQN to this point in the selection
        var name = myElement.getName();
        final var parent = myElement.getParent();
        if (parent instanceof ClassTypeNode) {
            final var children = List.of(parent.getChildren());
            if (children.isEmpty()) {
                return null;
            }
            final var index = children.indexOf(myElement);
            // @formatter:off
            name = children.subList(0, index + 1).stream()
                .filter(NameSegmentNode.class::isInstance)
                .map(NameSegmentNode.class::cast)
                .map(NameSegmentNode::getName)
                .collect(Collectors.joining("."));
            // @formatter:on
        }
        final var project = myElement.getProject();
        final var facade = JavaPsiFacade.getInstance(project);
        return facade.findPackage(name);
    }
}
