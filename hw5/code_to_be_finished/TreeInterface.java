import java.util.List;

public interface TreeInterface {
    abstract void init(int fanout);
    abstract void insert(int key);
    abstract void delete(String expr);
    abstract List<Integer> lookup(String expr);
    abstract void display(Node root, int identation);
    abstract void quit();
}