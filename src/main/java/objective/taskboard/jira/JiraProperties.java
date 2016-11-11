package objective.taskboard.jira;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jira")
public class JiraProperties {
    
    @NotNull
    @NotEmpty
    private String url;
    @NotNull
    private Lousa lousa;
    @NotNull
    private CustomField customfield;
    @NotNull
    private IssueLink issuelink;
    @NotNull
    private IssueType issuetype;
    @NotNull
    @NotEmpty
    private List<Integer> statusesCompletedIds;
    @NotNull
    @NotEmpty
    private List<Integer> statusesCanceledIds;
    @NotNull
    @NotEmpty
    private List<String> transitionsWithRequiredCommentNames;
    @NotNull
    @NotEmpty
    private List<String> transitionsDoneNames;
    @NotNull
    @NotEmpty
    private List<String> transitionsCancelNames;
    @NotNull
    private Resolutions resolutions;
    
    @Data 
    public static class Lousa {
        @NotNull
        @NotEmpty
        private String username;
        @NotNull
        @NotEmpty
        private String password;
    }
    
    @Data
    public static class CustomField {
        @NotNull
        private TShirtSize tShirtSize; 
        @NotNull
        private ClassOfServiceDetails classOfService;
        @NotNull
        private Blocked blocked;
        @NotNull
        private CustomFieldDetails lastBlockReason;
        @NotNull
        private CustomFieldDetails coAssignees;
        
        @Data
        public static class CustomFieldDetails {
            @NotNull
            @NotEmpty
            private String id; 
        }
        
        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class ClassOfServiceDetails extends CustomFieldDetails {
            private String defaultValue = "Standard";
            private Map<Long, String> colors;
        }
        
        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class TShirtSize extends CustomFieldDetails {
            @NotNull
            @NotEmpty
            private String extraSmall = "XS"; 
            @NotNull
            @NotEmpty
            private String small = "S"; 
            @NotNull
            @NotEmpty
            private String medium = "M";
            @NotNull
            @NotEmpty
            private String large = "L";
            @NotNull
            @NotEmpty
            private String extraLarge = "XL";
        }
        
        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class Blocked extends CustomFieldDetails {
            @NotNull
            @Size(min = 1)
            private Integer yesOptionId;
        }
    }
    
    @Data
    public static class IssueLink {
        @NotNull
        private List<String> dependencies;
        @NotNull
        private LinkDetails demand;
        
        @Data
        public static class LinkDetails {
            @NotNull
            @NotEmpty
            private String name;
        }
    }
    
    @Data
    public static class IssueType {
        @NotNull
        private IssueTypeDetails demand;
        
        @Data
        public static class IssueTypeDetails {
            @NotNull
            @Size(min = 1)
            private int id;
        }
    }
    
    @Data
    public static class Resolutions {
        @NotNull
        private Resolution done;
        @NotNull
        private Resolution canceled;
        
        @Data
        public static class Resolution {
            @NotNull
            @NotEmpty
            private String name;
        }
    }
}
