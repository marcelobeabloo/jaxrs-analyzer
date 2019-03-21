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

package com.sebastian_daschner.jaxrs_analyzer.analysis.bytecode.simulation;

import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.Element;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.JsonArray;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.JsonObject;
import com.sebastian_daschner.jaxrs_analyzer.model.methods.IdentifiableMethod;
import com.sebastian_daschner.jaxrs_analyzer.model.methods.MethodIdentifier;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Known JSON methods which apply logic to the result or to the return element.
 *
 * @author Sebastian Daschner
 */
enum KnownJsonResultMethod implements IdentifiableMethod {

    JSON_ARRAY_BUILDER_CREATE(MethodIdentifier.ofStatic(Types.CLASS_JSON, "createArrayBuilder", Types.JSON_ARRAY_BUILDER), (object, arguments) -> new Element(Types.JSON_ARRAY, new JsonArray())),

    JSON_ARRAY_BUILDER_ADD_BIG_DECIMAL(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.BIG_DECIMAL),
            (object, arguments) -> addToArray(object, arguments, Types.BIG_DECIMAL)),

    JSON_ARRAY_BUILDER_ADD_BIG_INTEGER(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.BIG_INTEGER),
            (object, arguments) -> addToArray(object, arguments, Types.BIG_INTEGER)),

    JSON_ARRAY_BUILDER_ADD_STRING(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.STRING),
            (object, arguments) -> addToArray(object, arguments, Types.STRING)),

    JSON_ARRAY_BUILDER_ADD_INT(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.PRIMITIVE_INT),
            (object, arguments) -> addToArray(object, arguments, Types.INTEGER)),

    JSON_ARRAY_BUILDER_ADD_LONG(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.PRIMITIVE_LONG),
            (object, arguments) -> addToArray(object, arguments, Types.LONG)),

    JSON_ARRAY_BUILDER_ADD_DOUBLE(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.PRIMITIVE_DOUBLE),
            (object, arguments) -> addToArray(object, arguments, Types.DOUBLE)),

    JSON_ARRAY_BUILDER_ADD_BOOLEAN(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.PRIMITIVE_BOOLEAN),
            (object, arguments) -> addToArray(object, arguments, Types.PRIMITIVE_BOOLEAN)),

    JSON_ARRAY_BUILDER_ADD_JSON(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.JSON_VALUE),
            KnownJsonResultMethod::addToArray),

    JSON_ARRAY_BUILDER_ADD_JSON_OBJECT(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.JSON_OBJECT_BUILDER), (object, arguments) ->
            addToArray(object, arguments, Types.JSON_OBJECT)),

    JSON_ARRAY_BUILDER_ADD_JSON_ARRAY(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "add", Types.JSON_ARRAY_BUILDER, Types.JSON_ARRAY_BUILDER), (object, arguments) ->
            addToArray(object, arguments, Types.JSON_ARRAY)),

    JSON_ARRAY_BUILDER_ADD_NULL(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "addNull", Types.JSON_ARRAY_BUILDER),
            (object, arguments) -> {
                object.getPossibleValues().stream().filter(o -> o instanceof JsonArray).map(o -> (JsonArray) o)
                        .forEach(a -> a.getElements().add(new Element(Types.OBJECT, null)));
                return object;
            }),

    JSON_ARRAY_BUILDER_BUILD(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_ARRAY_BUILDER, "build", Types.JSON_ARRAY), (object, arguments) -> {
        Element json = new Element(Types.JSON_ARRAY);
        json.getPossibleValues().addAll(object.getPossibleValues());
        return json;
    }),

    JSON_OBJECT_BUILDER_CREATE(MethodIdentifier.ofStatic(Types.CLASS_JSON, "createObjectBuilder", Types.JSON_OBJECT_BUILDER), (object, arguments) -> new Element(Types.JSON_OBJECT, new JsonObject())),

    JSON_OBJECT_BUILDER_ADD_BIG_DECIMAL(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.BIG_DECIMAL),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.BIG_DECIMAL)),

    JSON_OBJECT_BUILDER_ADD_BIG_INTEGER(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.BIG_INTEGER),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.BIG_INTEGER)),

    JSON_OBJECT_BUILDER_ADD_STRING(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.STRING),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.STRING)),

    JSON_OBJECT_BUILDER_ADD_INT(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.PRIMITIVE_INT),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.INTEGER)),

    JSON_OBJECT_BUILDER_ADD_LONG(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.PRIMITIVE_LONG),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.LONG)),

    JSON_OBJECT_BUILDER_ADD_DOUBLE(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.PRIMITIVE_DOUBLE),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.DOUBLE)),

    JSON_OBJECT_BUILDER_ADD_BOOLEAN(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.PRIMITIVE_BOOLEAN),
            (object, arguments) -> mergeJsonStructure(object, arguments, Types.PRIMITIVE_BOOLEAN)),

    JSON_OBJECT_BUILDER_ADD_JSON(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.JSON_VALUE),
            KnownJsonResultMethod::mergeJsonStructure),

    JSON_OBJECT_BUILDER_ADD_JSON_OBJECT(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.JSON_OBJECT_BUILDER), (object, arguments) ->
            mergeJsonStructure(object, arguments, Types.JSON_OBJECT)),

    JSON_OBJECT_BUILDER_ADD_JSON_ARRAY(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "add", Types.JSON_OBJECT_BUILDER, Types.STRING, Types.JSON_ARRAY_BUILDER), (object, arguments) ->
            mergeJsonStructure(object, arguments, Types.JSON_ARRAY)),

    JSON_OBJECT_BUILDER_ADD_NULL(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "addNull", Types.JSON_OBJECT_BUILDER, Types.STRING),
            (object, arguments) -> {
                object.getPossibleValues().stream()
                        .filter(o -> o instanceof JsonObject).map(o -> (JsonObject) o)
                        .forEach(o -> arguments.get(0).getPossibleValues().stream().map(s -> (String) s)
                                .forEach(s -> o.getStructure().merge(s, new Element(Types.OBJECT, null), Element::merge)));
                return object;
            }),

    JSON_OBJECT_BUILDER_BUILD(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT_BUILDER, "build", Types.JSON_OBJECT), (object, arguments) -> {
        final Element json = new Element(Types.JSON_OBJECT);
        json.getPossibleValues().addAll(object.getPossibleValues());
        return json;
    }),

    JSON_OBJECT_GET_BOOLEAN(MethodIdentifier.ofNonStatic(Types.CLASS_JSON_OBJECT, "getBoolean", Types.PRIMITIVE_BOOLEAN, Types.STRING),
            (object, arguments) -> object.getPossibleValues().stream()
                    .filter(o -> o instanceof JsonObject).map(o -> (JsonObject) o)
                    .map(o -> arguments.get(0).getPossibleValues().stream()
                            .map(s -> (String) s).map(s -> o.getStructure().get(s))
                            .reduce(new Element(Types.PRIMITIVE_BOOLEAN), Element::merge))
                    .reduce(new Element(Types.PRIMITIVE_BOOLEAN), Element::merge));

    private final MethodIdentifier identifier;

    private final BiFunction<Element, List<Element>, Element> function;

    KnownJsonResultMethod(final MethodIdentifier identifier,
                          final BiFunction<Element, List<Element>, Element> function) {
        this.identifier = identifier;
        this.function = function;
    }

    @Override
    public Element invoke(final Element object, final List<Element> arguments) {
        if (arguments.size() != identifier.getParameters().size())
            throw new IllegalArgumentException("Method arguments do not match expected signature!");

        return function.apply(object, arguments);
    }

    @Override
    public boolean matches(final MethodIdentifier identifier) {
        return this.identifier.equals(identifier);
    }

    private static Element addToArray(final Element object, final List<Element> arguments) {
        return addToArray(object, arguments.get(0));
    }

    private static Element addToArray(final Element object, final List<Element> arguments, final String typeOverride) {
        final Element element = new Element(typeOverride);
        element.getPossibleValues().addAll(arguments.get(0).getPossibleValues());
        return addToArray(object, element);
    }

    private static Element addToArray(final Element object, final Element argument) {
        object.getPossibleValues().stream()
                .filter(o -> o instanceof JsonArray).map(o -> (JsonArray) o)
                .forEach(a -> a.getElements().add(argument));
        return object;
    }

    private static Element mergeJsonStructure(final Element object, final List<Element> arguments) {
        final Element element = new Element(arguments.get(1).getTypes());
        element.merge(arguments.get(1));
        return mergeJsonStructure(object, arguments.get(0), element);
    }

    private static Element mergeJsonStructure(final Element object, final List<Element> arguments, final String typeOverride) {
        final Element element = new Element(typeOverride);
        element.getPossibleValues().addAll(arguments.get(1).getPossibleValues());
        return mergeJsonStructure(object, arguments.get(0), element);
    }

    private static Element mergeJsonStructure(final Element object, final Element key, final Element argument) {
        object.getPossibleValues().stream()
                .filter(o -> o instanceof JsonObject).map(o -> (JsonObject) o)
                .forEach(o -> key.getPossibleValues().stream().map(s -> (String) s)
                        .forEach(s -> o.getStructure().merge(s, argument, Element::merge)));
        return object;
    }

}

