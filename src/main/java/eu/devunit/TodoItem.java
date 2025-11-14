package eu.devunit;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TodoItem {
    private static int nextId = 1;

    private final int id;
    private String task;
    private String creator;
    private String assignee;
    private LocalDateTime createdDate;

    public TodoItem(String task, String creator, String assignee) {
        this.id = nextId++;
        this.task = task;
        this.creator = creator;
        this.assignee = assignee;
        this.createdDate = LocalDateTime.now();
    }

    // Constructor for deserialization
    public TodoItem(int id, String task, String creator, String assignee, LocalDateTime createdDate) {
        this.id = id;
        this.task = task;
        this.creator = creator;
        this.assignee = assignee;
        this.createdDate = createdDate;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    // Getters and setters
    public int getId() { return id; }
    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public LocalDateTime getCreatedDate() { return createdDate; }

    public boolean isUnassigned() {
        return assignee == null || assignee.isEmpty() || assignee.equals("unassigned");
    }

    @Override
    public String toString() {
        String assigneeStr = isUnassigned() ? "§7Unassigned" : "§b" + assignee;
        return String.format("§f#%d: §e%s §7| Creator: §a%s §7| Assignee: %s §7| Created: §6%s",
                id, task, creator, assigneeStr,
                createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    // JSON serializer/deserializer
    public static class TodoItemAdapter implements JsonSerializer<TodoItem>, JsonDeserializer<TodoItem> {
        @Override
        public JsonElement serialize(TodoItem src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.id);
            obj.addProperty("task", src.task);
            obj.addProperty("creator", src.creator);
            obj.addProperty("assignee", src.assignee);
            obj.addProperty("createdDate", src.createdDate.toString());
            return obj;
        }

        @Override
        public TodoItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new TodoItem(
                    obj.get("id").getAsInt(),
                    obj.get("task").getAsString(),
                    obj.get("creator").getAsString(),
                    obj.get("assignee").getAsString(),
                    LocalDateTime.parse(obj.get("createdDate").getAsString())
            );
        }
    }
}