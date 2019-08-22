package com.sebastian_daschner.jaxrs_analyzer.analysis.results;

import static com.sebastian_daschner.jaxrs_analyzer.analysis.results.TypeUtils.STRING_IDENTIFIER;
import static com.sebastian_daschner.jaxrs_analyzer.analysis.results.TypeUtils.STRING_LIST_IDENTIFIER;
import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeDefinition;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeIdentifier;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class DynamicTypeAnalyzerTest {

    private DynamicTypeAnalyzer cut;
    private Map<TypeIdentifier, TypeRepresentation> typeRepresentations;

    @Before
    public void setUp() {
        typeRepresentations = new HashMap<>();
        cut = new DynamicTypeAnalyzer(typeRepresentations);
    }

    @Test
    public void testPrimitives() {
        TypeIdentifier identifier = cut.analyze(JsonValue.FALSE);
        assertThat(identifier.getType(), is(Types.PRIMITIVE_BOOLEAN));

        identifier = cut.analyze(JsonValue.TRUE);
        assertThat(identifier.getType(), is(Types.PRIMITIVE_BOOLEAN));

        identifier = cut.analyze(JsonValue.NULL);
        assertThat(identifier.getType(), is(Types.OBJECT));

        identifier = cut.analyze(jsonString(""));
        assertThat(identifier.getType(), is(Types.STRING));

        identifier = cut.analyze(jsonNumber(0.5));
        assertThat(identifier.getType(), is(Types.DOUBLE));

        assertThat(typeRepresentations.size(), is(0));
    }

    @Test
    public void testArraysAndObjects() {
        TypeIdentifier identifier = cut.analyze(Json.createArrayBuilder().add("string").build());
        final String firstName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        identifier = cut.analyze(Json.createObjectBuilder().add("key", 0).build());
        final String secondName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        assertThat(typeRepresentations.size(), is(2));

        final TypeRepresentation.CollectionTypeRepresentation collection = getRepresentation(firstName);
        assertThat(collection.getIdentifier().getName(), is(firstName));
        assertThat(collection.getComponentType().getType(), is(Types.STRING));

        final TypeRepresentation.ConcreteTypeRepresentation concrete = getRepresentation(secondName);
        assertThat(concrete.getIdentifier().getName(), is(secondName));
        assertThat(concrete.getProperties().size(), is(1));
        assertThat(concrete.getProperties().get("key").getTypeIdentifier().getType(), is(Types.DOUBLE));
    }

    @Test
    public void testArraysAndObjectsNestedTypes() {
        TypeIdentifier identifier = cut.analyze(Json.createArrayBuilder().add(Json.createObjectBuilder().add("key", "value").add("number", 10.0)).build());
        final String firstName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        identifier = cut.analyze(Json.createObjectBuilder().add("key", 0).add("object", Json.createObjectBuilder().add("string", "value"))
                .add("array", Json.createArrayBuilder().add("string")).build());
        final String secondName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        assertThat(typeRepresentations.size(), is(5));

        TypeRepresentation.CollectionTypeRepresentation collection = getRepresentation(firstName);
        final String thirdName = collection.getComponentType().getName();
        assertThat(collection.getIdentifier().getName(), is(firstName));
        assertThat(collection.getComponentType().getType(), is(Types.JSON));

        TypeRepresentation.ConcreteTypeRepresentation concrete = getRepresentation(thirdName);
        assertThat(concrete.getIdentifier().getName(), is(thirdName));
        assertThat(concrete.getProperties().size(), is(2));
        assertThat(concrete.getProperties().get("key").getTypeIdentifier().getType(), is(Types.STRING));
        assertThat(concrete.getProperties().get("number").getTypeIdentifier().getType(), is(Types.DOUBLE));

        concrete = getRepresentation(secondName);
        assertThat(concrete.getIdentifier().getName(), is(secondName));
        assertThat(concrete.getProperties().size(), is(3));
        assertThat(concrete.getProperties().get("key").getTypeIdentifier().getType(), is(Types.DOUBLE));
        final String fourthName = concrete.getProperties().get("object").getTypeIdentifier().getName();
        assertThat(concrete.getProperties().get("object").getTypeIdentifier().getType(), is(Types.JSON));
        final String fifthName = concrete.getProperties().get("array").getTypeIdentifier().getName();
        assertThat(concrete.getProperties().get("array").getTypeIdentifier().getType(), is(Types.JSON));

        concrete = getRepresentation(fourthName);
        assertThat(concrete.getIdentifier().getName(), is(fourthName));
        assertThat(concrete.getProperties().get("string").getTypeIdentifier().getType(), is(Types.STRING));

        collection = getRepresentation(fifthName);
        assertThat(collection.getIdentifier().getName(), is(fifthName));
        assertThat(collection.getComponentType().getType(), is(Types.STRING));
    }

    @Test
    public void testEqualTypes() {
        // should be ignored
        typeRepresentations.put(STRING_LIST_IDENTIFIER, TypeRepresentation.ofCollection(STRING_LIST_IDENTIFIER, TypeRepresentation.ofConcrete(STRING_IDENTIFIER)));
        final TypeIdentifier modelIdentifier = TypeIdentifier.ofType("com.sebastian_daschner.test.Model");
        final Map<String, TypeDefinition> modelProperties = Collections.singletonMap("string", TypeDefinition.of(TypeUtils.STRING_IDENTIFIER));
        typeRepresentations.put(modelIdentifier, TypeRepresentation.ofConcrete(modelIdentifier, modelProperties));

        TypeIdentifier identifier = cut.analyze(Json.createArrayBuilder().add("foobar").build());
        final String firstName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        identifier = cut.analyze(Json.createObjectBuilder().add("key", 0).add("object", Json.createObjectBuilder().add("string", "value"))
                .add("array", Json.createArrayBuilder().add("string")).add("other", Json.createObjectBuilder().add("hello", "world")).build());
        final String secondName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        identifier = cut.analyze(Json.createObjectBuilder().add("string", "world").build());
        final String thirdName = identifier.getName();
        assertThat(identifier.getType(), is(Types.JSON));

        assertThat(typeRepresentations.size(), is(6));

        TypeRepresentation.CollectionTypeRepresentation collection = getRepresentation(firstName);
        assertThat(collection.getIdentifier().getName(), is(firstName));
        assertThat(collection.getComponentType().getType(), is(Types.STRING));

        TypeRepresentation.ConcreteTypeRepresentation concrete = getRepresentation(secondName);
        assertThat(concrete.getIdentifier().getName(), is(secondName));
        assertThat(concrete.getProperties().size(), is(4));
        assertThat(concrete.getProperties().get("key").getTypeIdentifier().getType(), is(Types.DOUBLE));
        assertThat(concrete.getProperties().get("object").getTypeIdentifier().getName(), is(thirdName));
        assertThat(concrete.getProperties().get("object").getTypeIdentifier().getType(), is(Types.JSON));
        assertThat(concrete.getProperties().get("array").getTypeIdentifier().getName(), is(firstName));
        assertThat(concrete.getProperties().get("array").getTypeIdentifier().getType(), is(Types.JSON));
        final String fourthName = concrete.getProperties().get("other").getTypeIdentifier().getName();
        assertThat(concrete.getProperties().get("other").getTypeIdentifier().getType(), is(Types.JSON));

        concrete = getRepresentation(thirdName);
        assertThat(concrete.getIdentifier().getName(), is(thirdName));
        assertThat(concrete.getProperties().get("string").getTypeIdentifier().getType(), is(Types.STRING));

        concrete = getRepresentation(fourthName);
        assertThat(concrete.getIdentifier().getName(), is(fourthName));
        assertThat(concrete.getProperties().get("hello").getTypeIdentifier().getType(), is(Types.STRING));
    }

    private <T extends TypeRepresentation> T getRepresentation(final String firstName) {
        return (T) typeRepresentations.entrySet().stream().filter(e -> e.getKey().getName().equals(firstName)).findAny().get().getValue();
    }

    private static JsonNumber jsonNumber(final double number) {
        return Json.createObjectBuilder().add("number", number).build().getJsonNumber("number");
    }

    private static JsonString jsonString(final String string) {
        return Json.createObjectBuilder().add("string", string).build().getJsonString("string");
    }

}