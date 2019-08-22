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

package com.sebastian_daschner.jaxrs_analyzer.analysis.results.testclasses.typeanalyzer;

import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeDefinition;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeIdentifier;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation;
import java.util.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class TestClass12 {

    private int first;
    private InnerTestClass child;

    @XmlAccessorType(XmlAccessType.FIELD)
    private class InnerTestClass {
        private int second;
        private TestClass12 child;
    }

    public static Set<TypeRepresentation> expectedTypeRepresentations() {
        final Map<String, TypeDefinition> properties = new HashMap<>();

        final TypeIdentifier innerTestIdentifier = TypeIdentifier.ofType("Lcom/sebastian_daschner/jaxrs_analyzer/analysis/results/testclasses/typeanalyzer/TestClass12$InnerTestClass;");
        properties.put("first", TypeDefinition.of(TypeIdentifier.ofType(Types.PRIMITIVE_INT)));
        properties.put("child", TypeDefinition.of(innerTestIdentifier));

        final TypeIdentifier testClass12Identifier = expectedIdentifier();
        final TypeRepresentation testClass12 = TypeRepresentation.ofConcrete(testClass12Identifier, properties);

        final Map<String, TypeDefinition> innerProperties = new HashMap<>();
        innerProperties.put("second", TypeDefinition.of(TypeIdentifier.ofType(Types.PRIMITIVE_INT)));
        innerProperties.put("child", TypeDefinition.of(testClass12Identifier));

        final TypeRepresentation innerTestClass = TypeRepresentation.ofConcrete(innerTestIdentifier, innerProperties);

        return new HashSet<>(Arrays.asList(testClass12, innerTestClass));
    }

    public static TypeIdentifier expectedIdentifier() {
        return TypeIdentifier.ofType("Lcom/sebastian_daschner/jaxrs_analyzer/analysis/results/testclasses/typeanalyzer/TestClass12;");
    }

}
