package com.sebastian_daschner.jaxrs_analyzer.backend.asciidoc;

import com.sebastian_daschner.jaxrs_analyzer.LogProvider;
import static com.sebastian_daschner.jaxrs_analyzer.backend.ComparatorUtils.mapKeyComparator;
import static com.sebastian_daschner.jaxrs_analyzer.backend.ComparatorUtils.parameterComparator;
import com.sebastian_daschner.jaxrs_analyzer.backend.JsonDefinitionAppender;
import com.sebastian_daschner.jaxrs_analyzer.backend.StringBackend;
import com.sebastian_daschner.jaxrs_analyzer.model.JavaUtils;
import static com.sebastian_daschner.jaxrs_analyzer.model.JavaUtils.toReadableType;
import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.MethodParameter;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.ParameterType;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.ResourceMethod;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.Response;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeDefinition;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeIdentifier;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation.CollectionTypeRepresentation;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation.ConcreteTypeRepresentation;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation.EnumTypeRepresentation;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentationVisitor;
import com.sebastian_daschner.jaxrs_analyzer.utils.StringUtils;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * A backend implementation which produces an AsciiDoc representation of the
 * JAX-RS project.
 *
 * @author Sebastian Daschner
 */
public class AsciiDocBackend extends StringBackend {

    private static final String NAME = "AsciiDoc";
    private static final String DOCUMENT_TITLE = "= REST resources of ";
    private static final String TYPE_WILDCARD = "\\*/*";

    @Override
    protected void appendMethod(final String baseUri, final String resource, final ResourceMethod resourceMethod) {
        builder.append("=== `").append(resourceMethod.getOperation().isEmpty() ? resourceMethod.getMethod() : resourceMethod.getOperation()).append("`\n\n");
        builder.append("==== Operation").append("\n").append("----\n");
        builder.append(resourceMethod.getMethod()).append(" ");
        if (!StringUtils.isBlank(baseUri)) {
            builder.append(baseUri).append('/');
        }
        builder.append(resource).append("\n----").append("\n\n");
        if (!StringUtils.isBlank(resourceMethod.getDescription())) {
            builder.append("==== Description").append("\n").append(resourceMethod.getDescription()).append("\n\n");
        }
        if (resourceMethod.isDeprecated()) {
            builder.append("CAUTION: deprecated\n\n");
        }
    }

    @Override
    protected void appendRequest(final ResourceMethod resourceMethod) {
        builder.append("==== Request\n");

        if (resourceMethod.getRequestBody() != null) {
            builder.append("*Content-Type*: `");
            builder.append(resourceMethod.getRequestMediaTypes().isEmpty() ? TYPE_WILDCARD : toString(resourceMethod.getRequestMediaTypes()));
            builder.append("` + \n");

            builder.append("*Request Body*: (").append(toTypeOrCollection(resourceMethod.getRequestBody())).append(")");

            if (resourceMethod.getRequestBodyDescription() != null) {
                builder.append("\n\n").append(resourceMethod.getRequestBodyDescription()).append("\n\n");
            }

            Optional.ofNullable(resources.getTypeRepresentations().get(resourceMethod.getRequestBody())).
                    ifPresent(this::generateRequestBodyTables);
            Optional.ofNullable(resources.getTypeRepresentations().get(resourceMethod.getRequestBody())).
                    ifPresent(this::generateSample);

            builder.append("\n");
        } else {
            builder.append("_No body_ + \n");
        }

        final Set<MethodParameter> parameters = resourceMethod.getMethodParameters();

        appendParams("Path Param", parameters, ParameterType.PATH);
        appendParams("Query Param", parameters, ParameterType.QUERY);
        appendParams("Form Param", parameters, ParameterType.FORM);
        appendParams("Header Param", parameters, ParameterType.HEADER);
        appendParams("Cookie Param", parameters, ParameterType.COOKIE);
        appendParams("Matrix Param", parameters, ParameterType.MATRIX);

        builder.append('\n');
    }

