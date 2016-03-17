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

import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.*;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.util.IndentingWriter;

public class ASTClassDefinition implements ClassDefinition {
    
    @Nonnull private final baksmaliOptions options;
    @Nonnull private final ClassDef classDef;

    protected boolean validationErrors;

    @Override
    public baksmaliOptions getOptions() {
        return options;
    }

    @Override
    public ClassDef getClassDef() {
        return classDef;
    }
    
    public ASTClassDefinition(@Nonnull baksmaliOptions options, @Nonnull ClassDef classDef) {
        this.options = options;
        this.classDef = classDef;
    }

    @Override
    public boolean hadValidationErrors() {
        return validationErrors;
    }

    public Collection<Node> createAST() throws IOException {
        Collection<Node> ast = createDirectMethodsAST(); 
        ast.addAll(createVirtualMethodsAST());
        return ast;
    }

    public Map<String, Node> createASTwithNames() throws IOException {
        Map<String, Node> astwithnames = createDirectMethodsASTwithNames();
        astwithnames.putAll(createVirtualMethodsASTwithNames());
        return astwithnames;
    }
    

    private Map<String, Node> createDirectMethodsASTwithNames() throws IOException {

        Map<String, Node> methodASTs = new HashMap<>();
        
        Iterable<? extends Method> directMethods;
        if (classDef instanceof DexBackedClassDef) {
            directMethods = ((DexBackedClassDef)classDef).getDirectMethods(false);
        } else {
            directMethods = classDef.getDirectMethods();
        }
        
        for (Method method: directMethods) {
            
            MethodImplementation methodImpl = method.getImplementation();
            if (methodImpl != null) {
                String name = method.getName();
                ASTMethodDefinition methodASTBuilder = new ASTMethodDefinition(this, method, methodImpl);
                methodASTs.put(name, methodASTBuilder.createAST());
            }
        }
        
        return methodASTs;
    }
    
    private Collection<Node> createDirectMethodsAST() throws IOException {
        return new ArrayList<>(createDirectMethodsASTwithNames().values());
    }
    
    private Map<String, Node> createVirtualMethodsASTwithNames() throws IOException {
        
        Map<String, Node> methodASTs = new HashMap<>();

        Iterable<? extends Method> virtualMethods;
        if (classDef instanceof DexBackedClassDef) {
            virtualMethods = ((DexBackedClassDef)classDef).getVirtualMethods(false);
        } else {
            virtualMethods = classDef.getVirtualMethods();
        }
        
        for (Method method: virtualMethods) {

            MethodImplementation methodImpl = method.getImplementation();
            if (methodImpl != null) {
                ASTMethodDefinition methodDefinition = new ASTMethodDefinition(this, method, methodImpl);
                String name = method.getName();
                methodASTs.put(name, methodDefinition.createAST());
            }
        }
        return methodASTs;
    }    
    
    private Collection<Node> createVirtualMethodsAST() throws IOException {
        return createVirtualMethodsASTwithNames().values();
    }

    @Override
    public void writeTo(IndentingWriter writer) throws IOException {
        throw new UnsupportedOperationException("Not intendet for writing.");
    }
}
