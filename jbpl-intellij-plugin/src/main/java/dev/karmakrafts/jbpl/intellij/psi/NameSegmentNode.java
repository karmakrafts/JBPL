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

package dev.karmakrafts.jbpl.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import dev.karmakrafts.jbpl.intellij.reference.ClassTypeReference;
import dev.karmakrafts.jbpl.intellij.reference.FieldReference;
import dev.karmakrafts.jbpl.intellij.reference.PackageReference;
import dev.karmakrafts.jbpl.intellij.util.JavaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class NameSegmentNode extends JBPLPsiNode {
    public NameSegmentNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull String getName() {
        return getText();
    }

    @Override
    public @Nullable PsiReference getReference() {
        if (!JavaUtils.isJavaAvailable()) {
            return null; // When Java is not available, we cannot create any Java references
        }
        final var parent = getParent();
        if (!(parent instanceof ClassTypeNode classTypeNode)) {
            final var fieldSignature = (FieldSignatureNode) PsiTreeUtil.findFirstParent(this,
                FieldSignatureNode.class::isInstance);
            if (fieldSignature != null) {
                return new FieldReference(this);
            }
            return null;
        }
        final var children = List.of(classTypeNode.getChildren());
        final var index = children.indexOf(this);
        if (index == children.size() - 2) {
            // This is the actual class name, so resolve to class
            return new ClassTypeReference(this);
        }
        // Otherwise this is a package segment
        return new PackageReference(this);
    }
}
