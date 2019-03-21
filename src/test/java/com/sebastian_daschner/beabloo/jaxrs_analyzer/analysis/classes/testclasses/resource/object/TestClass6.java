package com.sebastian_daschner.beabloo.jaxrs_analyzer.analysis.classes.testclasses.resource.object;

import com.sebastian_daschner.beabloo.jaxrs_analyzer.model.elements.HttpResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TestClass6 {

    @javax.ws.rs.GET public List<Model> method() {
        if ("".equals(""))
            return new ArrayList<>();
        return new LinkedList<>();
    }

    public static Set<HttpResponse> getResult() {
        final HttpResponse result = new HttpResponse();
        result.getEntityTypes().add("Ljava/util/List<Lcom/sebastian_daschner/jaxrs_analyzer/analysis/classes/testclasses/resource/object/TestClass6$Model;>;");

        return Collections.singleton(result);
    }

    private class Model {
        public Model(final String string) {
        }
    }

}
