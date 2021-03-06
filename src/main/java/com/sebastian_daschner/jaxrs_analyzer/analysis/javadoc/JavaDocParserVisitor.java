package com.sebastian_daschner.jaxrs_analyzer.analysis.javadoc;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.sebastian_daschner.jaxrs_analyzer.model.javadoc.ClassComment;
import com.sebastian_daschner.jaxrs_analyzer.model.javadoc.MemberParameterTag;
import com.sebastian_daschner.jaxrs_analyzer.model.javadoc.MethodComment;
import com.sebastian_daschner.jaxrs_analyzer.model.methods.MethodIdentifier;
import static com.sebastian_daschner.jaxrs_analyzer.model.methods.MethodIdentifier.ofNonStatic;
import static com.sebastian_daschner.jaxrs_analyzer.model.methods.MethodIdentifier.ofStatic;
import com.sebastian_daschner.jaxrs_analyzer.utils.Pair;
import com.sebastian_daschner.jaxrs_analyzer.utils.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sebastian Daschner
 */
public class JavaDocParserVisitor extends VoidVisitorAdapter<Void> {

    private String packageName;
    private String className;
    private final Map<MethodIdentifier, MethodComment> methodComments;
    private final Map<String, ClassComment> classComments;

    public JavaDocParserVisitor(Map<MethodIdentifier, MethodComment> methodComments, Map<String, ClassComment> classComments) {
        this.methodComments = methodComments;
        this.classComments = classComments;
    }

