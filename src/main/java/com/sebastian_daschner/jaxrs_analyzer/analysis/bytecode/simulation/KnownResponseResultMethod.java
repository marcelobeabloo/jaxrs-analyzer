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

import com.sebastian_daschner.jaxrs_analyzer.model.JavaUtils;
import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.Element;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.HttpResponse;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.JsonValue;
import com.sebastian_daschner.jaxrs_analyzer.model.elements.MethodHandle;
import com.sebastian_daschner.jaxrs_analyzer.model.methods.IdentifiableMethod;
import com.sebastian_daschner.jaxrs_analyzer.model.methods.Method;
import com.sebastian_daschner.jaxrs_analyzer.model.methods.MethodIdentifier;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Known methods which apply logic to the result or to the return element.
 *
 * @author Sebastian Daschner
 */
enum KnownResponseResultMethod implements IdentifiableMethod {

    // non-static methods in ResponseBuilder --------------------------

    RESPONSE_BUILDER_BUILD(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "build", Types.RESPONSE), (object, arguments) -> object),

    RESPONSE_BUILDER_CACHE_CONTROL(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "cacheControl", Types.RESPONSE_BUILDER, "Ljavax/ws/rs/core/CacheControl;"), (object, arguments) ->
            addHeader(object, HttpHeaders.CACHE_CONTROL)),

    RESPONSE_BUILDER_CONTENT_LOCATION(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "contentLocation", Types.RESPONSE_BUILDER, Types.URI), (object, arguments) ->
            addHeader(object, HttpHeaders.CONTENT_LOCATION)),

    RESPONSE_BUILDER_COOKIE(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "cookie", Types.RESPONSE_BUILDER, "[Ljavax/ws/rs/core/NewCookie;"), (object, arguments) ->
            addHeader(object, HttpHeaders.SET_COOKIE)),

    RESPONSE_BUILDER_ENCODING(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "encoding", Types.RESPONSE_BUILDER, Types.STRING), (object, arguments) ->
            addHeader(object, HttpHeaders.CONTENT_ENCODING)),

    RESPONSE_BUILDER_ENTITY(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "entity", Types.RESPONSE_BUILDER, Types.OBJECT), (object, arguments) ->
            addEntity(object, arguments.get(0))),

    RESPONSE_BUILDER_ENTITY_ANNOTATION(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "entity", Types.RESPONSE_BUILDER, Types.OBJECT, "[Ljava/lang/annotation/Annotation;"), (object, arguments) ->
            addEntity(object, arguments.get(0))),

    RESPONSE_BUILDER_EXPIRES(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "expires", Types.RESPONSE_BUILDER, Types.DATE), (object, arguments) ->
            addHeader(object, HttpHeaders.EXPIRES)),

    RESPONSE_BUILDER_HEADER(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "header", Types.RESPONSE_BUILDER, Types.STRING, Types.OBJECT), (object, arguments) -> {
        arguments.get(0).getPossibleValues().stream()
                .map(header -> (String) header).forEach(h -> addHeader(object, h));
        return object;
    }),

    RESPONSE_BUILDER_LANGUAGE_LOCALE(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "language", Types.RESPONSE_BUILDER, "Ljava/util/Locale;"), (object, arguments) ->
            addHeader(object, HttpHeaders.CONTENT_LANGUAGE)),

    RESPONSE_BUILDER_LANGUAGE_STRING(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "language", Types.RESPONSE_BUILDER, Types.STRING), (object, arguments) ->
            addHeader(object, HttpHeaders.CONTENT_LANGUAGE)),

    RESPONSE_BUILDER_LAST_MODIFIED(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "lastModified", Types.RESPONSE_BUILDER, Types.DATE), (object, arguments) ->
            addHeader(object, HttpHeaders.LAST_MODIFIED)),

    RESPONSE_BUILDER_LINK_URI(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "link", Types.RESPONSE_BUILDER, Types.URI, Types.STRING), (object, arguments) ->
            addHeader(object, HttpHeaders.LINK)),

    RESPONSE_BUILDER_LINK_STRING(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "link", Types.RESPONSE_BUILDER, Types.STRING, Types.STRING), (object, arguments) ->
            addHeader(object, HttpHeaders.LINK)),

    RESPONSE_BUILDER_LINKS(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "links", Types.RESPONSE_BUILDER, "[Ljavax/ws/rs/core/Link;"), (object, arguments) ->
            addHeader(object, HttpHeaders.LINK)),

    RESPONSE_BUILDER_LOCATION(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "location", Types.RESPONSE_BUILDER, Types.URI), (object, arguments) ->
            addHeader(object, HttpHeaders.LOCATION)),

    RESPONSE_BUILDER_STATUS_ENUM(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "status", Types.RESPONSE_BUILDER, Types.RESPONSE_STATUS), (object, arguments) -> {
        arguments.get(0).getPossibleValues().stream()
                .map(status -> ((Response.Status) status).getStatusCode()).forEach(s -> addStatus(object, s));
        return object;
    }),

    RESPONSE_BUILDER_STATUS_INT(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "status", Types.RESPONSE_BUILDER, Types.PRIMITIVE_INT), (object, arguments) -> {
        arguments.get(0).getPossibleValues().stream()
                .map(status -> (int) status).forEach(s -> addStatus(object, s));
        return object;
    }),

    RESPONSE_BUILDER_TAG_ENTITY(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "tag", Types.RESPONSE_BUILDER, Types.ENTITY_TAG), (object, arguments) ->
            addHeader(object, HttpHeaders.ETAG)),

    RESPONSE_BUILDER_TAG_STRING(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "tag", Types.RESPONSE_BUILDER, Types.STRING), (object, arguments) ->
            addHeader(object, HttpHeaders.ETAG)),

    RESPONSE_BUILDER_TYPE(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "type", Types.RESPONSE_BUILDER, "Ljavax/ws/rs/core/MediaType;"), (object, arguments) -> {
        arguments.get(0).getPossibleValues().stream()
                .map(m -> (MediaType) m).map(m -> m.getType() + '/' + m.getSubtype()).forEach(t -> addContentType(object, t));
        return object;
    }),

    RESPONSE_BUILDER_TYPE_STRING(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "type", Types.RESPONSE_BUILDER, Types.STRING), (object, arguments) -> {
        arguments.get(0).getPossibleValues().stream()
                .map(t -> (String) t).forEach(t -> addContentType(object, t));
        return object;
    }),

    RESPONSE_BUILDER_VARIANT(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "variant", Types.RESPONSE_BUILDER, Types.VARIANT), (object, arguments) -> {
        addHeader(object, HttpHeaders.CONTENT_LANGUAGE);
        addHeader(object, HttpHeaders.CONTENT_ENCODING);
        return object;
    }),

    RESPONSE_BUILDER_VARIANTS_LIST(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "variants", Types.RESPONSE_BUILDER, Types.LIST), (object, arguments) ->
            addHeader(object, HttpHeaders.VARY)),

    RESPONSE_BUILDER_VARIANTS_ARRAY(MethodIdentifier.ofNonStatic(Types.CLASS_RESPONSE_BUILDER, "variants", Types.RESPONSE_BUILDER, "[Ljavax/ws/rs/core/Variant;"), (object, arguments) ->
            addHeader(object, HttpHeaders.VARY)),

    // static methods in Response --------------------------

    RESPONSE_STATUS_ENUM(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "status", Types.RESPONSE_BUILDER, Types.RESPONSE_STATUS), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(0).getPossibleValues().stream()
                .map(status -> ((Response.Status) status).getStatusCode()).forEach(s -> addStatus(object, s));
        return object;
    }),

    RESPONSE_STATUS_INT(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "status", Types.RESPONSE_BUILDER, Types.PRIMITIVE_INT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(0).getPossibleValues().stream()
                .map(status -> (int) status).forEach(s -> addStatus(object, s));
        return object;
    }),

    RESPONSE_OK(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "ok", Types.RESPONSE_BUILDER), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.OK.getStatusCode());
    }),

    RESPONSE_OK_ENTITY(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "ok", Types.RESPONSE_BUILDER, Types.OBJECT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.OK.getStatusCode());
        return addEntity(object, arguments.get(0));
    }),

    RESPONSE_OK_VARIANT(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "ok", Types.RESPONSE_BUILDER, Types.OBJECT, Types.VARIANT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.OK.getStatusCode());
        addEntity(object, arguments.get(0));
        addHeader(object, HttpHeaders.CONTENT_LANGUAGE);
        return addHeader(object, HttpHeaders.CONTENT_ENCODING);
    }),

    RESPONSE_OK_MEDIATYPE(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "ok", Types.RESPONSE_BUILDER, Types.OBJECT, "Ljavax/ws/rs/core/MediaType;"), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.OK.getStatusCode());
        arguments.get(1).getPossibleValues().stream().map(m -> (MediaType) m)
                .map(m -> m.getType() + '/' + m.getSubtype()).forEach(t -> addContentType(object, t));
        return addEntity(object, arguments.get(0));
    }),

    RESPONSE_OK_MEDIATYPE_STRING(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "ok", Types.RESPONSE_BUILDER, Types.OBJECT, Types.STRING), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.OK.getStatusCode());
        arguments.get(1).getPossibleValues().stream()
                .map(t -> (String) t).forEach(t -> addContentType(object, t));
        return addEntity(object, arguments.get(0));
    }),

    RESPONSE_ACCEPTED(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "accepted", Types.RESPONSE_BUILDER), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.ACCEPTED.getStatusCode());
    }),

    RESPONSE_ACCEPTED_ENTITY(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "accepted", Types.RESPONSE_BUILDER, Types.OBJECT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.ACCEPTED.getStatusCode());
        return addEntity(object, arguments.get(0));
    }),

    RESPONSE_CREATED(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "created", Types.RESPONSE_BUILDER, Types.URI), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.CREATED.getStatusCode());
        return addHeader(object, HttpHeaders.LOCATION);
    }),

    RESPONSE_NO_CONTENT(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "noContent", Types.RESPONSE_BUILDER), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.NO_CONTENT.getStatusCode());
    }),

    RESPONSE_NOT_ACCEPTABLE(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "notAcceptable", Types.RESPONSE_BUILDER, Types.LIST), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.NOT_ACCEPTABLE.getStatusCode());
        return addHeader(object, HttpHeaders.VARY);
    }),

    RESPONSE_NOT_MODIFIED(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "notModified", Types.RESPONSE_BUILDER), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.NOT_MODIFIED.getStatusCode());
    }),

    RESPONSE_NOT_MODIFIED_ENTITYTAG(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "notModified", Types.RESPONSE_BUILDER, Types.ENTITY_TAG), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.NOT_MODIFIED.getStatusCode());
        return addHeader(object, HttpHeaders.ETAG);
    }),

    RESPONSE_NOT_MODIFIED_ENTITYTAG_STRING(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "notModified", Types.RESPONSE_BUILDER, Types.STRING), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.NOT_MODIFIED.getStatusCode());
        return addHeader(object, HttpHeaders.ETAG);
    }),

    RESPONSE_SEE_OTHER(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "seeOther", Types.RESPONSE_BUILDER, Types.URI), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.SEE_OTHER.getStatusCode());
        return addHeader(object, HttpHeaders.LOCATION);
    }),

    RESPONSE_TEMPORARY_REDIRECT(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "temporaryRedirect", Types.RESPONSE_BUILDER, Types.URI), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        addStatus(object, Response.Status.TEMPORARY_REDIRECT.getStatusCode());
        return addHeader(object, HttpHeaders.LOCATION);
    }),

    RESPONSE_SERVER_ERROR(MethodIdentifier.ofStatic(Types.CLASS_RESPONSE, "serverError", Types.RESPONSE_BUILDER), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }),

    // WebApplicationExceptions --------------------------

    WEB_APPLICATION_EXCEPTION_EMPTY(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }),

    WEB_APPLICATION_EXCEPTION_MESSAGE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }),

    WEB_APPLICATION_EXCEPTION_RESPONSE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.RESPONSE), (notAvailable, arguments) -> arguments.get(0)),

    WEB_APPLICATION_EXCEPTION_MESSAGE_RESPONSE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.RESPONSE),
            (notAvailable, arguments) -> arguments.get(1)),

    WEB_APPLICATION_EXCEPTION_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.PRIMITIVE_INT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(0).getPossibleValues().stream()
                .map(status -> (int) status).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_MESSAGE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.PRIMITIVE_INT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(1).getPossibleValues().stream()
                .map(status -> (int) status).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_RESPONSE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.RESPONSE_STATUS), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(0).getPossibleValues().stream()
                .map(status -> ((Response.Status) status).getStatusCode()).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_MESSAGE_RESPONSE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.RESPONSE_STATUS), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(1).getPossibleValues().stream()
                .map(status -> ((Response.Status) status).getStatusCode()).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_CAUSE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.THROWABLE), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }),

    WEB_APPLICATION_EXCEPTION_MESSAGE_CAUSE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.THROWABLE), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        return addStatus(object, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }),

    WEB_APPLICATION_EXCEPTION_CAUSE_RESPONSE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.THROWABLE, Types.RESPONSE),
            (notAvailable, arguments) -> arguments.get(1)),

    WEB_APPLICATION_EXCEPTION_MESSAGE_CAUSE_RESPONSE(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.THROWABLE, Types.RESPONSE),
            (notAvailable, arguments) -> arguments.get(2)),

    WEB_APPLICATION_EXCEPTION_CAUSE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.THROWABLE, Types.PRIMITIVE_INT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(1).getPossibleValues().stream()
                .map(status -> (int) status).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_MESSAGE_CAUSE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.THROWABLE, Types.PRIMITIVE_INT), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(2).getPossibleValues().stream()
                .map(status -> (int) status).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_CAUSE_RESPONSE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.THROWABLE, Types.RESPONSE_STATUS), (notAvailable, arguments) -> {
        final Element object = new Element(Types.RESPONSE, new HttpResponse());
        arguments.get(1).getPossibleValues().stream()
                .map(status -> ((Response.Status) status).getStatusCode()).forEach(s -> addStatus(object, s));
        return object;
    }),

    WEB_APPLICATION_EXCEPTION_MESSAGE_CAUSE_RESPONSE_STATUS(MethodIdentifier.ofNonStatic(Types.CLASS_WEB_APPLICATION_EXCEPTION, JavaUtils.INITIALIZER_NAME, Types.PRIMITIVE_VOID, Types.STRING, Types.THROWABLE, Types.RESPONSE_STATUS),
            (notAvailable, arguments) -> {
                final Element object = new Element(Types.RESPONSE, new HttpResponse());
                arguments.get(2).getPossibleValues().stream()
                        .map(status -> ((Response.Status) status).getStatusCode()).forEach(s -> addStatus(object, s));
                return object;
            }),

    // other methods --------------------------

    RESOURCE_CONTEXT_INIT(MethodIdentifier.ofNonStatic(Types.CLASS_RESOURCE_CONTEXT, "getResource", Types.OBJECT, Types.CLASS),
            (object, arguments) -> new Element(arguments.get(0).getPossibleValues().stream()
                    .filter(s -> s instanceof String).map(s -> (String) s).collect(Collectors.toSet()))
    ),

    RESOURCE_CONTEXT_GET(MethodIdentifier.ofNonStatic(Types.CLASS_RESOURCE_CONTEXT, "initResource", Types.OBJECT, Types.OBJECT),
            (object, arguments) -> new Element(arguments.get(0).getTypes())),

    INTEGER_VALUE_OF(MethodIdentifier.ofStatic(Types.CLASS_INTEGER, "valueOf", Types.PRIMITIVE_INT, Types.INTEGER),
            (object, arguments) -> new Element(Types.INTEGER, arguments.get(0).getPossibleValues().toArray())),

    DOUBLE_VALUE_OF(MethodIdentifier.ofStatic(Types.CLASS_DOUBLE, "valueOf", Types.PRIMITIVE_DOUBLE, Types.DOUBLE),
            (object, arguments) -> new Element(Types.INTEGER, arguments.get(0).getPossibleValues().toArray())),

    LONG_VALUE_OF(MethodIdentifier.ofStatic(Types.CLASS_LONG, "valueOf", Types.PRIMITIVE_LONG, Types.LONG),
            (object, arguments) -> new Element(Types.INTEGER, arguments.get(0).getPossibleValues().toArray())),

    // stream related methods --------------------------

    LIST_STREAM(MethodIdentifier.ofNonStatic(Types.CLASS_LIST, "stream", Types.STREAM),
            (object, arguments) -> new Element(object.getTypes())),

    LIST_FOR_EACH(MethodIdentifier.ofNonStatic(Types.CLASS_LIST, "forEach", Types.PRIMITIVE_VOID, Types.CONSUMER), (object, arguments) -> {
        if (arguments.get(0) instanceof MethodHandle)
            ((Method) arguments.get(0)).invoke(null, Collections.singletonList(object));
        return null;
    }),

    SET_STREAM(MethodIdentifier.ofNonStatic(Types.CLASS_SET, "stream", Types.STREAM),
            (object, arguments) -> new Element(object.getTypes())),

    SET_FOR_EACH(MethodIdentifier.ofNonStatic(Types.CLASS_SET, "forEach", Types.PRIMITIVE_VOID, Types.CONSUMER), (object, arguments) -> {
        if (arguments.get(0) instanceof MethodHandle)
            ((Method) arguments.get(0)).invoke(null, Collections.singletonList(object));
        return null;
    }),

    STREAM_COLLECT(MethodIdentifier.ofNonStatic(Types.CLASS_STREAM, "collect", Types.OBJECT, Types.SUPPLIER, Types.BI_CONSUMER, Types.BI_CONSUMER),
            (object, arguments) -> {
                if (arguments.get(0) instanceof MethodHandle && arguments.get(1) instanceof MethodHandle) {
                    final Element collectionElement = ((Method) arguments.get(0)).invoke(null, Collections.emptyList());
                    ((Method) arguments.get(1)).invoke(null, Arrays.asList(collectionElement, object));
                    return collectionElement;
                }
                return new Element();
            }),

    STREAM_FOR_EACH(MethodIdentifier.ofNonStatic(Types.CLASS_STREAM, "forEach", Types.PRIMITIVE_VOID, Types.CONSUMER), (object, arguments) -> {
        if (arguments.get(0) instanceof MethodHandle)
            ((Method) arguments.get(0)).invoke(null, Collections.singletonList(object));
        return null;
    }),

    STREAM_MAP(MethodIdentifier.ofNonStatic(Types.CLASS_STREAM, "map", Types.STREAM, "Ljava/util/function/Function;"), (object, arguments) -> {
        if (arguments.get(0) instanceof MethodHandle) {
            return ((MethodHandle) arguments.get(0)).invoke(null, Collections.singletonList(object));
        }
        return new Element();
    });

    private final MethodIdentifier identifier;

    private final BiFunction<Element, List<Element>, Element> function;

    KnownResponseResultMethod(final MethodIdentifier identifier,
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

    private static Element addHeader(final Element object, final String header) {
        object.getPossibleValues().stream().filter(r -> r instanceof HttpResponse).map(r -> (HttpResponse) r).forEach(r -> r.getHeaders().add(header));
        return object;
    }

    private static Element addEntity(final Element object, final Element argument) {
        object.getPossibleValues().stream().filter(r -> r instanceof HttpResponse).map(r -> (HttpResponse) r)
                .forEach(r -> {
                    r.getEntityTypes().addAll(argument.getTypes());
                    argument.getPossibleValues().stream().filter(j -> j instanceof JsonValue).map(j -> (JsonValue) j).forEach(j -> r.getInlineEntities().add(j));
                });
        return object;
    }

    private static Element addStatus(final Element object, final Integer status) {
        object.getPossibleValues().stream().filter(r -> r instanceof HttpResponse).map(r -> (HttpResponse) r).forEach(r -> r.getStatuses().add(status));
        return object;
    }

    private static Element addContentType(final Element object, final String type) {
        object.getPossibleValues().stream().filter(r -> r instanceof HttpResponse).map(r -> (HttpResponse) r).forEach(r -> r.getContentTypes().add(type));
        return object;
    }

}