package org.androidlibid.proto;

import org.jf.baksmali.baksmaliOptions;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ASTRegisterNodeCreator {
    @Nonnull private final baksmaliOptions options;
    private final int registerCount;
    private final int parameterRegisterCount;

    public ASTRegisterNodeCreator(@Nonnull baksmaliOptions options, int registerCount, int parameterRegisterCount) {
        this.options = options;
        this.registerCount = registerCount;
        this.parameterRegisterCount = parameterRegisterCount;
    }

    public Node createRegisterNode(int register) throws IOException {
        if (!options.noParameterRegisters) {
            if (register >= registerCount - parameterRegisterCount) {
                return new Node(NodeType.PARAMETER);
            }
        }
        return new Node(NodeType.LOCAL);
    }
}

