package org.jf.baksmali.Adaptors;

import java.io.IOException;
import org.jf.baksmali.baksmaliOptions;
import org.jf.util.IndentingWriter;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface RegisterFormatter {

    baksmaliOptions getOptions();

    int getParameterRegisterCount();

    int getRegisterCount();

    /**
     * Write out the register range value used by Format3rc. If baksmali.noParameterRegisters is true, it will always
     * output the registers in the v<n> format. But if false, then it will check if *both* registers are parameter
     * registers, and if so, use the p<n> format for both. If only the last register is a parameter register, it will
     * use the v<n> format for both, otherwise it would be confusing to have something like {v20 .. p1}
     * @param writer the <code>IndentingWriter</code> to write to
     * @param startRegister the first register in the range
     * @param lastRegister the last register in the range
     */
    void writeRegisterRange(IndentingWriter writer, int startRegister, int lastRegister) throws IOException;

    /**
     * Writes a register with the appropriate format. If baksmali.noParameterRegisters is true, then it will always
     * output a register in the v<n> format. If false, then it determines if the register is a parameter register,
     * and if so, formats it in the p<n> format instead.
     *
     * @param writer the <code>IndentingWriter</code> to write to
     * @param register the register number
     */
    void writeTo(IndentingWriter writer, int register) throws IOException;
    
}
