package com.sebastian_daschner.jaxrs_analyzer.model.rest;

import java.util.Objects;

public class TypeDefinition {

    private TypeIdentifier typeIdentifier;
    private Integer length;
    private Boolean required;
    private String description;

    public TypeIdentifier getTypeIdentifier() {
        return typeIdentifier;
    }

    public void setTypeIdentifier(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.typeIdentifier);
        hash = 53 * hash + Objects.hashCode(this.length);
        hash = 53 * hash + Objects.hashCode(this.required);
        hash = 53 * hash + Objects.hashCode(this.description);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeDefinition other = (TypeDefinition) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.typeIdentifier, other.typeIdentifier)) {
            return false;
        }
        if (!Objects.equals(this.length, other.length)) {
            return false;
        }
        if (!Objects.equals(this.required, other.required)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TypeDefinition{" + "typeIdentifier=" + typeIdentifier + ", length=" + length + ", required=" + required + ", description=" + description + '}';
    }

    public static TypeDefinition of(TypeIdentifier identifier) {
        TypeDefinition definition = new TypeDefinition();
        definition.setTypeIdentifier(identifier);
        return definition;
    }

    public static TypeDefinition of(TypeIdentifier identifier, Boolean required, String javadoc) {
        TypeDefinition definition = new TypeDefinition();
        definition.setTypeIdentifier(identifier);
        if (Objects.nonNull(required) && required) {
            definition.setRequired(required);
        }
        definition.setDescription(javadoc);
        return definition;
    }

}
