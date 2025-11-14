package eu.devunit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TodoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("todo")
                .executes(TodoCommand::listAllTodos)
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("task", StringArgumentType.greedyString())
                                .executes(TodoCommand::addUnassignedTodo)))
                .then(CommandManager.literal("assign")
                        .then(CommandManager.argument("task", StringArgumentType.string())
                                .then(CommandManager.argument("assignee", StringArgumentType.word())
                                        .executes(TodoCommand::addAssignedTodo))))
                .then(CommandManager.literal("take")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer(1))
                                .executes(TodoCommand::takeTodo)))
                .then(CommandManager.literal("list")
                        .executes(TodoCommand::listAllTodos)
                        .then(CommandManager.literal("unassigned")
                                .executes(TodoCommand::listUnassignedTodos))
                        .then(CommandManager.literal("mine")
                                .executes(TodoCommand::listMyTodos))
                        .then(CommandManager.argument("assignee", StringArgumentType.word())
                                .executes(TodoCommand::listTodosByAssignee)))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer(1))
                                .executes(TodoCommand::removeTodo)))
                .then(CommandManager.literal("clear")
                        .executes(TodoCommand::clearTodos)));
    }

    private static int addUnassignedTodo(CommandContext<ServerCommandSource> context) {
        String task = StringArgumentType.getString(context, "task");
        String creator = context.getSource().getName();

        TodoItem item = TodoManager.addUnassignedTodo(task, creator);
        context.getSource().sendFeedback(() ->
                Text.literal("§aTodo added: " + item.toString()), false);
        return 1;
    }

    private static int addAssignedTodo(CommandContext<ServerCommandSource> context) {
        String task = StringArgumentType.getString(context, "task");
        String assignee = StringArgumentType.getString(context, "assignee");
        String creator = context.getSource().getName();

        TodoItem item = TodoManager.addTodo(task, creator, assignee);
        context.getSource().sendFeedback(() ->
                Text.literal("§aTodo added: " + item.toString()), false);
        return 1;
    }

    private static int takeTodo(CommandContext<ServerCommandSource> context) {
        int id = IntegerArgumentType.getInteger(context, "id");
        String player = context.getSource().getName();

        TodoItem todo = TodoManager.getTodo(id);
        if (todo == null) {
            context.getSource().sendFeedback(() ->
                    Text.literal("§cTodo #" + id + " not found"), false);
            return 0;
        }

        if (!todo.isUnassigned()) {
            context.getSource().sendFeedback(() ->
                    Text.literal("§cTodo #" + id + " is already assigned to " + todo.getAssignee()), false);
            return 0;
        }

        TodoManager.takeTodo(id, player);
        context.getSource().sendFeedback(() ->
                Text.literal("§aYou took todo: " + todo.toString()), false);
        return 1;
    }

    private static int listAllTodos(CommandContext<ServerCommandSource> context) {
        var todos = TodoManager.getAllTodos();
        if (todos.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("§eNo todos found"), false);
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("§6=== All Todos ==="), false);
        for (TodoItem todo : todos) {
            context.getSource().sendFeedback(() -> Text.literal(todo.toString()), false);
        }
        return todos.size();
    }

    private static int listUnassignedTodos(CommandContext<ServerCommandSource> context) {
        var todos = TodoManager.getUnassignedTodos();
        if (todos.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("§eNo unassigned todos found"), false);
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("§6=== Unassigned Todos ==="), false);
        for (TodoItem todo : todos) {
            context.getSource().sendFeedback(() -> Text.literal(todo.toString()), false);
        }
        return todos.size();
    }

    private static int listMyTodos(CommandContext<ServerCommandSource> context) {
        String player = context.getSource().getName();
        var todos = TodoManager.getTodosByAssignee(player);

        if (todos.isEmpty()) {
            context.getSource().sendFeedback(() ->
                    Text.literal("§eYou have no assigned todos"), false);
            return 0;
        }

        context.getSource().sendFeedback(() ->
                Text.literal("§6=== Your Todos ==="), false);
        for (TodoItem todo : todos) {
            context.getSource().sendFeedback(() -> Text.literal(todo.toString()), false);
        }
        return todos.size();
    }

    private static int listTodosByAssignee(CommandContext<ServerCommandSource> context) {
        String assignee = StringArgumentType.getString(context, "assignee");
        var todos = TodoManager.getTodosByAssignee(assignee);

        if (todos.isEmpty()) {
            context.getSource().sendFeedback(() ->
                    Text.literal("§eNo todos found for " + assignee), false);
            return 0;
        }

        context.getSource().sendFeedback(() ->
                Text.literal("§6=== Todos for " + assignee + " ==="), false);
        for (TodoItem todo : todos) {
            context.getSource().sendFeedback(() -> Text.literal(todo.toString()), false);
        }
        return todos.size();
    }

    private static int removeTodo(CommandContext<ServerCommandSource> context) {
        int id = IntegerArgumentType.getInteger(context, "id");

        if (TodoManager.removeTodo(id)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("§aTodo #" + id + " removed"), false);
            return 1;
        } else {
            context.getSource().sendFeedback(() ->
                    Text.literal("§cTodo #" + id + " not found"), false);
            return 0;
        }
    }

    private static int clearTodos(CommandContext<ServerCommandSource> context) {
        TodoManager.clearAll();
        context.getSource().sendFeedback(() -> Text.literal("§aAll todos cleared"), false);
        return 1;
    }
}