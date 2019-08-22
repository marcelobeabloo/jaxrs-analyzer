package com.sebastian_daschner.jaxrs_analyzer.backend.swagger;

import static com.sebastian_daschner.jaxrs_analyzer.analysis.results.TypeUtils.*;
import static com.sebastian_daschner.jaxrs_analyzer.backend.swagger.TypeIdentifierTestSupport.resetTypeIdentifierCounter;
import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeDefinition;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeIdentifier;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class SchemaBuilderTest {

    private static final TypeIdentifier INTEGER_IDENTIFIER = TypeIdentifier.ofType(Types.INTEGER);
    private static final TypeIdentifier INT_LIST_IDENTIFIER = TypeIdentifier.ofType("Ljava/util/List<Ljava/lang/Integer;>;");

    private SchemaBuilder cut;
    private final Map<TypeIdentifier, TypeRepresentation> representations = new HashMap<>();

    @Before
    public void resetRepresentations() {
        representations.clear();
        resetTypeIdentifierCounter();
    }

    @Test
    public void testSimpleDefinitions() {
        representations.put(INT_LIST_IDENTIFIER, TypeRepresentation.ofCollection(INTEGER_IDENTIFIER, TypeRepresentation.ofConcrete(INTEGER_IDENTIFIER)));

        final TypeIdentifier modelIdentifier = MODEL_IDENTIFIER;
        final Map<String, TypeDefinition> modelProperties = new HashMap<>();

        modelProperties.put("test1", TypeDefinition.of(INT_IDENTIFIER));
        modelProperties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));
        modelProperties.put("array1", TypeDefinition.of(INT_LIST_IDENTIFIER));

        representations.put(modelIdentifier, TypeRepresentation.ofConcrete(modelIdentifier, modelProperties));

        final Map<String, TypeDefinition> dynamicProperties = new HashMap<>();

        dynamicProperties.put("test2", TypeDefinition.of(INT_IDENTIFIER));
        dynamicProperties.put("hello2", TypeDefinition.of(STRING_IDENTIFIER));
        dynamicProperties.put("array2", TypeDefinition.of(INT_LIST_IDENTIFIER));

        final TypeIdentifier nestedDynamicIdentifier = TypeIdentifier.ofDynamic();
        final Map<String, TypeDefinition> nestedDynamicProperties = new HashMap<>();
        nestedDynamicProperties.put("test", TypeDefinition.of(INT_IDENTIFIER));

        dynamicProperties.put("object2", TypeDefinition.of(nestedDynamicIdentifier));

        representations.put(nestedDynamicIdentifier, TypeRepresentation.ofConcrete(nestedDynamicIdentifier, nestedDynamicProperties));
        representations.put(OBJECT_IDENTIFIER, TypeRepresentation.ofConcrete(OBJECT_IDENTIFIER, dynamicProperties));

        cut = new SchemaBuilder(representations);
        assertThat(cut.build(TypeDefinition.of(modelIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Model").build()));
        assertThat(cut.build(TypeDefinition.of(OBJECT_IDENTIFIER)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Object").build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("Model", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array1", Json.createObjectBuilder().add("type", "array").add("items", type("integer")))
                        .add("hello1", type("string"))
                        .add("test1", type("integer"))))
                .add("JsonObject", Json.createObjectBuilder().add("properties", Json.createObjectBuilder().add("test", type("integer"))))
                .add("Object", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array2", Json.createObjectBuilder().add("type", "array").add("items", type("integer")))
                        .add("hello2", type("string"))
                        .add("object2", Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject"))
                        .add("test2", type("integer"))))
                .build()));
    }

    @Test
    public void testMultipleDefinitionsNameCollisions() {
        final TypeIdentifier lockIdentifier = TypeIdentifier.ofType("Ljava/util/concurrent/locks/Lock;");
        final TypeIdentifier anotherLockIdentifier = TypeIdentifier.ofType("Ljavax/ejb/Lock;");

        final Map<String, TypeDefinition> lockProperties = new HashMap<>();
        lockProperties.put("test1", TypeDefinition.of(INT_IDENTIFIER));
        lockProperties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));
        lockProperties.put("array1", TypeDefinition.of(INT_LIST_IDENTIFIER));

        final Map<String, TypeDefinition> anotherLockProperties = new HashMap<>();
        anotherLockProperties.put("test1", TypeDefinition.of(INT_IDENTIFIER));
        anotherLockProperties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));
        anotherLockProperties.put("array1", TypeDefinition.of(INT_LIST_IDENTIFIER));

        representations.put(INT_LIST_IDENTIFIER, TypeRepresentation.ofCollection(INTEGER_IDENTIFIER, TypeRepresentation.ofConcrete(INTEGER_IDENTIFIER)));
        representations.put(lockIdentifier, TypeRepresentation.ofConcrete(lockIdentifier, lockProperties));
        representations.put(anotherLockIdentifier, TypeRepresentation.ofConcrete(anotherLockIdentifier, anotherLockProperties));

        cut = new SchemaBuilder(representations);

        assertThat(cut.build(TypeDefinition.of(lockIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Lock").build()));
        assertThat(cut.build(TypeDefinition.of(anotherLockIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Lock_2").build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("Lock", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array1", Json.createObjectBuilder().add("type", "array").add("items", type("integer")))
                        .add("hello1", type("string"))
                        .add("test1", type("integer"))))
                .add("Lock_2", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array1", Json.createObjectBuilder().add("type", "array").add("items", type("integer")))
                        .add("hello1", type("string"))
                        .add("test1", type("integer"))))
                .build()));
    }

    @Test
    public void testSingleDynamicDefinitionMissingNestedType() {
        final TypeIdentifier identifier = TypeIdentifier.ofDynamic();

        final Map<String, TypeDefinition> properties = new HashMap<>();
        properties.put("test1", TypeDefinition.of(INT_IDENTIFIER));
        properties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));
        // unknown type identifier
        properties.put("array1", TypeDefinition.of(INT_LIST_IDENTIFIER));

        representations.put(identifier, TypeRepresentation.ofConcrete(identifier, properties));

        cut = new SchemaBuilder(representations);

        assertThat(cut.build(TypeDefinition.of(identifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject").build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("JsonObject", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array1", Json.createObjectBuilder().add("type", "object"))
                        .add("hello1", type("string"))
                        .add("test1", type("integer"))))
                .build()));
    }

    @Test
    public void testMultipleDifferentDefinitions() {
        final Map<String, TypeDefinition> properties = new HashMap<>();

        properties.put("test1", TypeDefinition.of(INT_IDENTIFIER));
        properties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));
        properties.put("array1", TypeDefinition.of(INT_LIST_IDENTIFIER));

        representations.put(MODEL_IDENTIFIER, TypeRepresentation.ofConcrete(MODEL_IDENTIFIER, properties));
        representations.put(INT_LIST_IDENTIFIER, TypeRepresentation.ofCollection(INTEGER_IDENTIFIER, TypeRepresentation.ofConcrete(INTEGER_IDENTIFIER)));

        cut = new SchemaBuilder(representations);

        assertThat(cut.build(TypeDefinition.of(MODEL_IDENTIFIER)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Model").build()));
        assertThat(cut.build(TypeDefinition.of(TypeIdentifier.ofType(Types.OBJECT))).build(), is(Json.createObjectBuilder().add("type", "object").build()));
        // build with different type identifier instance
        assertThat(cut.build(TypeDefinition.of(TypeIdentifier.ofType("Lcom/sebastian_daschner/test/Model;"))).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Model").build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("Model", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array1", Json.createObjectBuilder().add("type", "array").add("items", type("integer")))
                        .add("hello1", type("string"))
                        .add("test1", type("integer"))))
                .build()));
    }

    @Test
    public void testSameDynamicDefinitions() {
        final TypeIdentifier firstIdentifier = TypeIdentifier.ofDynamic();
        final TypeIdentifier secondIdentifier = TypeIdentifier.ofDynamic();

        final Map<String, TypeDefinition> firstProperties = new HashMap<>();
        firstProperties.put("test1", TypeDefinition.of(INT_IDENTIFIER));
        firstProperties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));
        firstProperties.put("array1", TypeDefinition.of(INT_LIST_IDENTIFIER));
        firstProperties.put("nested", TypeDefinition.of(secondIdentifier));

        final Map<String, TypeDefinition> secondProperties = new HashMap<>();
        secondProperties.put("nested", TypeDefinition.of(firstIdentifier));

        representations.put(INT_LIST_IDENTIFIER, TypeRepresentation.ofCollection(INTEGER_IDENTIFIER, TypeRepresentation.ofConcrete(INTEGER_IDENTIFIER)));
        representations.put(firstIdentifier, TypeRepresentation.ofConcrete(firstIdentifier, firstProperties));
        representations.put(secondIdentifier, TypeRepresentation.ofConcrete(secondIdentifier, secondProperties));

        cut = new SchemaBuilder(representations);

        assertThat(cut.build(TypeDefinition.of(firstIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject").build()));
        assertThat(cut.build(TypeDefinition.of(firstIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject").build()));
        assertThat(cut.build(TypeDefinition.of(secondIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject_2").build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("JsonObject", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("array1", Json.createObjectBuilder().add("type", "array").add("items", type("integer")))
                        .add("hello1", type("string"))
                        .add("nested", Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject_2"))
                        .add("test1", type("integer"))))
                .add("JsonObject_2", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("nested", Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject"))))
                .build()));
    }

    @Test
    public void testDifferentDynamicDefinitions() {
        final TypeIdentifier firstIdentifier = TypeIdentifier.ofDynamic();
        final TypeIdentifier secondIdentifier = TypeIdentifier.ofDynamic();
        final TypeIdentifier thirdIdentifier = TypeIdentifier.ofDynamic();

        final Map<String, TypeDefinition> firstProperties = new HashMap<>();
        firstProperties.put("_links", TypeDefinition.of(secondIdentifier));

        final Map<String, TypeDefinition> secondProperties = new HashMap<>();
        secondProperties.put("self", TypeDefinition.of(thirdIdentifier));

        final Map<String, TypeDefinition> thirdProperties = new HashMap<>();
        thirdProperties.put("href", TypeDefinition.of(STRING_IDENTIFIER));

        representations.put(firstIdentifier, TypeRepresentation.ofConcrete(firstIdentifier, firstProperties));
        representations.put(secondIdentifier, TypeRepresentation.ofConcrete(secondIdentifier, secondProperties));
        representations.put(thirdIdentifier, TypeRepresentation.ofConcrete(thirdIdentifier, thirdProperties));

        cut = new SchemaBuilder(representations);

        assertThat(cut.build(TypeDefinition.of(firstIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject").build()));
        assertThat(cut.build(TypeDefinition.of(secondIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject_2").build()));
        assertThat(cut.build(TypeDefinition.of(thirdIdentifier)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject_3").build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("JsonObject", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("_links", Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject_2"))))
                .add("JsonObject_2", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("self", Json.createObjectBuilder().add("$ref", "#/definitions/JsonObject_3"))))
                .add("JsonObject_3", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("href", type("string"))))
                .build()));
    }

    @Test
    public void testEnumDefinitions() {
        final TypeIdentifier enumIdentifier = TypeIdentifier.ofType("Lcom/sebastian_daschner/test/Enumeration;");
        final Map<String, TypeDefinition> modelProperties = new HashMap<>();

        modelProperties.put("foobar", TypeDefinition.of(enumIdentifier));
        modelProperties.put("hello1", TypeDefinition.of(STRING_IDENTIFIER));

        representations.put(MODEL_IDENTIFIER, TypeRepresentation.ofConcrete(MODEL_IDENTIFIER, modelProperties));
        representations.put(enumIdentifier, TypeRepresentation.ofEnum(TypeDefinition.of(enumIdentifier), "THIRD", "FIRST", "SECOND"));

        cut = new SchemaBuilder(representations);

        assertThat(cut.build(TypeDefinition.of(MODEL_IDENTIFIER)).build(), is(Json.createObjectBuilder().add("$ref", "#/definitions/Model").build()));
        assertThat(cut.build(TypeDefinition.of(enumIdentifier)).build(), is(Json.createObjectBuilder().add("type", "string")
                .add("enum", Json.createArrayBuilder().add("FIRST").add("SECOND").add("THIRD")).build()));

        final JsonObject definitions = cut.getDefinitions();
        assertThat(definitions, is(Json.createObjectBuilder()
                .add("Model", Json.createObjectBuilder().add("properties", Json.createObjectBuilder()
                        .add("foobar", Json.createObjectBuilder().add("type", "string").add("enum", Json.createArrayBuilder().add("FIRST").add("SECOND").add("THIRD")))
                        .add("hello1", type("string"))))
                .build()));
    }

    private static JsonObject type(final String type) {
        return Json.createObjectBuilder().add("type", type).build();
    }

}