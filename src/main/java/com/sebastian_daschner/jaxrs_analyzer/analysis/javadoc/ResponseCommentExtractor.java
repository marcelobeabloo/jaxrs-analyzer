package com.sebastian_daschner.jaxrs_analyzer.analysis.javadoc;

import com.sebastian_daschner.jaxrs_analyzer.LogProvider;
import com.sebastian_daschner.jaxrs_analyzer.utils.Pair;

class ResponseCommentExtractor {

    private ResponseCommentExtractor() {
    }

    static final String RESPONSE_TAG_NAME = "response";
    static final String RETURN_TAG_NAME = "return";

    static Pair<Integer, String> extract(String comment) {
        try {
            String commentText = comment.trim();
            String statusPart = commentText.split("\\s")[0];
            int status = Integer.parseInt(statusPart);
            return Pair.of(status, commentText.substring(statusPart.length()).trim());
        } catch (Exception e) {
            LogProvider.info("@response JavaDoc tag was not found for : '"+ comment + "'. Using @return pattern with the default http status code 200");
            return extractByReturnAnnotation(comment);
        }
    }

    static Pair<Integer, String> extractByReturnAnnotation(String comment) {
        String commentText = comment.trim();
        int status = 200;
        return Pair.of(status, commentText);
    }


}
