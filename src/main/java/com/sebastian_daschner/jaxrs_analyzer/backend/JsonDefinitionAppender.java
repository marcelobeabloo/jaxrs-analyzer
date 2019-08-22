package com.sebastian_daschner.jaxrs_analyzer.backend;

import static com.sebastian_daschner.jaxrs_analyzer.backend.ComparatorUtils.mapKeyComparator;
import com.sebastian_daschner.jaxrs_analyzer.model.Types;
import static com.sebastian_daschner.jaxrs_analyzer.model.Types.BOOLEAN;
import static com.sebastian_daschner.jaxrs_analyzer.model.Types.DOUBLE_TYPES;
import static com.sebastian_daschner.jaxrs_analyzer.model.Types.INTEGER_TYPES;
import static com.sebastian_daschner.jaxrs_analyzer.model.Types.PRIMITIVE_BOOLEAN;
import static com.sebastian_daschner.jaxrs_analyzer.model.Types.STRING;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeIdentifier;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentation;
import com.sebastian_daschner.jaxrs_analyzer.model.rest.TypeRepresentationVisitor;
import com.sebastian_daschner.jaxrs_analyzer.utils.StringUtils;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Adds the JSON definition of type identifiers to String builders
 */
public class JsonDefinitionAppender implements TypeRepresentationVisitor {

    private final StringBuilder builder;
    private final Map<TypeIdentifier, TypeRepresentation> representations;

    public JsonDefinitionAppender(final StringBuilder builder, final Map<TypeIdentifier, TypeRepresentation> representations) {
        this.builder = builder;
        this.representations = representations;
    }

    @Override
    public void visit(TypeRepresentation.ConcreteTypeRepresentation representation) {
        if (representation.getProperties().isEmpty()) {
            builder.append(toDefinitionType(representation.getIdentifier()));
        } else {
            builder.append('{');
            representation.getProperties().entrySet().stream().sorted(mapKeyComparator()).forEach(e -> {
                builder.append('"').append(e.getKey()).append("\":");
                builder.append("{");
                builder.append("\"type\":").append(toDefinitionType(e.getValue().getTypeIdentifier()));
                if (Objects.nonNull(e.getValue().getDescription())) {
                    String description = StringUtils.remove(e.getValue().getDescription(), "\n");
                    builder.append(",").append("\"description\":\"").append(description).append("\"");
                }
                if (Objects.nonNull(e.getValue().getRequired())) {
                    builder.append(",").append("\"required\":\"").append(e.getValue().getRequired()).append("\"");
                }
                builder.append("}");
                builder.append(',');
            });
            builder.deleteCharAt(builder.length() - 1).append('}');
        }
    }

    @Override
    public void visitStart(TypeRepresentation.CollectionTypeRepresentation representation) {
        builder.append('[');
    }

    @Override
    public void visitEnd(TypeRepresentation.CollectionTypeRepresentation representation) {
        builder.append(']');
    }

    @Override
    public void visit(final TypeRepresentation.EnumTypeRepresentation representation) {
        final String values = representation.getEnumValues().stream().sorted().collect(Collectors.joining("|"));

        if (values.isEmpty()) {
            builder.append("\"String\"");
        } else {
            builder.append('"').append("String. Allowed values : ").append(values).append('"');
        }
    }

    private static String toDefinitionType(final TypeIdentifier value) {
        final String type = value.getType();

        if (STRING.equals(type)) {
            return "\"String\"";
        }

        if (Types.DATE.equals(type)) {
            return "\"Timestamp\"";
        }

        if (BOOLEAN.equals(type) || PRIMITIVE_BOOLEAN.equals(type)) {
            return "\"Boolean\"";
        }

        if (INTEGER_TYPES.contains(type)) {
            return "\"Integer\"";
        }

        if (DOUBLE_TYPES.contains(type)) {
            return "\"Decimal\"";
        }

        return "{}";
    }
}
