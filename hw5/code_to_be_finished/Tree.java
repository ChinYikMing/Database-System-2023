import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class Tree implements TreeInterface {
    protected Node root;
    public Node getRoot() {
        return root;
    }
    public void setRoot(Node root) {
        this.root = root;
    }

    protected int fanout;   // fanout 應該介於 2 ~ initData.size(), 不然沒有加速搜尋作用
    public int getFanout() {
        return fanout;
    }
    public void setFanout(int fanout) {
        this.fanout = fanout;
    }

    protected int maxLevel;
    public int getMaxLevel() {
        return maxLevel;
    }
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    Tree(){
        setRoot(null);
    }

    @Override
    public void init(int fanout) {

    }

    @Override
    public void insert(int key) {
    }

    @Override
    public void delete(String expr) {
        
    }

    @Override
    public List<Integer> lookup(String expr) {
        return null;
    }

    /**
     * get level of tree
     * @param root
     * @return int
     */
    private int getMLevel(Node root){
        if (root == null)
			return 0;

        List<Node> childPtrList = root.getChildPtrList();
        List<Integer> levelArr = new ArrayList<Integer>();
        if(childPtrList != null){
            for(int i = 0; i < childPtrList.size(); i++){
                levelArr.add(getMLevel(childPtrList.get(i)));
            }
        }

        return Collections.max(levelArr) + 1;
    }

    /**
     * li = 2 if i = 0, li = l0 + 2^i if i = 1, 2, 3, ..., h
     * get identation of level
     * @param level
     * @return int
     */
    private int getIdent(int level){
        int maxIdent = (int) (Math.pow(2, getMaxLevel()));
        int identStep = (int) Math.ceil(maxIdent / maxLevel);

        if(level == 0){
            return maxIdent;
        }

        return (int) (maxIdent - Math.pow(2, level));
    }

    @Override
    /**
     * level order
     * @param root
     * @return void
     */
    public void display(Node root, int identation) {
        // level-order
        Queue<Node> queue = new LinkedList<Node>();
        if(root == null){
            return;
        }

        queue.add(root);
        queue.add(null);

        Node nodeIter;
        List<Node> childPtrList;
        List<Integer> keyList = null;
        int level = 0;

        while(!queue.isEmpty()){
            nodeIter = queue.poll();
            if(nodeIter == null){
                level++;
                System.out.println();
                queue.add(null);
                if(queue.peek() == null)
                    break;
                else
                    continue;
            }

            keyList = nodeIter.getKeyList();
            childPtrList = nodeIter.getChildPtrList();

            if(keyList != null){
                for(int i = 0; i < keyList.size(); i++){
                    if(i == keyList.size() - 1){
                        System.out.print("(" + keyList.get(i) + ")");
                    } else {
                        System.out.print("(" + keyList.get(i) + "),");
                    }
                }
            }

            if(childPtrList != null){
                for(int i = 0; i < childPtrList.size(); i++){
                    if(childPtrList.get(i) != null){
                        queue.add(childPtrList.get(i));
                    }
                }
            }
        }

        // in-order
        // if(root == null)
        //     return;

        // List<Integer> keyList = root.getKeyList();
        // List<Node> childPtrList = root.getChildPtrList();

        // display(childPtrList.get(0));
        // System.out.println(keyList.get(0));
        // display(childPtrList.get(1));
    }

    @Override
    public void quit() {
    }
}