package eu.devunit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class TodoManager {
    private static final Map<Integer, TodoItem> todos = new HashMap<>();
    private static final Path SAVE_FILE = FabricLoader.getInstance().getConfigDir().resolve("todolist.json");
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(TodoItem.class, new TodoItem.TodoItemAdapter())
            .setPrettyPrinting()
            .create();

    public static void loadTodos() {
        if (!Files.exists(SAVE_FILE)) {
            return;
        }

        try {
            String json = Files.readString(SAVE_FILE);
            Type listType = new TypeToken<List<TodoItem>>(){}.getType();
            List<TodoItem> todoList = gson.fromJson(json, listType);

            todos.clear();
            if (todoList != null) {
                for (TodoItem todo : todoList) {
                    todos.put(todo.getId(), todo);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load todos: " + e.getMessage());
        }
    }

    public static void saveTodos() {
        try {
            List<TodoItem> todoList = new ArrayList<>(todos.values());
            String json = gson.toJson(todoList);

            Files.createDirectories(SAVE_FILE.getParent());
            Files.writeString(SAVE_FILE, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save todos: " + e.getMessage());
        }
    }

    public static TodoItem addTodo(String task, String creator, String assignee) {
        TodoItem item = new TodoItem(task, creator, assignee);
        todos.put(item.getId(), item);
        saveTodos();
        return item;
    }

    public static TodoItem addUnassignedTodo(String task, String creator) {
        return addTodo(task, creator, "unassigned");
    }

    public static boolean removeTodo(int id) {
        boolean removed = todos.remove(id) != null;
        if (removed) {
            saveTodos();
        }
        return removed;
    }

    public static TodoItem getTodo(int id) {
        return todos.get(id);
    }

    public static boolean takeTodo(int id, String newAssignee) {
        TodoItem todo = todos.get(id);
        if (todo != null) {
            todo.setAssignee(newAssignee);
            saveTodos();
            return true;
        }
        return false;
    }

    public static List<TodoItem> getAllTodos() {
        return new ArrayList<>(todos.values());
    }

    public static List<TodoItem> getTodosByAssignee(String assignee) {
        return todos.values().stream()
                .filter(todo -> todo.getAssignee().equalsIgnoreCase(assignee))
                .collect(Collectors.toList());
    }

    public static List<TodoItem> getUnassignedTodos() {
        return todos.values().stream()
                .filter(TodoItem::isUnassigned)
                .collect(Collectors.toList());
    }

    public static void clearAll() {
        todos.clear();
        saveTodos();
    }
}