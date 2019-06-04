package objective.taskboard.controller;


import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.json.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.database.AuditService;

@RestController
@RequestMapping(value = "/audit")
public class AuditController {

    private final AuditService auditService;
    private final JsonConverter jsonConverter;
    private final Map<String, Class<?>> typeNameMap;

    @Autowired
    public AuditController(AuditService auditService, Javers javers) {
        this.auditService = auditService;
        this.jsonConverter = javers.getJsonConverter();
        typeNameMap = mapTypes(auditService.getAuditedTypes());
    }

    private static Map<String, Class<?>> mapTypes(Collection<Class<?>> types) {
        return types.stream()
                .collect(toMap(type ->
                                StringUtils.join(
                                        StringUtils.splitByCharacterTypeCamelCase(type.getSimpleName()),
                                        "-")
                                        .toLowerCase(Locale.ENGLISH)
                        , Function.identity()));
    }

    @GetMapping(value = "/{type}/changes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getEntityChanges(@PathVariable("type") String typeName) {
        return Optional.ofNullable(typeNameMap.get(typeName))
                .map(this::getChanges)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @GetMapping(value = "/{type}/snapshots", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getEntitySnapshots(@PathVariable("type") String typeName) {
        return Optional.ofNullable(typeNameMap.get(typeName))
                .map(this::getSnapshots)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private String getChanges(Class<?> type) {
        return jsonConverter.toJson(auditService.getChanges(type));
    }

    private String getSnapshots(Class<?> type) {
        return jsonConverter.toJson(auditService.getSnapshots(type));
    }

}
