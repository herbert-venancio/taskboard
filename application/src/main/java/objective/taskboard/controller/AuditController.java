package objective.taskboard.controller;


import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.json.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{type}/changes")
    public ResponseEntity getEntityChanges(@PathVariable("type") String typeName) {
        return Optional.ofNullable(typeNameMap.get(typeName))
                .map(type -> ResponseEntity.ok(getChanges(type)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{type}/snapshots")
    public ResponseEntity getEntitySnapshots(@PathVariable("type") String typeName) {
        return Optional.ofNullable(typeNameMap.get(typeName))
                .map(type -> ResponseEntity.ok(getSnapshots(type)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("{type}/restore")
    public ResponseEntity restoreEntity(@PathVariable("type") String typeName, @RequestBody RestoreRequest request) {
        return Optional.ofNullable(typeNameMap.get(typeName))
                .map(type -> restore(request, type))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public static class RestoreRequest {

        private String id;
        private BigDecimal commitId;

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public BigDecimal getCommitId() {
            return commitId;
        }
        public void setCommitId(BigDecimal commitId) {
            this.commitId = commitId;
        }
    }

    private String getChanges(Class<?> type) {
        return jsonConverter.toJson(auditService.getChanges(type));
    }

    private String getSnapshots(Class<?> type) {
        return jsonConverter.toJson(auditService.getSnapshots(type));
    }

    private ResponseEntity restore(RestoreRequest request, Class<?> type) {
        try {
            auditService.restore(request.getId(), request.getCommitId(), type);
            return ResponseEntity.ok("ok");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
