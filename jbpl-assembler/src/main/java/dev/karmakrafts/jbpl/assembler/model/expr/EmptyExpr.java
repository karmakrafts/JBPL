package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.model.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.decl.FieldDecl;

/**
 * An instance of this may be passed to the receiver of
 * any subtype of {@link AbstractReceiverExpr}, to denote
 * that the receiver is the top-level file itself.
 * <p>
 * It may also be passed as an initializer to {@link FieldDecl}
 * to denote that the field uses default initialization.
 */
public final class EmptyExpr extends AbstractElement implements Expr {
}