    private void appendParams(final String name, final Set<MethodParameter> parameters, final ParameterType parameterType) {
        parameters.stream().filter(p -> p.getParameterType() == parameterType)
                .sorted(parameterComparator()).forEach(p -> builder
                .append('*')
                .append(name)
                .append("*: `")
                .append(p.getName())
                .append("` + \n")
                .append("*Type*: ")
                .append("`")
                .append(toReadableType(p.getType().getType()))
                .append(!StringUtils.isBlank(p.getDescription()) ? "` + \n *Description*: " + p.getDescription() + "\n\n" : ""));

    }

    @Override
    protected void appendResponse(final ResourceMethod resourceMethod) {
        builder.append("==== Response\n");

        builder.append("*Content-Type*: `");
        builder.append(resourceMethod.getResponseMediaTypes().isEmpty() ? TYPE_WILDCARD : toString(resourceMethod.getResponseMediaTypes()));
        builder.append("`\n\n");

        resourceMethod.getResponses().entrySet().stream().sorted(mapKeyComparator()).forEach(e -> {
            builder.append("===== `").append(e.getKey()).append(' ')
                    .append(javax.ws.rs.core.Response.Status.fromStatusCode(e.getKey()).getReasonPhrase()).append("`\n");
            final Response response = e.getValue();
            response.getHeaders().forEach(h -> builder.append("*Header*: `").append(h).append("` + \n"));

            if (response.getResponseBody() != null) {
                builder.append("*Response Body*: ").append('(').append(toTypeOrCollection(response.getResponseBody())).append(")");
                if (e.getValue().getDescription() != null) {
                    builder.append("\n\n").append(e.getValue().getDescription()).append("\n\n");
                }
                TypeIdentifier responseType = TypeIdentifier.ofType(response.getResponseBody().getType());
                Optional.ofNullable(resources.getTypeRepresentations().get(responseType)).
                        ifPresent(this::generateResponseBodyTables);
                Optional.ofNullable(resources.getTypeRepresentations().get(response.getResponseBody())).ifPresent(
                        this::generateSample);
            } else if (e.getValue().getDescription() != null) {
                builder.append("\n\n").append(e.getValue().getDescription()).append("\n\n");
            }
        });
    }

    private void generateRequestBodyTables(TypeRepresentation representation) {

        List<TypeRepresentation> representations = new ArrayList<>();
        representations.add(representation);
        recollectRepresentations(representation, representations);

        representations.forEach(r -> {
            if (r instanceof ConcreteTypeRepresentation) {
                generateRequestBodyTable((ConcreteTypeRepresentation) r);
            } else if (r instanceof CollectionTypeRepresentation) {
                generateRequestBodyTable((CollectionTypeRepresentation) r);
            } else if (r instanceof EnumTypeRepresentation) {
                generateEnumTable((EnumTypeRepresentation) r);
            }
        });
    }

    private void generateResponseBodyTables(TypeRepresentation representation) {

        List<TypeRepresentation> representations = new ArrayList<>();
        representations.add(representation);
        recollectRepresentations(representation, representations);

        representations.forEach(r -> {
            if (r instanceof ConcreteTypeRepresentation) {
                generateResponseBodyTable((ConcreteTypeRepresentation) r);
            } else if (r instanceof CollectionTypeRepresentation) {
                generateResponseBodyTable((CollectionTypeRepresentation) r);
            } else if (r instanceof EnumTypeRepresentation) {
                generateEnumTable((EnumTypeRepresentation) r);
            }
        });
    }

