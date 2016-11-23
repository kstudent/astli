package org.jf.baksmali.Adaptors;

import java.io.IOException;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.util.IndentingWriter;

/**
 *
 * @author chri
 */
public interface ClassDefinition {

    ClassDef getClassDef();

    baksmaliOptions getOptions();

    boolean hadValidationErrors();

    void writeTo(IndentingWriter writer) throws IOException;
    
}
