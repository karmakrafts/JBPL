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

package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldNode;

import java.util.EnumSet;
import java.util.stream.Collectors;

public final class FieldDecl extends AbstractExprContainer implements Declaration {
    public static final int SIGNATURE_INDEX = 0;
    public static final int INITIALIZER_INDEX = 1;

    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);

    public FieldDecl() {
        addExpression(ConstExpr.unit()); // Signature
        addExpression(ConstExpr.unit()); // Initializer
    }

    public @NotNull Expr getInitializer() {
        return getExpressions().get(INITIALIZER_INDEX);
    }

    public void setInitializer(final @NotNull Expr initializer) {
        getInitializer().setParent(null);
        initializer.setParent(this);
        getExpressions().set(INITIALIZER_INDEX, initializer);
    }

    public @NotNull Expr getSignature() {
        return getExpressions().get(SIGNATURE_INDEX);
    }

    public void setSignature(final @NotNull Expr signature) {
        getSignature().setParent(null);
        signature.setParent(this);
        getExpressions().set(SIGNATURE_INDEX, signature);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        if (accessModifiers.stream().noneMatch(mod -> mod.applicableToField)) {
            final var mods = accessModifiers.stream().map(AccessModifier::toString).collect(Collectors.joining(" "));
            final var message = String.format("Modifiers '%s' are not applicable to field", mods);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        final var signature = getSignature().evaluateAs(context, FieldSignatureExpr.class);
        final var owner = signature.getFieldOwner().evaluateAs(context, ClassType.class);
        final var modifier = AccessModifier.combine(accessModifiers);
        final var name = signature.getFieldName().evaluateAs(context, String.class);
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        final var initializer = getInitializer();
        final var initializerType = initializer.getType(context);
        final var fieldType = signature.getFieldType().evaluateAs(context, Type.class);
        // @formatter:off
        // Handle cases where the initializer never gets updated from unit-expr by creating default value from field type
        final var initialValue = initializerType == BuiltinType.VOID
            ? copySourcesTo(fieldType.createDefaultValue(context)).evaluateAsConstAndMaterialize(context)
            : initializer.evaluateAsConstAndMaterialize(context);
        // @formatter:on
        final var node = new FieldNode(context.bytecodeApi, modifier, name, descriptor, descriptor, initialValue);
        context.addField(owner.name(), node);
    }

    @Override
    public @NotNull FieldDecl copy() {
        final var field = copyParentAndSourceTo(new FieldDecl());
        field.setInitializer(getInitializer().copy());
        field.setSignature(getSignature().copy());
        field.accessModifiers.addAll(accessModifiers);
        field.addExpressions(getExpressions().stream().map(Expr::copy).toList());
        return copyParentAndSourceTo(field);
    }

    @Override
    public @NotNull String toString() {
        // @formatter:off
        final var mods = accessModifiers.stream()
            .map(AccessModifier::toString)
            .collect(Collectors.joining(" ", "", " "));
        // @formatter:on
        return String.format("%sfield %s = %s", mods, getSignature(), getInitializer());
    }
}