    private void recollectRepresentations(TypeRepresentation representation, List<TypeRepresentation> representations) {
        if (representation instanceof ConcreteTypeRepresentation) {
            ConcreteTypeRepresentation concrete = (ConcreteTypeRepresentation) representation;
            concrete.getProperties().entrySet().stream().sorted(mapKeyComparator()).forEach(e -> {
                TypeIdentifier identifier = e.getValue().getTypeIdentifier();
                TypeRepresentation typeRepresentation = resources.getTypeRepresentations().get(identifier);
                if (Objects.isNull(typeRepresentation) && JavaUtils.isTypeCollection(identifier.getType())) {
                    identifier = TypeIdentifier.ofType(JavaUtils.toSpecificType(identifier.getType()));
                    typeRepresentation = resources.getTypeRepresentations().get(identifier);
                }
                Optional.ofNullable(typeRepresentation).ifPresent(r -> {
                    if (r instanceof CollectionTypeRepresentation) {
                        CollectionTypeRepresentation collection = (CollectionTypeRepresentation) r;
                        if (!JavaUtils.isJDKType(collection.getRepresentation().getIdentifier().getType())) {
                            representations.add(collection.getRepresentation());
                            recollectRepresentations(collection.getRepresentation(), representations);
                        }
                    } else {
                        representations.add(r);
                        recollectRepresentations(r, representations);
                    }
                });

            });
        } else if (representation instanceof CollectionTypeRepresentation) {
            CollectionTypeRepresentation collection = (CollectionTypeRepresentation) representation;
            TypeRepresentation typeRepresentation = resources.getTypeRepresentations().get(collection.getRepresentation().getIdentifier());
            Optional.ofNullable(typeRepresentation).ifPresent(r -> {
                representations.add(r);
                recollectRepresentations(r, representations);
            });
        }
    }

    private void generateRequestBodyTable(ConcreteTypeRepresentation r) {
        builder.append("\n\n").append(".Type ").append(toTypeOrCollection(r.getIdentifier())).append("\n");
        builder.append("[cols=\"5,4,3,10\",options=\"header\"]\n");
        builder.append("|===\n");
        builder.append("|Property |Type |Required? |Description\n\n");
        builder.append(defineObjectBody(r, true));
        builder.append("|===\n\n");
    }

    private void generateRequestBodyTable(CollectionTypeRepresentation r) {
        builder.append("\n\n").append(".Type ").append(toTypeOrCollection(r.getIdentifier())).append("\n");
        builder.append("[cols=\"5,4,3,10\",options=\"header\"]\n");
        builder.append("|===\n");
        builder.append("|Property |Type |Required? |Description\n\n");
        builder.append(defineObjectBody(r, true));
        builder.append("|===\n\n");
    }

    private void generateResponseBodyTable(ConcreteTypeRepresentation r) {
        builder.append("\n\n").append(".Type ").append(toTypeOrCollection(r.getIdentifier())).append("\n");
        builder.append("[cols=\"5,4,10\",options=\"header\"]\n");
        builder.append("|===\n");
        builder.append("|Property |Type |Description\n");
        builder.append(defineObjectBody(r, false));
        builder.append("|===\n\n");
    }

    private void generateResponseBodyTable(CollectionTypeRepresentation r) {
        builder.append("\n\n").append(".Type ").append(toTypeOrCollection(r.getIdentifier())).append("\n");
        builder.append("[cols=\"5,4,10\",options=\"header\"]\n");
        builder.append("|===\n");
        builder.append("|Property |Type |Description\n");
        builder.append(defineObjectBody(r, false));
        builder.append("|===\n\n");
    }

