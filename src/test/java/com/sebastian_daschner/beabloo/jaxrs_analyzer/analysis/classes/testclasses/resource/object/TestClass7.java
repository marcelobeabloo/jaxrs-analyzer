package com.sebastian_daschner.beabloo.jaxrs_analyzer.analysis.classes.testclasses.resource.object;

import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.elements.HttpResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TestClass7 {

    @javax.ws.rs.GET public List<Model> method() {
        if ("".equals(""))
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return new LinkedList<>();
    }

    public static Set<HttpResponse> getResult() {
        final HttpResponse httpResponse = new HttpResponse();
        httpResponse.getEntityTypes().add("Ljava/util/List<Lcom/sebastian_daschner/jaxrs_analyzer/analysis/classes/testclasses/resource/object/TestClass7$Model;>;");

        final HttpResponse notFoundResponse = new HttpResponse();
        notFoundResponse.getStatuses().add(404);

        return new HashSet<>(Arrays.asList(httpResponse, notFoundResponse));
    }

    private class Model {
        public Model(final String string) {
        }
    }

}
