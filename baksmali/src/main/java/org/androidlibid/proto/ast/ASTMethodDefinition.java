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
package org.androidlibid.proto.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jf.baksmali.Adaptors.Debug.DebugMethodItem;
import org.jf.baksmali.Adaptors.Format.InstructionMethodItemFactory;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.analysis.AnalysisException;
import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.analysis.MethodAnalyzer;
import org.jf.dexlib2.dexbacked.DexBackedDexFile.InvalidItemIndex;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31t;
import org.jf.dexlib2.util.InstructionOffsetMap;
import org.jf.dexlib2.util.InstructionOffsetMap.InvalidInstructionOffset;
import org.jf.dexlib2.util.ReferenceUtil;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.jf.dexlib2.util.SyntheticAccessorResolver.AccessedMember;
import org.jf.dexlib2.util.TypeUtils;
import org.jf.util.ExceptionWithContext;
import org.jf.util.IndentingWriter;
import org.jf.util.SparseIntArray;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import org.jf.baksmali.Adaptors.BlankMethodItem;
import org.jf.baksmali.Adaptors.CatchMethodItem;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.Adaptors.CommentMethodItem;
import org.jf.baksmali.Adaptors.CommentedOutMethodItem;
import org.jf.baksmali.Adaptors.Format.InstructionMethodItem;
import org.jf.baksmali.Adaptors.LabelMethodItem;
import org.jf.baksmali.Adaptors.MethodDefinition;
import org.jf.baksmali.Adaptors.MethodItem;
import org.jf.baksmali.Adaptors.PostInstructionRegisterInfoMethodItem;
import org.jf.baksmali.Adaptors.PreInstructionRegisterInfoMethodItem;
import org.jf.baksmali.Adaptors.RegisterFormatter;
import org.jf.baksmali.Adaptors.SyntheticAccessCommentMethodItem;
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction;

public class ASTMethodDefinition implements MethodDefinition {
    @Nonnull private final ClassDefinition classDef;
    @Nonnull private final Method method;
    @Nonnull private final MethodImplementation methodImpl;
    @Nonnull private final ImmutableList<Instruction> instructions;
    @Nonnull private final List<Instruction> effectiveInstructions;
    @Nonnull private final ImmutableList<MethodParameter> methodParameters;
    private RegisterFormatter registerFormatter;

