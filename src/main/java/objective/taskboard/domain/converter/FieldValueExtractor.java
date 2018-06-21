package objective.taskboard.domain.converter;

import static java.util.stream.Collectors.joining;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.jira.client.JiraFieldDataDto;
import objective.taskboard.jira.client.JiraFieldSchemaDto;
import objective.taskboard.jira.client.JiraFieldSchemaDto.CustomFieldTypes;
import objective.taskboard.jira.client.JiraFieldSchemaDto.FieldSchemaType;
import objective.taskboard.jira.client.JiraIssueDto;

public class FieldValueExtractor {

    private static final Logger log = LoggerFactory.getLogger(FieldValueExtractor.class);

    public static final String UNSUPPORTED_EXTRACTION_VALUE = "UNSUPPORTED FIELD EXTRACTION";

    public static final Casting<String> asString = (obj -> obj == null ? null : String.valueOf(obj));

    public static <T, R> R from(T object, Extractor<T, R> extractor) {
        return extractor.apply(object);
    }

    public static Extractor<JiraIssueDto, String> extractExtraFieldValue(JiraFieldDataDto fieldInfo) {
        return issue -> {
            JiraFieldSchemaDto schema = fieldInfo.getSchema();
            try {
                if (FieldSchemaType.array == schema.getType()
                        && FieldSchemaType.option == schema.getItems()
                        && CustomFieldTypes.multiselect == schema.getCustom())
                    return extract(issue.getField(fieldInfo.getId())
                            , ofArray(ofProperty("value", asString)))
                            .map(stream -> stream.collect(joining()))
                            .orElse("");
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            return UNSUPPORTED_EXTRACTION_VALUE;
        };
    }

    private static <T, R> Optional<R> extract(T fromObject, Function<T, R> transformer) {
        return Optional.ofNullable(fromObject == null ? null : transformer.apply(fromObject));
    }

    public static <R> Function<JSONArray, Stream<R>> ofArray(final Function<Object, R> elementTransformer) {
        return arrayValue -> arrayValue == null ? null
                : IntStream.range(0, arrayValue.length())
                .mapToObj(arrayValue::opt)
                .map(elementTransformer);
    }

    public static <R> Function<Object, R> ofProperty(final String propertyName, Casting<R> casting) {
        return obj -> {
            if(obj == null)
                return null;

            if(obj instanceof JSONObject)
                return casting.apply(((JSONObject) obj).opt(propertyName));

            throw new IllegalArgumentException("Was expecting JSONObject, got " + obj.getClass());
        };
    }

    public interface Extractor<T, R> extends Function<T, R> {}
    public interface Casting<R> extends Function<Object, R> {}
}
