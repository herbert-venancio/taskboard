package objective.taskboard.issueBuffer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class RequiresReindexException extends IllegalStateException {

	private static final long serialVersionUID = 1L;
	
	private final List<String> missingParents;

    public RequiresReindexException(List<String> missingParents) {
        super("Some parents were never found: " + StringUtils.join(missingParents,","));
        this.missingParents = missingParents;
    }

    public List<String> getMissingParents() {
        return missingParents;
    }
}
