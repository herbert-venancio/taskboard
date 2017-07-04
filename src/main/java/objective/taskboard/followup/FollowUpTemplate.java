package objective.taskboard.followup;

import lombok.Getter;

@Getter
public class FollowUpTemplate {

    private String pathSharedStringsInitial;
    private String pathSharedStringsTemplate;
    private String pathSISharedStringsTemplate;
    private String pathSheet7Template;
    private String pathSheet7RowTemplate;
    private String pathFollowupTemplateXLSM;

    public FollowUpTemplate(String pathSharedStringsInitial
            , String pathSharedStringsTemplate
            , String pathSISharedStringsTemplate
            , String pathSheet7Template
            , String pathSheet7RowTemplate
            , String pathFollowupTemplateXLSM) {
        this.pathSharedStringsInitial = pathSharedStringsInitial;
        this.pathSharedStringsTemplate = pathSharedStringsTemplate;
        this.pathSISharedStringsTemplate = pathSISharedStringsTemplate;
        this.pathSheet7Template = pathSheet7Template;
        this.pathSheet7RowTemplate = pathSheet7RowTemplate;
        this.pathFollowupTemplateXLSM = pathFollowupTemplateXLSM;
    }
}
