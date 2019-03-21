/*
 * Copyright (C) 2015 Sebastian Daschner, sebastian-daschner.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastian_daschner.beabloo.jaxrs_analyzer.analysis.bytecode.collection.testclasses;

import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.instructions.DefaultInstruction;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.instructions.DupInstruction;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.instructions.Instruction;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.instructions.PushInstruction;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.instructions.SizeChangingInstruction;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.instructions.StoreInstruction;

import java.util.LinkedList;
import java.util.List;

public class TestClass2 {

    public void method() {
        int status = 200;
        if ((status = 300) > 0) {
            status = 100;
        }
        status = 200;
    }

    public static List<Instruction> getResult() {
        final List<Instruction> instructions = new LinkedList<>();

        instructions.add(new PushInstruction(200, Types.PRIMITIVE_INT, null));
        instructions.add(new StoreInstruction(1, Types.OBJECT, null));
        instructions.add(new PushInstruction(300, Types.PRIMITIVE_INT, null));
        instructions.add(new DupInstruction(null));
        instructions.add(new StoreInstruction(1, Types.PRIMITIVE_INT, "status", null));
        instructions.add(new SizeChangingInstruction("IFLE", 0, 1, null));
        instructions.add(new PushInstruction(100, Types.PRIMITIVE_INT, null));
        instructions.add(new StoreInstruction(1, Types.PRIMITIVE_INT, "status", null));
        instructions.add(new PushInstruction(200, Types.PRIMITIVE_INT, null));
        instructions.add(new StoreInstruction(1, Types.PRIMITIVE_INT, "status", null));
        instructions.add(new DefaultInstruction("RETURN", null));

        return instructions;
    }

}
