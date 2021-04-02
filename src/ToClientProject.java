import Model.Project;

import java.io.Serializable;

public class ToClientProject implements Serializable {
    private String message;
    private Project project;

    public ToClientProject(String message, Project project) {
        this.message = message;
        this.project = project;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
