/*
 * Copyright 2016 Christof Rabensteiner <christof.rabensteiner@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jf.baksmali.Adaptors;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.util.ExceptionWithContext;
import org.jf.util.IndentingWriter;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface MethodDefinition {

    ClassDefinition getClassDef();

    Method getMethod();

    MethodImplementation getMethodImpl();
    
    int findPayloadOffset(int targetOffset, Opcode type);

    @Nonnull
    MethodDefinition.LabelCache getLabelCache();

    int getPackedSwitchBaseAddress(int packedSwitchPayloadCodeOffset);

    RegisterFormatter getRegisterFormatter();

    int getSparseSwitchBaseAddress(int sparseSwitchPayloadCodeOffset);
    
    public static class LabelCache {
            protected HashMap<LabelMethodItem, LabelMethodItem> labels = new HashMap<LabelMethodItem, LabelMethodItem>();

            public LabelCache() {
            }

            public LabelMethodItem internLabel(LabelMethodItem labelMethodItem) {
                LabelMethodItem internedLabelMethodItem = labels.get(labelMethodItem);
                if (internedLabelMethodItem != null) {
                    return internedLabelMethodItem;
                }
                labels.put(labelMethodItem, labelMethodItem);
                return labelMethodItem;
            }


            public Collection<LabelMethodItem> getLabels() {
                return labels.values();
            }
        }

    public static class InvalidSwitchPayload extends ExceptionWithContext {
        private final int payloadOffset;

        public InvalidSwitchPayload(int payloadOffset) {
            super("No switch payload at offset: %d", payloadOffset);
            this.payloadOffset = payloadOffset;
        }

        public int getPayloadOffset() {
            return payloadOffset;
        }
    }
}
