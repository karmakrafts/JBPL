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
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public final class ClassDecl extends AbstractExprContainer implements Declaration {
    public static final int TYPE_INDEX = 0;
    public static final int SUPER_TYPE_INDEX = 1;
    public static final int INTERFACES_INDEX = 2;

    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);

    public ClassDecl(final @NotNull Expr type) {
        addExpression(type);
        addExpression(ConstExpr.unit());
    }

    public @NotNull List<Expr> getInterfaces() {
        final var expressions = getExpressions();
        return expressions.subList(INTERFACES_INDEX, expressions.size());
    }

    public void addInterfaces(final @NotNull Collection<Expr> interfaces) {
        addExpressions(interfaces);
    }

    public @NotNull Expr getSuperType() {
        return getExpressions().get(SUPER_TYPE_INDEX);
    }

    public void setSuperType(final @NotNull Expr superType) {
        superType.setParent(this);
        getExpressions().set(SUPER_TYPE_INDEX, superType);
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        type.setParent(this);
        getExpressions().set(TYPE_INDEX, type);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var clazz = new ClassNode(context.bytecodeApi);
        clazz.name = getType().evaluateAs(context, Type.class).materialize(context).getInternalName();
        var superType = getSuperType();
        if (superType.isUnit()) {
            // If there's no explicit super type, we default to object
            superType = ConstExpr.of(BuiltinType.OBJECT, superType.getTokenRange());
        }
        clazz.superName = superType.evaluateAs(context, Type.class).materialize(context).getInternalName();
        // @formatter:off
        clazz.interfaces.addAll(getInterfaces().stream()
            .map(ExceptionUtils.unsafeFunction(expr -> expr.evaluateAs(context, Type.class).materialize(context).getInternalName()))
            .toList());
        // @formatter:on
        if (accessModifiers.stream().noneMatch(mod -> mod.applicableToClass)) {
            final var mods = accessModifiers.stream().map(AccessModifier::toString).collect(Collectors.joining(" "));
            final var message = String.format("Modifiers '%s' are not applicable to class", mods);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        clazz.access = AccessModifier.combine(accessModifiers);
        context.addClass(clazz);
    }

    @Override
    public @NotNull ClassDecl copy() {
        final var clazz = copyParentAndSourceTo(new ClassDecl(getType().copy()));
        clazz.accessModifiers.addAll(accessModifiers);
        return clazz;
    }
}
