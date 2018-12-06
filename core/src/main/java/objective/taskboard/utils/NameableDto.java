package objective.taskboard.utils;

public class NameableDto<I> {

    public I id;
    public String name;

    public NameableDto(I id, String name) {
        this.id = id;
        this.name = name;
    }

    public NameableDto() {
    }

}
