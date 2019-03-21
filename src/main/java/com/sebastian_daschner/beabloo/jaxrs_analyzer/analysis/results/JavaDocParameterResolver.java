package com.sebastian_daschner.beabloo.jaxrs_analyzer.analysis.results;

import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.JavaUtils;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.Types;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.javadoc.ClassComment;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.javadoc.MemberParameterTag;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.javadoc.MethodComment;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.rest.MethodParameter;
import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.rest.ParameterType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Resolves the actual {@code *Param} parameters analyzed by both the JavaDoc and Bytecode analysis.
 *
 * @author Sebastian Daschner
 */
final class JavaDocParameterResolver {

    private static final String[] KNOWN_ANNOTATIONS = {Types.PATH_PARAM, Types.QUERY_PARAM, Types.HEADER_PARAM, Types.FORM_PARAM, Types.COOKIE_PARAM, Types.MATRIX_PARAM, Types.DEFAULT_VALUE, Types.SUSPENDED, Types.CONTEXT};

    private JavaDocParameterResolver() {
        throw new UnsupportedOperationException();
    }

    static Optional<MemberParameterTag> findParameterDoc(final MethodParameter parameter, final MethodComment methodDoc) {
        return methodDoc.getParamTags().stream()
                .filter(p -> hasAnnotation(parameter, p.getAnnotations()))
                .findAny();
    }

    static Optional<MemberParameterTag> findFieldDoc(final MethodParameter parameter, final ClassComment classDoc) {
        if (classDoc == null)
            return Optional.empty();

        return classDoc.getFieldComments().stream()
                .filter(f -> hasAnnotation(parameter, f.getAnnotations()))
                .findAny();
    }

    static Optional<MemberParameterTag> findRequestBodyDoc(final MethodComment methodDoc) {
        return methodDoc.getParamTags().stream()
                .filter(p -> isRequestBody(p.getAnnotations()))
                .findAny();
    }

    private static boolean hasAnnotation(final MethodParameter parameter, final Map<String, String> annotations) {
        return annotations.entrySet().stream()
                .filter(e -> annotationTypeMatches(e.getKey(), parameter.getParameterType()))
                .anyMatch(e -> Objects.equals(e.getValue(), parameter.getName()));
    }

    private static boolean isRequestBody(final Map<String, String> annotations) {
        return annotations.entrySet().stream()
                .noneMatch(e -> findKnownAnnotation(e.getKey()));
    }

    private static boolean findKnownAnnotation(String simpleTypeName) {
        return Stream.of(KNOWN_ANNOTATIONS).anyMatch(a -> a.contains(simpleTypeName));
    }

    private static boolean annotationTypeMatches(final String qualifiedTypeName, final ParameterType parameterType) {
        String javaType = getJavaType(parameterType);
        return javaType != null && javaType.contains(qualifiedTypeName);
    }

    private static String getJavaType(final ParameterType parameterType) {
        switch (parameterType) {
            case PATH:
                return JavaUtils.toReadableType(Types.PATH_PARAM);
            case QUERY:
                return JavaUtils.toReadableType(Types.QUERY_PARAM);
            case HEADER:
                return JavaUtils.toReadableType(Types.HEADER_PARAM);
            case FORM:
                return JavaUtils.toReadableType(Types.FORM_PARAM);
            case MATRIX:
                return JavaUtils.toReadableType(Types.MATRIX_PARAM);
            case COOKIE:
                return JavaUtils.toReadableType(Types.COOKIE_PARAM);
            default:
                return null;
        }
    }

}
