package objective.taskboard.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PARENT_LINK_CONFIG")
public class ParentIssueLink extends TaskboardEntity {

    @Column(name = "DESCRIPTION_ISSUE_LINK")
    public String descriptionIssueLink;

    public String getDescriptionIssueLink() {
        return this.descriptionIssueLink;
    }

    public void setDescriptionIssueLink(final String descriptionIssueLink) {
        this.descriptionIssueLink = descriptionIssueLink;
    }

    @Override
    public String toString() {
        return "ParentIssueLink{" +
                "descriptionIssueLink='" + descriptionIssueLink + '\'' +
                '}';
    }
}
