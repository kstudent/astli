package astli.extraction;

import astli.pojo.NodeType;
import astli.extraction.MethodASTBuilder;
import astli.extraction.Node;
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
    ArrayList<MethodParameter> methodParameters = new ArrayList<>();
    
    private static final Logger LOGGER = LogManager.getLogger(ASTBuilderTest.class);
    
    @Test
    public void testCreateEmptyAST() throws IOException {
        
        boolean noParameterRegister = false;
        int accessFlags = 0;
        int methodRegisterCount = 0;
        String classType = "Lorg/pckg/clzz;";
        
        MethodASTBuilder astBuilder = createASTBuilder(accessFlags, 
                methodRegisterCount, noParameterRegister, classType);
        
        Node method = astBuilder.build();
        
        assert(method.getType().equals(NodeType.MTH));
    }
    
    @Test
    public void testCreateASTwithLocalAndParameterRegisters() throws IOException {
//        boolean noParameterRegister = false;
//        int localRegisterCount = 3; 
//        int accessFlags = 0;
//        String classType = "Lorg/pckg/clzz;";
//        
//        addParameter("I");
//        addParameter("I");
//        addParameter("I");
//        
//        ASTBuilder astBuidler = createASTBuilder(accessFlags, localRegisterCount, 
//                noParameterRegister, classType);
//        
//        addInstruction(Opcode.INVOKE_DIRECT,  0,1,2);
//        addInstruction(Opcode.INVOKE_VIRTUAL, 3,4,5);
//        
//        Node method = astBuidler.buildAST();
//        
//        assert(method.getType().equals(NodeType.MTH));
//        assert(method.getChildren().size() == 3);
//        
//        Node signature = method.getChildren().get(0);
//        assert(signature.getChildren().isEmpty());
//        assert(signature.getType() == NodeType.SGN);
//        assert(signature.getSignature().equals("III:V"));
//        
//        Node direct = method.getChildren().get(1);
//        assert(direct.getType().equals(NodeType.DRC));
//        assert(direct.getChildren().size() == 3);
//        assert(direct.getChildren().get(0).getType().equals(NodeType.LOC));
//        assert(direct.getChildren().get(1).getType().equals(NodeType.LOC));
//        assert(direct.getChildren().get(2).getType().equals(NodeType.LOC));
//        
//        Node virtual = method.getChildren().get(2);
//        assert(virtual.getType().equals(NodeType.VRT));
//        assert(virtual.getChildren().size() == 3);
//        assert(virtual.getChildren().get(0).getType().equals(NodeType.PAR));
//        assert(virtual.getChildren().get(1).getType().equals(NodeType.PAR));
//        assert(virtual.getChildren().get(2).getType().equals(NodeType.PAR));
        
    }
    
    @Test
    public void testCreateASTwithArrayParameters() throws IOException {
//        boolean noParameterRegister = false;
//        int localRegisterCount = 0; 
//        int accessFlags = 0;
//        String classType = "Lorg/pckg/clzz;";
//        
//        addParameter("[Z");
//        addParameter("[[" + classType);
//        
//        ASTBuilder astBuidler = createASTBuilder(accessFlags, localRegisterCount, 
//                noParameterRegister, classType);
//        
//        Node method = astBuidler.buildAST();
//        
//        assert(method.getType().equals(NodeType.MTH));
//        assert(method.getChildren().size() == 1);
//        
//        Node signature = method.getChildren().get(0);
//        assert(signature.getChildren().isEmpty());
//        assert(signature.getType() == NodeType.SGN);
//        assert(signature.getSignature().equals("[Z[[T:V"));
    }
    
    @Test
    public void testCreateASTWithInternalExternalTypesInSignature() throws IOException {
        
        boolean noParameterRegister = false;
        int localRegisterCount = 3; 
        int accessFlags = 0;
        String currentClassType = "Lorg/pckg/currentClazz;";
        String internalType     = "Lorg/pckg/internalClazz;";
        String externalType     = "Lorg/differentPckg/otherClazz;";
        String returnType       = externalType;
        
        addParameter(currentClassType);
        addParameter(internalType);
        addParameter(externalType);
        
        MethodASTBuilder methodDefinition = createASTBuilder(accessFlags, 
                localRegisterCount, noParameterRegister, currentClassType, returnType);
        
        Node method = methodDefinition.build();

        assert(method.getType().equals(NodeType.MTH));
        assert(method.getChildren().size() == 1);
        
        Node signature = method.getChildren().get(0);
        assert(signature.getChildren().isEmpty());
        assert(signature.getType() == NodeType.SGN);
        assert(signature.getSignature().equals("TOE:E"));
        
    } 

    private MethodASTBuilder createASTBuilder(int accessFlags, 
            int localRegisterCount, boolean noParameterRegister, 
            String classType) {
        return createASTBuilder(accessFlags, localRegisterCount, 
                noParameterRegister, classType, "V");
    }
    
    private MethodASTBuilder createASTBuilder(int accessFlags, 
            int localRegisterCount, boolean noParameterRegister, 
            String classType, String returnType) {        
        
        int registerCount = localRegisterCount + methodParameters.size() + 1; 
        
        methodDefinitionImpl = mock(MethodDefinitionImpl.class);

        Method method = mock(Method.class);
        when(method.getAccessFlags()).thenReturn(accessFlags);
        when(method.getReturnType()).thenReturn(returnType);
        when(methodDefinitionImpl.getMethod()).thenReturn(method);
        
        MethodImplementation methodImplementation = mock(MethodImplementation.class);
        when(methodImplementation.getRegisterCount()).thenReturn(registerCount);
        when(methodDefinitionImpl.getMethodImpl()).thenReturn(methodImplementation);
        
        methodItems = new ArrayList<>();
        when(methodDefinitionImpl.getMethodItems()).thenReturn(methodItems);
        
        when(methodDefinitionImpl.getMethodParameters())
                .thenAnswer(new CreateImmutableCopyOfMethodParameters());
        
        return new MethodASTBuilder(methodDefinitionImpl, noParameterRegister, classType);
    }
    
    private void addInstruction(Opcode opcode, int... registerValues) {
        InstructionMethodItem instructionMethodItem = mock(InstructionMethodItem.class);
        FiveRegisterInstruction instruction = mock(FiveRegisterInstruction.class);
        
        int registerCount = registerValues.length;
        
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
