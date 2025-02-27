import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subTasksIds;

    public Epic(String name, String description, int id) {
        super(name, description, id);
        this.subTasksIds = new ArrayList<Integer>();
    }

    public Epic(String name, String description, int id, List<Integer> subTasksIds, TaskStatus status) {
        super(name, description, id, status);
        this.subTasksIds = subTasksIds;
    }

    public List<Integer> getSubTasks() {
        return subTasksIds;
    }

    public void setSubTasks(List<Integer> subTasks) {
        this.subTasksIds = subTasks;
    }

    public void addSubTask(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public void removeSubTask(int subTaskId) {
        subTasksIds.remove(Integer.valueOf(subTaskId));
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", subTasks=" + subTasksIds +
                '}';
    }
}
