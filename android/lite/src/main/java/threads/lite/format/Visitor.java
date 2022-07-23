package threads.lite.format;

import androidx.annotation.NonNull;

import java.util.Stack;

public class Visitor {
    private final Stack<Stage> stack = new Stack<>();
    private boolean rootVisited;

    public Visitor(@NonNull Node root) {
        rootVisited = false;
        pushActiveNode(root);
    }

    public void reset(@NonNull Stack<Stage> stages) {
        stack.clear();
        stack.addAll(stages);
        rootVisited = true;
    }

    public void pushActiveNode(@NonNull Node node) {
        stack.push(new Stage(node));
    }

    public void popStage() {
        stack.pop();
    }

    public Stage peekStage() {
        return stack.peek();
    }

    public boolean isRootVisited(boolean visited) {
        boolean temp = rootVisited;
        rootVisited = visited;
        return temp;
    }

    public boolean isPresent() {
        return !stack.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        String result = "";
        for (Stage stage : stack) {
            result = result.concat(stage.toString());
        }
        return result;
    }
}
