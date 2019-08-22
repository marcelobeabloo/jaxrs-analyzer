package com.sebastian_daschner.jaxrs_analyzer.model.rest;

import static com.sebastian_daschner.jaxrs_analyzer.analysis.results.TypeUtils.*;
import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CollectionTypeRepresentationTest {

    @Test
    public void testContentEqualsConcrete() {
        final TypeRepresentation.ConcreteTypeRepresentation stringRepresentation = (TypeRepresentation.ConcreteTypeRepresentation) TypeRepresentation.ofConcrete(TypeIdentifier.ofType(Types.STRING));
        final TypeRepresentation.ConcreteTypeRepresentation objectRepresentation = (TypeRepresentation.ConcreteTypeRepresentation) TypeRepresentation.ofConcrete(TypeIdentifier.ofType(Types.OBJECT));

        assertTrue(stringRepresentation.contentEquals(objectRepresentation.getProperties()));

        final Map<String, TypeDefinition> firstProperties = new HashMap<>();
        firstProperties.put("hello", TypeDefinition.of(STRING_IDENTIFIER));
        firstProperties.put("world", TypeDefinition.of(INT_IDENTIFIER));
        final TypeRepresentation.ConcreteTypeRepresentation firstRepresentation = (TypeRepresentation.ConcreteTypeRepresentation) TypeRepresentation.ofConcrete(OBJECT_IDENTIFIER, firstProperties);

        final Map<String, TypeDefinition> secondProperties = new HashMap<>();
        secondProperties.put("hello", TypeDefinition.of(STRING_IDENTIFIER));
        secondProperties.put("world", TypeDefinition.of(INT_IDENTIFIER));

        assertTrue(firstRepresentation.contentEquals(secondProperties));
    }

    @Test
    public void testContentEqualsCollection() {
        final Map<String, TypeDefinition> firstProperties = new HashMap<>();
        firstProperties.put("hello", TypeDefinition.of(STRING_IDENTIFIER));
        firstProperties.put("world", TypeDefinition.of(INT_IDENTIFIER));

        final TypeRepresentation firstRepresentation = TypeRepresentation.ofConcrete(OBJECT_IDENTIFIER, firstProperties);
        final TypeRepresentation secondRepresentation = TypeRepresentation.ofConcrete(OBJECT_IDENTIFIER, Collections.emptyMap());
        final TypeRepresentation thirdRepresentation = TypeRepresentation.ofConcrete(TypeIdentifier.ofDynamic(), new HashMap<>(firstProperties));
        final TypeRepresentation fourthRepresentation = TypeRepresentation.ofConcrete(TypeIdentifier.ofDynamic(), new HashMap<>(firstProperties));

        final TypeRepresentation.CollectionTypeRepresentation firstCollection = (TypeRepresentation.CollectionTypeRepresentation) TypeRepresentation.ofCollection(TypeIdentifier.ofDynamic(), firstRepresentation);
        final TypeRepresentation.CollectionTypeRepresentation secondCollection = (TypeRepresentation.CollectionTypeRepresentation) TypeRepresentation.ofCollection(TypeIdentifier.ofDynamic(), secondRepresentation);
        final TypeRepresentation.CollectionTypeRepresentation thirdCollection = (TypeRepresentation.CollectionTypeRepresentation) TypeRepresentation.ofCollection(TypeIdentifier.ofDynamic(), thirdRepresentation);
        final TypeRepresentation.CollectionTypeRepresentation fourthCollection = (TypeRepresentation.CollectionTypeRepresentation) TypeRepresentation.ofCollection(TypeIdentifier.ofDynamic(), fourthRepresentation);

        assertTrue(firstCollection.contentEquals(secondCollection.getRepresentation()));
        assertFalse(firstCollection.contentEquals(thirdCollection.getRepresentation()));
        assertTrue(thirdCollection.contentEquals(fourthCollection.getRepresentation()));
    }

}