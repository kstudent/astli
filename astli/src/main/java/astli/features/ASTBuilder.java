/*
 * [The "BSD licence"]
 * Copyright (c) 2010 Ben Gruver (JesusFreke)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package astli.features;

import com.google.common.collect.ImmutableList;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.util.TypeUtils;
import java.io.IOException;
import java.util.*;
import astli.utils.SmaliNameConverter;
import org.jf.baksmali.Adaptors.Format.InstructionMethodItem;
import org.jf.baksmali.Adaptors.MethodDefinition;
import org.jf.baksmali.Adaptors.MethodItem;
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction;

/**
 * Facade class for MethodDefinitionImpl
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public class ASTBuilder {

    private final MethodDefinition methodDefinition;
    private final boolean noParameterRegisters;
    private final String currentClassType;
    private final String currentPackage;
    
    private final static Map<Opcode, NodeType> OPCODE_TO_NODETYPE_MAP;
    
    static {
        OPCODE_TO_NODETYPE_MAP = new HashMap<>();
        OPCODE_TO_NODETYPE_MAP.put(Opcode.INVOKE_DIRECT , NodeType.DRC);
        OPCODE_TO_NODETYPE_MAP.put(Opcode.INVOKE_VIRTUAL, NodeType.VRT);
    }

    public ASTBuilder(MethodDefinition methodDefinition, 
            boolean noParameterRegisters, String smaliClassType) {
        this.methodDefinition     = methodDefinition;
        this.noParameterRegisters = noParameterRegisters;
        this.currentClassType     = SmaliNameConverter.convertTypeFromSmali(smaliClassType);
        this.currentPackage       = SmaliNameConverter.extractPackageNameFromClassName(currentClassType);
    }
    
    public Node buildAST() throws IOException {
        
        Node root = new Node(NodeType.MTH);
        
        int tmpParameterRegisterCount = 0;
        if (!AccessFlags.STATIC.isSet(getMethod().getAccessFlags())) {
            tmpParameterRegisterCount++;
        }

        tmpParameterRegisterCount += countParameterRegisters(); 
        final int parameterRegisterCount = tmpParameterRegisterCount; 
        
        String signature = createSignature();
        root.addChild(new Node(signature));

        getMethodItems().stream()
            .filter(item -> item instanceof InstructionMethodItem)
            .map(InstructionMethodItem.class::cast)
            .map(item -> item.getInstruction())
            .filter(instruction -> instruction.getOpcode().format == Format.Format35c)
            .filter(instruction -> OPCODE_TO_NODETYPE_MAP.containsKey(instruction.getOpcode()))
            .filter(instruction -> instruction instanceof FiveRegisterInstruction)
            .map(instruction -> (FiveRegisterInstruction) instruction)
            .map(ins -> createInstructionNode(ins, parameterRegisterCount))
            .forEach(child -> root.addChild(child));
                
        return root;
        
    }
    
    private Node createInstructionNode(FiveRegisterInstruction ins, int parameterRegisterCount) {
        Node child = new Node(OPCODE_TO_NODETYPE_MAP.get(ins.getOpcode()));
        int mrc = getMethodImpl().getRegisterCount();
        int irc = ins.getRegisterCount();
        if(irc >= 1) child.addChild(createRegisterNode(ins.getRegisterC(), mrc, parameterRegisterCount));
        if(irc >= 2) child.addChild(createRegisterNode(ins.getRegisterD(), mrc, parameterRegisterCount));
        if(irc >= 3) child.addChild(createRegisterNode(ins.getRegisterE(), mrc, parameterRegisterCount));
        if(irc >= 4) child.addChild(createRegisterNode(ins.getRegisterF(), mrc, parameterRegisterCount));
        if(irc >= 5) child.addChild(createRegisterNode(ins.getRegisterG(), mrc, parameterRegisterCount));
        return child;
    }
    
    
    private Method getMethod() {
        return methodDefinition.getMethod();
    }

    private MethodImplementation getMethodImpl() {
        return methodDefinition.getMethodImpl();
    }

    private ImmutableList<MethodParameter> getMethodParameters() {
        return methodDefinition.getMethodParameters();
    }

    private List<MethodItem> getMethodItems() {
        return methodDefinition.getMethodItems();
    }
    
    private String getMethodReturnType() {
        return methodDefinition.getMethod().getReturnType();
    }

    private Node createRegisterNode(int register, int registerCount, int parameterRegisterCount) {
        if (!noParameterRegisters) {
            if (register >= registerCount - parameterRegisterCount) {
                return new Node(NodeType.PAR);
            }
        }
        return new Node(NodeType.LOC);
    }

    private int countParameterRegisters() {
        
        int parameterRegisterCount = 0; 
        
        for (MethodParameter parameter: getMethodParameters()) {
            parameterRegisterCount++;
            if (TypeUtils.isWideType(parameter.getType())) {
                parameterRegisterCount++;
            }
        }
        
        return parameterRegisterCount; 
    }

    private String createSignature() {
        StringBuilder signature = new StringBuilder();
        
        for (MethodParameter parameter: getMethodParameters()) {
            signature.append(prepareType(parameter.getType()));
        }
        
        signature.append(":").append(prepareType(getMethodReturnType()));
        
       return signature.toString();
    }
    
    // returns primitive type letters (see
    // https://github.com/JesusFreke/smali/wiki/TypesMethodsAndFields)
    // or one of the following letters
    // E - Type that is not located in the same package (External)
    // O - Type that is located in the same package 
    // T - Current Class Type (This)
    private String prepareType(String smaliType) {
        int arrayDimensions = 0;
        
        String typeWithoutBrackets = smaliType;
        
        while (typeWithoutBrackets.startsWith("[")) {
            arrayDimensions++;
            typeWithoutBrackets = typeWithoutBrackets.substring(1);
        }
        
        if(SmaliNameConverter.isPrimitiveSmaliType(typeWithoutBrackets)) {
            return (typeWithoutBrackets.equals("V")) ? "" : smaliType ; 
        }
            
        String classOfType   = SmaliNameConverter.convertTypeFromSmali(typeWithoutBrackets);
        String packageOfType = SmaliNameConverter.extractPackageNameFromClassName(classOfType);

        StringBuilder type = new StringBuilder();

        for(int i = 0; i < arrayDimensions; i++) {
            type.append("[");
        }

        boolean isCurrentClassObject = (classOfType.equals(currentClassType));            
        boolean isInternalObject = (packageOfType.equals(currentPackage));            
        
        char typeChar =  isCurrentClassObject? 'T' : 
                         isInternalObject?     'O' : 'E' ;
        
        type.append(typeChar);

        return type.toString();
    }
}