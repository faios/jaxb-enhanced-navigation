package de.foursoft.harness.xml.xjc.plugin;

import javax.xml.bind.Marshaller;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

public class EmptyListHandlerPlugin extends Plugin {

    @Override
    public String getOptionName() {
        return "Xnull-empty-lists";
    }

    @Override
    public String getUsage() {
        return "to be defined!";

    }

    @Override
    public boolean run(final Outline outline, final Options opt, final ErrorHandler errorHandler) throws SAXException {
        outline.getClasses()
                .forEach(c -> createBeforeMarshall(outline.getCodeModel(), c));
        return true;
    }

    public void createBeforeMarshall(final JCodeModel jCodeModel, final ClassOutline outline) {
        // boolean beforeMarshal(Marshaller)
        final JDefinedClass targetClass = outline.implClass;
        // targetClass.method(JMod.PUBLIC, baseType, getterName)
        final JMethod method = targetClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "beforeMarshal");

        final JVar marshaller = method.param(Marshaller.class, "marshaller");

        final JBlock body = method.body();
        if (outline.getSuperClass() != null) {
            body.invoke(JExpr._super(), "beforeMarshal")
                    .arg(marshaller);
        }

        for (final FieldOutline f : outline.getDeclaredFields()) {
            createEmptyCheck(f, body);
        }

        body._return(JExpr.TRUE);

    }

    private void createEmptyCheck(final FieldOutline fieldOutline, final JBlock body) {
        final CPropertyInfo propertyInfo = fieldOutline.getPropertyInfo();
        if (propertyInfo.isCollection()) {
            final JFieldRef fieldRef = JExpr.ref(propertyInfo.getName(false));
            final JBlock conditional = body._if(fieldRef.ne(JExpr._null())
                    .cand(fieldRef.invoke("isEmpty")))
                    ._then();
            conditional.assign(fieldRef, JExpr._null());
        }

    }

}