    private String defineObjectBody(final TypeRepresentation typeRepresentation, final boolean hasRequiredColumn) {
        final StringBuilder sBuilder = new StringBuilder();
        final TypeRepresentationVisitor appender = new JsonDefinitionAppender(sBuilder, resources.getTypeRepresentations());
        typeRepresentation.accept(appender);
        final String json = sBuilder.toString();
        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
            JsonObject properties = jsonReader.readObject();
            if (typeRepresentation instanceof ConcreteTypeRepresentation) {
                return createTable(properties, (ConcreteTypeRepresentation) typeRepresentation, hasRequiredColumn);
            } else {
                return createTable(properties, (CollectionTypeRepresentation) typeRepresentation, hasRequiredColumn);
            }
        }
    }

    private String createTable(final JsonObject properties, final ConcreteTypeRepresentation representation, final boolean hasRequiredColumn) {
        StringBuilder sbuilder = new StringBuilder();
        properties.forEach((String key, JsonValue property) -> {
            if (property instanceof JsonObject) {
                buildTableProperty(property.asJsonObject(), sbuilder, key, representation, hasRequiredColumn);
            } else if (property instanceof JsonArray) {
                JsonValue content = ((JsonArray) property).get(0);
                switch (content.getValueType()) {
                    case OBJECT:
                        buildTableProperty(content.asJsonObject(), sbuilder, key, representation, hasRequiredColumn);
                        break;
                    case STRING:
                        buildTableProperty((JsonString) content, sbuilder, key, hasRequiredColumn);
                        break;
                    default:
                        LogProvider.error("Other value type to display on the table");
                        break;
                }
            }
        });
        return sbuilder.toString();
    }

    private void buildTableProperty(JsonObject property, StringBuilder sbuilder, String key, final ConcreteTypeRepresentation representation, final boolean hasRequiredColumn) {
        JsonObject content = (JsonObject) property;
        sbuilder.append("|").append(key).append("\n");
        TypeDefinition definition = representation.getProperties().get(key);
        sbuilder.append("|").append(toTypeOrCollection(definition.getTypeIdentifier())).append("\n");
        if (hasRequiredColumn) {
            sbuilder.append("|").append(content.containsKey("required") ? content.getString("required") : "").append("\n");
        }
        sbuilder.append("|").append(content.containsKey("description") ? content.getString("description") : "").append("\n\n");
    }

    private void buildTableProperty(JsonString property, StringBuilder sbuilder, String key, final boolean hasRequiredColumn) {
        sbuilder.append("|").append(key).append("\n");
        sbuilder.append("|").append("`").append(property.getString()).append("`").append("\n");
        if (hasRequiredColumn) {
            sbuilder.append("|").append("\n");
        }
        sbuilder.append("|").append("\n\n");
    }

    private String createTable(final JsonObject properties, final CollectionTypeRepresentation representation, final boolean hasRequiredColumn) {
        StringBuilder sbuilder = new StringBuilder();
        properties.forEach((key, property) -> {
            if (property instanceof JsonObject) {
                JsonObject content = (JsonObject) property;
                sbuilder.append("|").append(key).append("\n");
                sbuilder.append("|").append(toTypeOrCollection(representation.getComponentType())).append("\n");
                if (hasRequiredColumn) {
                    sbuilder.append("|").append(content.containsKey("required") ? content.getString("required") : "").append("\n");
                }
                sbuilder.append("|").append(content.containsKey("description") ? content.getString("description") : "").append("\n\n");
            }
        });
        return sbuilder.toString();
    }

    private void generateEnumTable(EnumTypeRepresentation r) {
        builder.append("\n\n").append(".Enumeration ").append(toTypeOrCollection(r.getIdentifier())).append("\n");
        builder.append("[width=\"25%\",options=\"header\"]\n");
        builder.append("|===\n");
        builder.append("|Values\n\n");
        r.getEnumValues().stream().sorted().forEach(v -> builder.append("|").append(v).append("\n"));
        builder.append("|===\n\n");
    }

    private void generateSample(TypeRepresentation r) {
        builder.append("\nFor instance:");
        builder.append("\n\n[source,javascript]\n----\n");
        builder.append(doVisit(r));
        builder.append("\n----\n\n");
    }

    private String toTypeOrCollection(final TypeIdentifier type) {
        final TypeRepresentation representation = resources.getTypeRepresentations().get(type);
        if (representation != null && !representation.getComponentType().equals(type) && !type.getType().equals(Types.JSON)) {
            return "Collection of `" + toReadableComponentType(representation.getComponentType()) + '`';
        }
        return '`' + toReadableType(type.getType()) + '`';
    }

    private static String toString(final Set<String> set) {
        return set.stream().sorted().map(Object::toString).collect(Collectors.joining(", "));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void appendFirstLine() {
        builder.append(DOCUMENT_TITLE).append(projectName).append("\n");
    }
}