    @Override
    public ClassDefinition getClassDef() {
        return classDef;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public MethodImplementation getMethodImpl() {
        return methodImpl;
    }

    public ImmutableList<Instruction> getInstructions() {
        return instructions;
    }

    public List<Instruction> getEffectiveInstructions() {
        return effectiveInstructions;
    }

    public ImmutableList<MethodParameter> getMethodParameters() {
        return methodParameters;
    }

    public RegisterFormatter getRegisterFormatter() {
        return registerFormatter;
    }
    
    @Nonnull private final MethodDefinition.LabelCache labelCache = new MethodDefinition.LabelCache();
    @Nonnull private final SparseIntArray packedSwitchMap;
    @Nonnull private final SparseIntArray sparseSwitchMap;
    @Nonnull private final InstructionOffsetMap instructionOffsetMap;

    public ASTMethodDefinition(@Nonnull ClassDefinition classDef, @Nonnull Method method,
                            @Nonnull MethodImplementation methodImpl) {
        this.classDef = classDef;
        this.method = method;
        this.methodImpl = methodImpl;

        try {
            //TODO: what about try/catch blocks inside the dead code? those will need to be commented out too. ugh.

            instructions = ImmutableList.copyOf(methodImpl.getInstructions());
            methodParameters = ImmutableList.copyOf(method.getParameters());

            effectiveInstructions = Lists.newArrayList(instructions);

            packedSwitchMap = new SparseIntArray(0);
            sparseSwitchMap = new SparseIntArray(0);
            instructionOffsetMap = new InstructionOffsetMap(instructions);

            int endOffset = instructionOffsetMap.getInstructionCodeOffset(instructions.size()-1) +
                    instructions.get(instructions.size()-1).getCodeUnits();

            for (int i=0; i<instructions.size(); i++) {
                Instruction instruction = instructions.get(i);

                Opcode opcode = instruction.getOpcode();
                if (opcode == Opcode.PACKED_SWITCH) {
                    boolean valid = true;
                    int codeOffset = instructionOffsetMap.getInstructionCodeOffset(i);
                    int targetOffset = codeOffset + ((OffsetInstruction)instruction).getCodeOffset();
                    try {
                        targetOffset = findPayloadOffset(targetOffset, Opcode.PACKED_SWITCH_PAYLOAD);
                    } catch (MethodDefinition.InvalidSwitchPayload ex) {
                        valid = false;
                    }
                    if (valid) {
                        if (packedSwitchMap.get(targetOffset, -1) != -1) {
                            Instruction payloadInstruction =
                                    findSwitchPayload(targetOffset, Opcode.PACKED_SWITCH_PAYLOAD);
                            targetOffset = endOffset;
                            effectiveInstructions.set(i, new ImmutableInstruction31t(opcode,
                                    ((Instruction31t)instruction).getRegisterA(), targetOffset-codeOffset));
                            effectiveInstructions.add(payloadInstruction);
                            endOffset += payloadInstruction.getCodeUnits();
                        }
                        packedSwitchMap.append(targetOffset, codeOffset);
                    }
                } else if (opcode == Opcode.SPARSE_SWITCH) {
                    boolean valid = true;
                    int codeOffset = instructionOffsetMap.getInstructionCodeOffset(i);
                    int targetOffset = codeOffset + ((OffsetInstruction)instruction).getCodeOffset();
                    try {
                        targetOffset = findPayloadOffset(targetOffset, Opcode.SPARSE_SWITCH_PAYLOAD);
                    } catch (MethodDefinition.InvalidSwitchPayload ex) {
                        valid = false;
                        // The offset to the payload instruction was invalid. Nothing to do, except that we won't
                        // add this instruction to the map.
                    }
                    if (valid) {
                        if (sparseSwitchMap.get(targetOffset, -1) != -1) {
                            Instruction payloadInstruction =
                                    findSwitchPayload(targetOffset, Opcode.SPARSE_SWITCH_PAYLOAD);
                            targetOffset = endOffset;
                            effectiveInstructions.set(i, new ImmutableInstruction31t(opcode,
                                    ((Instruction31t)instruction).getRegisterA(), targetOffset-codeOffset));
                            effectiveInstructions.add(payloadInstruction);
                            endOffset += payloadInstruction.getCodeUnits();
                        }
                        sparseSwitchMap.append(targetOffset, codeOffset);
                    }
                }
            }
        } catch (Exception ex) {
            String methodString;
            try {
                methodString = ReferenceUtil.getMethodDescriptor(method);
            } catch (Exception ex2) {
                throw ExceptionWithContext.withContext(ex, "Error while processing method");
            }
            throw ExceptionWithContext.withContext(ex, "Error while processing method %s", methodString);
        }
    }
    
    public Node createAST() throws IOException {
        
        Node root = new Node(NodeType.METHOD);
        
        int parameterRegisterCount = 0;
        if (!AccessFlags.STATIC.isSet(method.getAccessFlags())) {
            parameterRegisterCount++;
        }

        for (MethodParameter parameter: methodParameters) {
            root.addChild(new Node(NodeType.ARGUMENT));
            parameterRegisterCount++;
            if (TypeUtils.isWideType(parameter.getType())) {
                parameterRegisterCount++;
            }
        }

        ASTRegisterNodeCreator rfm = new ASTRegisterNodeCreator(classDef.getOptions(), methodImpl.getRegisterCount(),parameterRegisterCount);


        List<MethodItem> methodItems = getMethodItems();
        for (MethodItem methodItem: methodItems) {
            
            if(methodItem instanceof InstructionMethodItem) {
                InstructionMethodItem iMethodItem = (InstructionMethodItem) methodItem;
                Instruction ins = iMethodItem.getInstruction();
                
                if(ins.getOpcode().format == Format.Format35c && (ins.getOpcode() == Opcode.INVOKE_DIRECT || ins.getOpcode() == Opcode.INVOKE_VIRTUAL)) {
                    
                    Node child;
                    
                    switch (ins.getOpcode()) {
                        case INVOKE_DIRECT:
                            child = new Node(NodeType.DIRECT);
                            break;
                        case INVOKE_VIRTUAL:
                            child = new Node(NodeType.VIRTUAL);
                            break;
                        default:
                            continue;
                    }
                        
                    root.addChild(child);
                    
                    FiveRegisterInstruction instruction = (FiveRegisterInstruction) ins;
                    
                    switch (instruction.getRegisterCount()) {
                        case 1:
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterC()));
                            break;
                        case 2:
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterC()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterD()));
                            break;
                        case 3:
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterC()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterD()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterE()));
                            break;
                        case 4:
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterC()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterD()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterE()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterF()));
                            break;
                        case 5:
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterC()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterD()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterE()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterF()));
                            child.addChild(rfm.createRegisterNode(instruction.getRegisterG()));
                            break;
                    }
                    
                }
                
            }
        }
        
        return root;
        
    }

    public Instruction findSwitchPayload(int targetOffset, Opcode type) {
        int targetIndex;
        try {
            targetIndex = instructionOffsetMap.getInstructionIndexAtCodeOffset(targetOffset);
        } catch (InvalidInstructionOffset ex) {
            throw new MethodDefinition.InvalidSwitchPayload(targetOffset);
        }

        //TODO: does dalvik let you pad with multiple nops?
        //TODO: does dalvik let a switch instruction point to a non-payload instruction?

        Instruction instruction = instructions.get(targetIndex);
        if (instruction.getOpcode() != type) {
            // maybe it's pointing to a NOP padding instruction. Look at the next instruction
            if (instruction.getOpcode() == Opcode.NOP) {
                targetIndex += 1;
                if (targetIndex < instructions.size()) {
                    instruction = instructions.get(targetIndex);
                    if (instruction.getOpcode() == type) {
                        return instruction;
                    }
                }
            }
            throw new MethodDefinition.InvalidSwitchPayload(targetOffset);
        } else {
            return instruction;
        }
    }

    @Override
    public int findPayloadOffset(int targetOffset, Opcode type) {
        int targetIndex;
        try {
            targetIndex = instructionOffsetMap.getInstructionIndexAtCodeOffset(targetOffset);
        } catch (InvalidInstructionOffset ex) {
            throw new MethodDefinition.InvalidSwitchPayload(targetOffset);
        }

        //TODO: does dalvik let you pad with multiple nops?
        //TODO: does dalvik let a switch instruction point to a non-payload instruction?

        Instruction instruction = instructions.get(targetIndex);
        if (instruction.getOpcode() != type) {
            // maybe it's pointing to a NOP padding instruction. Look at the next instruction
            if (instruction.getOpcode() == Opcode.NOP) {
                targetIndex += 1;
                if (targetIndex < instructions.size()) {
                    instruction = instructions.get(targetIndex);
                    if (instruction.getOpcode() == type) {
                        return instructionOffsetMap.getInstructionCodeOffset(targetIndex);
                    }
                }
            }
            throw new MethodDefinition.InvalidSwitchPayload(targetOffset);
        } else {
            return targetOffset;
        }
    }

    @Nonnull
    @Override
    public MethodDefinition.LabelCache getLabelCache() {
        return labelCache;
    }

    @Override
    public int getPackedSwitchBaseAddress(int packedSwitchPayloadCodeOffset) {
        return packedSwitchMap.get(packedSwitchPayloadCodeOffset, -1);
    }

    @Override
    public int getSparseSwitchBaseAddress(int sparseSwitchPayloadCodeOffset) {
        return sparseSwitchMap.get(sparseSwitchPayloadCodeOffset, -1);
    }

    private List<MethodItem> getMethodItems() {
        ArrayList<MethodItem> methodItems = new ArrayList<>();

        if ((classDef.getOptions().registerInfo != 0) || (classDef.getOptions().normalizeVirtualMethods) ||
                (classDef.getOptions().deodex && needsAnalyzed())) {
            addAnalyzedInstructionMethodItems(methodItems);
        } else {
            addInstructionMethodItems(methodItems);
        }

        addTries(methodItems);
        if (classDef.getOptions().outputDebugInfo) {
            addDebugInfo(methodItems);
        }

        if (classDef.getOptions().useSequentialLabels) {
            setLabelSequentialNumbers();
        }

        for (LabelMethodItem labelMethodItem: labelCache.getLabels()) {
            methodItems.add(labelMethodItem);
        }

        Collections.sort(methodItems);

        return methodItems;
    }

    private boolean needsAnalyzed() {
        for (Instruction instruction: methodImpl.getInstructions()) {
            if (instruction.getOpcode().odexOnly()) {
                return true;
            }
        }
        return false;
    }

    private void addInstructionMethodItems(List<MethodItem> methodItems) {
        int currentCodeAddress = 0;

        for (int i=0; i<effectiveInstructions.size(); i++) {
            Instruction instruction = effectiveInstructions.get(i);

            MethodItem methodItem = InstructionMethodItemFactory.makeInstructionFormatMethodItem(this,
                    currentCodeAddress, instruction);

            methodItems.add(methodItem);

            if (i != effectiveInstructions.size() - 1) {
                methodItems.add(new BlankMethodItem(currentCodeAddress));
            }

            if (classDef.getOptions().addCodeOffsets) {
                methodItems.add(new MethodItem(currentCodeAddress) {

                    @Override
                    public double getSortOrder() {
                        return -1000;
                    }

                    @Override
                    public boolean writeTo(IndentingWriter writer) throws IOException {
                        writer.write("#@");
                        writer.printUnsignedLongAsHex(codeAddress & 0xFFFFFFFFL);
                        return true;
                    }
                });
            }

            if (!classDef.getOptions().noAccessorComments && (instruction instanceof ReferenceInstruction)) {
                Opcode opcode = instruction.getOpcode();

                if (opcode.referenceType == ReferenceType.METHOD) {
                    MethodReference methodReference = null;
                    try {
                        methodReference = (MethodReference)((ReferenceInstruction)instruction).getReference();
                    } catch (InvalidItemIndex ex) {
                        // just ignore it for now. We'll deal with it later, when processing the instructions
                        // themselves
                    }

                    if (methodReference != null &&
                            SyntheticAccessorResolver.looksLikeSyntheticAccessor(methodReference.getName())) {
                        AccessedMember accessedMember =
                                classDef.getOptions().syntheticAccessorResolver.getAccessedMember(methodReference);
                        if (accessedMember != null) {
                            methodItems.add(new SyntheticAccessCommentMethodItem(accessedMember, currentCodeAddress));
                        }
                    }
                }
            }

            currentCodeAddress += instruction.getCodeUnits();
        }
    }

    private void addAnalyzedInstructionMethodItems(List<MethodItem> methodItems) {
        MethodAnalyzer methodAnalyzer = new MethodAnalyzer(classDef.getOptions().classPath, method,
                classDef.getOptions().inlineResolver, classDef.getOptions().normalizeVirtualMethods);

        AnalysisException analysisException = methodAnalyzer.getAnalysisException();
        if (analysisException != null) {
            // TODO: need to keep track of whether any errors occurred, so we can exit with a non-zero result
            methodItems.add(new CommentMethodItem(
                    String.format("AnalysisException: %s", analysisException.getMessage()),
                    analysisException.codeAddress, Integer.MIN_VALUE));
            analysisException.printStackTrace(System.err);
        }

        List<AnalyzedInstruction> instructions = methodAnalyzer.getAnalyzedInstructions();

        int currentCodeAddress = 0;
        for (int i=0; i<instructions.size(); i++) {
            AnalyzedInstruction instruction = instructions.get(i);

            MethodItem methodItem = InstructionMethodItemFactory.makeInstructionFormatMethodItem(
                    this, currentCodeAddress, instruction.getInstruction());

            methodItems.add(methodItem);

            if (instruction.getInstruction().getOpcode().format == Format.UnresolvedOdexInstruction) {
                methodItems.add(new CommentedOutMethodItem(
                        InstructionMethodItemFactory.makeInstructionFormatMethodItem(
                                this, currentCodeAddress, instruction.getOriginalInstruction())));
            }

            if (i != instructions.size() - 1) {
                methodItems.add(new BlankMethodItem(currentCodeAddress));
            }

            if (classDef.getOptions().addCodeOffsets) {
                methodItems.add(new MethodItem(currentCodeAddress) {

                    @Override
                    public double getSortOrder() {
                        return -1000;
                    }

                    @Override
                    public boolean writeTo(IndentingWriter writer) throws IOException {
                        writer.write("#@");
                        writer.printUnsignedLongAsHex(codeAddress & 0xFFFFFFFFL);
                        return true;
                    }
                });
            }

            if (classDef.getOptions().registerInfo != 0 &&
                    !instruction.getInstruction().getOpcode().format.isPayloadFormat) {
                methodItems.add(
                        new PreInstructionRegisterInfoMethodItem(classDef.getOptions().registerInfo,
                                methodAnalyzer, registerFormatter, instruction, currentCodeAddress));

                methodItems.add(
                        new PostInstructionRegisterInfoMethodItem(registerFormatter, instruction, currentCodeAddress));
            }

            currentCodeAddress += instruction.getInstruction().getCodeUnits();
        }
    }

    private void addTries(List<MethodItem> methodItems) {
        List<? extends TryBlock<? extends ExceptionHandler>> tryBlocks = methodImpl.getTryBlocks();
        if (tryBlocks.size() == 0) {
            return;
        }

        int lastInstructionAddress = instructionOffsetMap.getInstructionCodeOffset(instructions.size() - 1);
        int codeSize = lastInstructionAddress + instructions.get(instructions.size() - 1).getCodeUnits();

        for (TryBlock<? extends ExceptionHandler> tryBlock: tryBlocks) {
            int startAddress = tryBlock.getStartCodeAddress();
            int endAddress = startAddress + tryBlock.getCodeUnitCount();

            if (startAddress >= codeSize) {
                throw new RuntimeException(String.format("Try start offset %d is past the end of the code block.",
                        startAddress));
            }
            // Note: not >=. endAddress == codeSize is valid, when the try covers the last instruction
            if (endAddress > codeSize) {
                throw new RuntimeException(String.format("Try end offset %d is past the end of the code block.",
                        endAddress));
            }

            /**
             * The end address points to the address immediately after the end of the last
             * instruction that the try block covers. We want the .catch directive and end_try
             * label to be associated with the last covered instruction, so we need to get
             * the address for that instruction
             */

            int lastCoveredIndex = instructionOffsetMap.getInstructionIndexAtCodeOffset(endAddress - 1, false);
            int lastCoveredAddress = instructionOffsetMap.getInstructionCodeOffset(lastCoveredIndex);

            for (ExceptionHandler handler: tryBlock.getExceptionHandlers()) {
                int handlerAddress = handler.getHandlerCodeAddress();
                if (handlerAddress >= codeSize) {
                    throw new ExceptionWithContext(
                            "Exception handler offset %d is past the end of the code block.", handlerAddress);
                }

                //use the address from the last covered instruction
                CatchMethodItem catchMethodItem = new CatchMethodItem(classDef.getOptions(), labelCache, lastCoveredAddress,
                        handler.getExceptionType(), startAddress, endAddress, handlerAddress);
                methodItems.add(catchMethodItem);
            }
        }
    }

    private void addDebugInfo(final List<MethodItem> methodItems) {
        for (DebugItem debugItem: methodImpl.getDebugItems()) {
            methodItems.add(DebugMethodItem.build(registerFormatter, debugItem));
        }
    }

    private void setLabelSequentialNumbers() {
        HashMap<String, Integer> nextLabelSequenceByType = new HashMap<String, Integer>();
        ArrayList<LabelMethodItem> sortedLabels = new ArrayList<LabelMethodItem>(labelCache.getLabels());

        //sort the labels by their location in the method
        Collections.sort(sortedLabels);

        for (LabelMethodItem labelMethodItem: sortedLabels) {
            Integer labelSequence = nextLabelSequenceByType.get(labelMethodItem.getLabelPrefix());
            if (labelSequence == null) {
                labelSequence = 0;
            }
            labelMethodItem.setLabelSequence(labelSequence);
            nextLabelSequenceByType.put(labelMethodItem.getLabelPrefix(), labelSequence + 1);
        }
    }

}