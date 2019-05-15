package objective.taskboard.testUtils;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JsonBuilder<T> {

    static JsonArrayBuilder array() {
        return new JsonArrayBuilder();
    }

    static JsonArrayBuilder array(JsonBuilder<?>... elements) {
        return new JsonArrayBuilder(elements);
    }

    static JsonObjectBuilder object() {
        return new JsonObjectBuilder();
    }

    static JsonObjectBuilder object(JsonObjectPropertyBuilder... properties) {
        return new JsonObjectBuilder(properties);
    }

    static JsonConstantBuilder constant(boolean value) {
        return new JsonConstantBuilder(value);
    }

    static JsonConstantBuilder constant(Number value) {
        return new JsonConstantBuilder(value);
    }

    static JsonConstantBuilder constant(String value) {
        return new JsonConstantBuilder(value);
    }

    static JsonObjectPropertyBuilder property(String name, boolean value) {
        return new JsonObjectPropertyBuilder(name, value);
    }

    static JsonObjectPropertyBuilder property(String name, Number value) {
        return new JsonObjectPropertyBuilder(name, value);
    }

    static JsonObjectPropertyBuilder property(String name, String value) {
        return new JsonObjectPropertyBuilder(name, value);
    }

    static JsonObjectPropertyBuilder property(String name, JsonBuilder<?> value) {
        return new JsonObjectPropertyBuilder(name, value);
    }

    T build();

    T build(T json);

    class JsonArrayBuilder implements JsonBuilder<JSONArray> {

        private List<JsonBuilder<?>> elementBuilders = new ArrayList<>();

        protected JsonArrayBuilder() {
        }

        protected JsonArrayBuilder(JsonBuilder<?>... builders) {
            this.elementBuilders.addAll(asList(builders));
        }

        @Override
        public JSONArray build() {
            return build(new JSONArray());
        }

        @Override
        public JSONArray build(JSONArray json) {
            elementBuilders.forEach(elementBuilder -> json.put(elementBuilder.build()));
            return json;
        }
    }

    class JsonObjectBuilder implements JsonBuilder<JSONObject> {

        protected List<JsonObjectPropertyBuilder> propertyBuilders = new ArrayList<>();

        protected JsonObjectBuilder() {
        }

        protected JsonObjectBuilder(JsonObjectPropertyBuilder... properties) {
            this.propertyBuilders.addAll(asList(properties));
        }

        public JsonObjectBuilder add(JsonObjectPropertyBuilder property) {
            this.propertyBuilders.add(property);
            return this;
        }

        @Override
        public JSONObject build() {
            return build(new JSONObject());
        }

        @Override
        public JSONObject build(JSONObject json) {
            propertyBuilders.forEach(property -> property.build(json));
            return json;
        }

        public <T> T as(Class<T> type) {
            String json = build().toString();
            try {
                return new ObjectMapper().readValue(json, type);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    class JsonObjectPropertyBuilder {

        private String name;
        private JsonBuilder<?> value;

        protected JsonObjectPropertyBuilder(String name, boolean value) {
            this(name, new JsonConstantBuilder(value));
        }

        protected JsonObjectPropertyBuilder(String name, Number value) {
            this(name, new JsonConstantBuilder(value));
        }

        protected JsonObjectPropertyBuilder(String name, String value) {
            this(name, new JsonConstantBuilder(value));
        }

        protected JsonObjectPropertyBuilder(String name, JsonBuilder<?> value) {
            this.name = name;
            this.value = value;
        }

        public JSONObject build(JSONObject json) {
            try {
                return json.put(name, value.build());
            } catch (JSONException ex) {
                return rethrow(ex);
            }
        }
    }

    class JsonConstantBuilder implements JsonBuilder<Object> {

        private Object constant;

        protected JsonConstantBuilder() {
            this.constant = null;
        }

        protected JsonConstantBuilder(boolean constant) {
            this.constant = constant;
        }

        protected JsonConstantBuilder(Number constant) {
            this.constant = constant;
        }

        protected JsonConstantBuilder(String constant) {
            this.constant = constant;
        }

        @Override
        public Object build() {
            return constant;
        }

        @Override
        public Object build(Object json) {
            return json;
        }

    }

}

