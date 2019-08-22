package com.sebastian_daschner.jaxrs_analyzer.analysis.results.testclasses.typeanalyzer;

import com.sebastian_daschner.jaxrs_analyzer.analysis.results.TypeUtils;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeDefinition;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeIdentifier;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

// TODO remove
@XmlAccessorType(XmlAccessType.FIELD)
public class TestClass18 extends SuperTestClass4 {

    private String foobar;
    private TestClass18 partner;

    public static Set<TypeRepresentation> expectedTypeRepresentations() {
        final Map<String, TypeDefinition> properties = new HashMap<>();

        final TypeIdentifier identifier = expectedIdentifier();
        properties.put("foobar", TypeDefinition.of(TypeUtils.STRING_IDENTIFIER));
        properties.put("test", TypeDefinition.of(TypeUtils.STRING_IDENTIFIER));
        properties.put("partner", TypeDefinition.of(identifier));

        return Collections.singleton(TypeRepresentation.ofConcrete(identifier, properties));
    }

    public static TypeIdentifier expectedIdentifier() {
        return TypeIdentifier.ofType("Lcom/sebastian_daschner/jaxrs_analyzer/analysis/results/testclasses/typeanalyzer/TestClass18;");
    }

}

@XmlAccessorType(XmlAccessType.FIELD)
class SuperTestClass4 {
    private String test;
}