    @Override
    public void visit(PackageDeclaration packageDeclaration, Void arg) {
        packageName = packageDeclaration.getNameAsString();
        super.visit(packageDeclaration, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterface, Void arg) {
        className = calculateClassName(classOrInterface);

        classOrInterface.getComment()
                .filter(Comment::isJavadocComment)
                .map(this::toJavaDoc)
                .ifPresent(this::recordClassComment);

        super.visit(classOrInterface, arg);
    }

    private Javadoc toJavaDoc(Comment comment) {
        return comment.asJavadocComment().parse();
    }

    private boolean isDeprecated(Javadoc javadoc) {
        return javadoc.getBlockTags().stream().anyMatch(t -> t.getType() == JavadocBlockTag.Type.DEPRECATED);
    }

    private String calculateClassName(ClassOrInterfaceDeclaration classOrInterface) {
        if (StringUtils.isBlank(packageName)) {
            return classOrInterface.getNameAsString();
        }
        return packageName.replace('.', '/') + "/" + classOrInterface.getNameAsString();
    }

    private void recordClassComment(Javadoc javadoc) {
        String comment = javadoc.getDescription().toText();
        Map<Integer, String> responseComments = createResponseComments(javadoc);
        classComments.put(className, new ClassComment(comment, responseComments, isDeprecated(javadoc)));
    }

    @Override
    public void visit(FieldDeclaration field, Void arg) {
        field.getComment()
                .filter(Comment::isJavadocComment)
                .map(this::toJavaDoc)
                .ifPresent(c -> createFieldComment(c, field));
        super.visit(field, arg);
    }

    private void createFieldComment(Javadoc javadoc, FieldDeclaration field) {
        ClassComment classComment = classComments.get(className);
        if (classComment == null) {
            classComment = new ClassComment();
            classComments.put(className, classComment);
        }
        String fieldType = field.getElementType().asString();
        String fieldName = field.getVariables().get(0).getNameAsString();
        classComment.getFieldComments().add(createMemberParamTag(fieldType, fieldName, javadoc.getDescription(), field.getAnnotations().stream()));
    }

    @Override
    public void visit(MethodDeclaration method, Void arg) {
        method.getComment()
                .filter(Comment::isJavadocComment)
                .map(this::toJavaDoc)
                .ifPresent(c -> recordMethodComment(c, method));
        super.visit(method, arg);
    }

    private void recordMethodComment(Javadoc javadoc, MethodDeclaration method) {
        MethodIdentifier identifier = calculateMethodIdentifier(method);
        String comment = javadoc.getDescription().toText();
        Optional<AnnotationExpr> annotOperation = method.getAnnotationByName("Operation");
        String operation = annotOperation.isPresent() ? getAnnotationValue(annotOperation.get()) : "";
        List<MemberParameterTag> tags = createMethodParameterTags(javadoc, method);
        Map<Integer, String> responseComments = createResponseComments(javadoc);
        methodComments.put(identifier, new MethodComment(comment, tags, responseComments, classComments.get(className), isDeprecated(javadoc), operation));
    }

    private List<MemberParameterTag> createMethodParameterTags(Javadoc javadoc, MethodDeclaration method) {
        return javadoc.getBlockTags().stream()
                .filter(t -> t.getType() == JavadocBlockTag.Type.PARAM)
                .map(t -> createMethodParameterTag(t, method))
                .collect(Collectors.toList());
    }

    private MemberParameterTag createMethodParameterTag(JavadocBlockTag tag, MethodDeclaration method) {
        Stream<AnnotationExpr> annotations = method.getParameterByName(tag.getName().orElse(null))
                .map(Parameter::getAnnotations)
                .map(NodeList::stream)
                .orElseGet(Stream::empty);

        return createMemberParamTag(tag.getType().name(), tag.getName().orElse("unknown"), tag.getContent(), annotations);
    }

    private MemberParameterTag createMemberParamTag(String type, String name, JavadocDescription javadocDescription, Stream<AnnotationExpr> annotationStream) {
        Map<String, String> annotations = annotationStream
                .filter(Expression::isSingleMemberAnnotationExpr)
                .collect(Collectors.toMap(a -> a.getName().getIdentifier(),
                        this::createMemberParamValue));
        return new MemberParameterTag(type, name, javadocDescription.toText(), annotations);
    }

    private String createMemberParamValue(AnnotationExpr a) {
        Expression memberValue = a.asSingleMemberAnnotationExpr().getMemberValue();
        if (memberValue.getClass().isAssignableFrom(StringLiteralExpr.class)) {
            return memberValue.asStringLiteralExpr().asString();
        }

        if (memberValue.getClass().isAssignableFrom(NameExpr.class)) {
            return memberValue.asNameExpr().getNameAsString();
        }

        throw new IllegalArgumentException(String.format("Javadoc param type (%s) not supported.", memberValue.toString()));
    }

    private Map<Integer, String> createResponseComments(Javadoc javadoc) {
        return javadoc.getBlockTags().stream()
                .filter(t -> ResponseCommentExtractor.RESPONSE_TAG_NAME.equalsIgnoreCase(t.getTagName())
                || ResponseCommentExtractor.RETURN_TAG_NAME.equalsIgnoreCase(t.getTagName()))
                .map(t -> t.getContent().toText())
                .map(ResponseCommentExtractor::extract)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    /**
     * <b>Note:</b> This will not return the actual identifier but only the
     * simple names of the types (return type &amp; parameter types). Doing a
     * full type resolving with all imports adds too much complexity at this
     * point. This is a best-effort approach.
     */
    private MethodIdentifier calculateMethodIdentifier(MethodDeclaration method) {
        String[] parameters = method.getParameters().stream()
                .map(p -> p.getType().asString())
                .map(p -> p.replace('.', '/'))
                .toArray(String[]::new);
        String returnType = method.getType().asString().replace('.', '/');

        if (method.isStatic()) {
            return ofStatic(className, method.getNameAsString(), returnType, parameters);
        }
        return ofNonStatic(className, method.getNameAsString(), returnType, parameters);
    }

    private String getAnnotationValue(AnnotationExpr annotationExpr) {
        Expression expression = getParameter(annotationExpr, "value");
        if (expression == null) {
            List<Expression> children = annotationExpr.getChildNodesByType(Expression.class);
            if (!children.isEmpty()) {
                expression = children.get(0);
            }
        }
        return Objects.isNull(expression) ? "" : expression.asStringLiteralExpr().getValue();
    }

    public static Expression getParameter(AnnotationExpr annotationExpr, String parameterName) {
        List<MemberValuePair> children = annotationExpr.getChildNodesByType(MemberValuePair.class);
        for (MemberValuePair memberValuePair : children) {
            if (parameterName.equals(memberValuePair.getNameAsString())) {
                return memberValuePair.getValue();
            }
        }
        return null;
    }

}
