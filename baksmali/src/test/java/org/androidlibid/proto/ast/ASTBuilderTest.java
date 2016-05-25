package org.androidlibid.proto.ast;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.Adaptors.Format.InstructionMethodItem;
import org.jf.baksmali.Adaptors.MethodDefinitionImpl;
import org.jf.baksmali.Adaptors.MethodItem;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTBuilderTest {

    MethodDefinitionImpl methodDefinitionImpl;
    List<MethodItem> methodItems;
    ArrayList<MethodParameter> methodParameters;
    
    private static final Logger LOGGER = LogManager.getLogger(ASTBuilderTest.class);
    
    @Test
    public void testCreateEmptyAST() throws IOException {
        
        boolean noParameterRegister = false;
        int accessFlags = 0;
        int methodRegisterCount = 0;
        
        ASTBuilder methodDefinition = createASTMethodDefinition(accessFlags, methodRegisterCount, noParameterRegister);
        
        Node method = methodDefinition.buildAST();
        
        assert(method.getType().equals(NodeType.METHOD));
        assert(method.getParent() == null);
    }
    
    @Test
    public void testCreateASTofMethod() throws IOException {
        
        boolean noParameterRegister = false;
        int accessFlags = 0;
        int methodRegisterCount = 4;
        
        ASTBuilder methodDefinition = createASTMethodDefinition(accessFlags, methodRegisterCount, noParameterRegister);
        addInstruction(Opcode.INVOKE_DIRECT,  1, 0);
        addInstruction(Opcode.INVOKE_VIRTUAL, 5, 0,1,2,3,4);
        addInstruction(Opcode.INVOKE_DIRECT,  5, 5,6,7,8,9);
        addInstruction(Opcode.INVOKE_DIRECT,  0);
        addParameter("I");
        addParameter("Lorg/pckgA/subpckgA/classB;");
        addParameter("Lorg/pckgA/subpckgB/classB;");
        addParameter("[[Z");
        
        Node method = methodDefinition.buildAST();

        assert(method.getType().equals(NodeType.METHOD));
        assert(method.getChildren().size() == 5);
        
        Node signature = method.getChildren().get(0);
        assert(signature.getChildren().isEmpty());
        assert(signature.getType() == NodeType.SIGNATURE);
        assert(signature.getSignature().equals("IOE[[Z:V"));
        
        Node direct1 = method.getChildren().get(1);
        assert(direct1.getType().equals(NodeType.DIRECT));
        assert(direct1.getChildren().size() == 1);
        assert(direct1.getChildren().get(0).getType().equals(NodeType.LOCAL));
        
        Node virtual2 = method.getChildren().get(2);
        assert(virtual2.getType().equals(NodeType.VIRTUAL));
        assert(virtual2.getChildren().size() == 5);
        assert(virtual2.getChildren().get(0).getType().equals(NodeType.LOCAL));
        assert(virtual2.getChildren().get(1).getType().equals(NodeType.LOCAL));
        assert(virtual2.getChildren().get(2).getType().equals(NodeType.PARAMETER));
        assert(virtual2.getChildren().get(3).getType().equals(NodeType.PARAMETER));
        assert(virtual2.getChildren().get(4).getType().equals(NodeType.PARAMETER));
        
        Node direct3 = method.getChildren().get(3);
        assert(direct3.getType().equals(NodeType.DIRECT));
        assert(direct3.getChildren().size() == 5);
        for(Node direct3args : direct3.getChildren()) {
            assert(direct3args.getType().equals(NodeType.PARAMETER));
        }
        
        Node direct4 = method.getChildren().get(4);
        assert(direct4.getType().equals(NodeType.DIRECT));
        assert(direct4.getChildren().isEmpty());
        
    } 

    private ASTBuilder createASTMethodDefinition(int accessFlags, int methodRegisterCount, boolean noParameterRegister) {        
        methodDefinitionImpl = mock(MethodDefinitionImpl.class);

        Method method = mock(Method.class);
        when(method.getAccessFlags()).thenReturn(accessFlags);
        when(method.getReturnType()).thenReturn("V");
        when(methodDefinitionImpl.getMethod()).thenReturn(method);
        
        MethodImplementation methodImplementation = mock(MethodImplementation.class);
        when(methodImplementation.getRegisterCount()).thenReturn(methodRegisterCount);
        when(methodDefinitionImpl.getMethodImpl()).thenReturn(methodImplementation);
        
        methodItems = new ArrayList<>();
        when(methodDefinitionImpl.getMethodItems()).thenReturn(methodItems);
        
        methodParameters = new ArrayList<>();
        when(methodDefinitionImpl.getMethodParameters())
                .thenAnswer(new CreateImmutableCopyOfMethodParameters());
        
        return new ASTBuilder(methodDefinitionImpl, noParameterRegister, "Lorg/pckgA/subpckgA/classA;");
    }
    
    private void addInstruction(Opcode opcode, int registerCount, int... registerValues) {
        InstructionMethodItem instructionMethodItem = mock(InstructionMethodItem.class);
        FiveRegisterInstruction instruction = mock(FiveRegisterInstruction.class);
        
        when(instructionMethodItem.getInstruction()).thenReturn(instruction);
        when(instruction.getOpcode()).thenReturn(opcode);
        when(instruction.getRegisterCount()).thenReturn(registerCount);
        
        if(registerValues.length >= 1) when(instruction.getRegisterC()).thenReturn(registerValues[0]);
        if(registerValues.length >= 2) when(instruction.getRegisterD()).thenReturn(registerValues[1]);
        if(registerValues.length >= 3) when(instruction.getRegisterE()).thenReturn(registerValues[2]);
        if(registerValues.length >= 4) when(instruction.getRegisterF()).thenReturn(registerValues[3]);
        if(registerValues.length  > 4) when(instruction.getRegisterG()).thenReturn(registerValues[4]);
        
        methodItems.add(instructionMethodItem);
    }
    
    private void addParameter(String type) {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getType()).thenReturn(type);
        methodParameters.add(parameter);
    }

    private class CreateImmutableCopyOfMethodParameters implements Answer<ImmutableList<MethodParameter>> {
           @Override
           public ImmutableList<MethodParameter> answer(InvocationOnMock invocation) throws Throwable {
               return ImmutableList.copyOf(methodParameters);
           }
    }
    
}
