package objective.taskboard.auth.authorizer.permission;

public abstract class BasePermission implements Permission {

    private final String name;

    public BasePermission(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
