// Copyright (c) 2002-2014 JavaMOP Team. All Rights Reserved.
package javamop.parser.ast.aspectj;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import javamop.parser.ast.visitor.BaseVisitor;
import javamop.parser.ast.visitor.MOPVoidVisitor;

public class BaseTypePattern extends TypePattern {
    
    public BaseTypePattern(TokenRange tokenRange, String op) {
        super(tokenRange, op);
    }
    
    public <A> void accept(VoidVisitor<A> v, A arg) {
        if (v instanceof MOPVoidVisitor) {
            ((MOPVoidVisitor)v).visit(this, arg);
        }
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        if (v instanceof BaseVisitor) {
            return ((BaseVisitor<R, A>) v).visit(this, arg);
        } else {
            return null;
        }
    }

}
