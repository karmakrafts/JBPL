package dev.karmakrafts.jbpl.assembler.model.element;

import dev.karmakrafts.jbpl.assembler.eval.Evaluable;
import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.source.SourceOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceRange;
import dev.karmakrafts.jbpl.assembler.util.Copyable;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

public interface Element extends SourceOwner, Evaluable, Copyable<Element> {
    default <E extends Element> @NotNull E copyParentAndSourceTo(final @NotNull E element) {
        element.setParent(getParent());
        return copySourcesTo(element);
    }

    @Nullable ElementContainer getParent();

    void setParent(final @Nullable ElementContainer parent);

    default void accept(final @NotNull ElementVisitor visitor) {
        visitor.visitElement(this);
    }

    default @NotNull Element transform(final @NotNull ElementVisitor visitor) {
        return visitor.visitElement(this);
    }

    default @NotNull AssemblyFile getContainingFile() {
        final var parentStack = new Stack<Element>();
        parentStack.push(this);
        while (!parentStack.isEmpty()) {
            final var currentParent = parentStack.pop();
            if (currentParent instanceof AssemblyFile file) {
                return file;
            }
            final var grandParent = currentParent.getParent();
            if (grandParent == null) {
                break;
            }
            parentStack.push(grandParent);
        }
        throw new IllegalStateException("Could not find parent file for element");
    }

    default @NotNull SourceRange getSourceRange() {
        return getContainingFile().getSourceRange(getTokenRange());
    }

    default @NotNull List<Token> getTokens() {
        return getContainingFile().getTokens(getTokenRange());
    }
}
