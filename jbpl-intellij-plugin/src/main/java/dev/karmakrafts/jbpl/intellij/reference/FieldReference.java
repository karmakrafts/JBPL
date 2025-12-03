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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import dev.karmakrafts.jbpl.intellij.psi.FieldSignatureNode;
import dev.karmakrafts.jbpl.intellij.psi.NameSegmentNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class FieldReference extends PsiReferenceBase<NameSegmentNode> {
    public FieldReference(final @NotNull NameSegmentNode element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    @Override
    public Object @NotNull [] getVariants() {
        final var signature = (FieldSignatureNode) PsiTreeUtil.findFirstParent(myElement,
            FieldSignatureNode.class::isInstance);
        if (signature == null) {
            return new Object[0];
        }
        final var project = myElement.getProject();
        final var facade = JavaPsiFacade.getInstance(project);
        final var scope = GlobalSearchScope.allScope(project);
        final var ownerName = signature.getOwnerName();
        final var owner = facade.findClass(ownerName, scope);
        if (owner == null) {
            return new Object[0];
        }
        final var name = myElement.getText();
        // @formatter:off
        return Arrays.stream(owner.getFields())
            .filter(field -> field.getName().equals(name))
            .toArray(Object[]::new);
        // @formatter:on
    }

    @Override
    public @Nullable PsiElement resolve() {
        final var elements = getVariants();
        if (elements.length == 0) {
            return null;
        }
        return (PsiElement) elements[0];
    }
}
