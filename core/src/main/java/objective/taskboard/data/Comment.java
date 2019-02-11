package objective.taskboard.data;

import objective.taskboard.jira.client.JiraCommentDto;


import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    public Date created;
    public String author;
    public String body;
    public String avatar;

    public Comment(Date created, String author, String body, String avatar){
        this.created = created;
        this.author = author;
        this.body = body;
        this.avatar = avatar;
    }

    public static Comment from(JiraCommentDto c){
        return new Comment(c.created, c.author.getDisplayName(), c.body, c.author.getAvatarUri("32x32").toASCIIString());
    }
}
