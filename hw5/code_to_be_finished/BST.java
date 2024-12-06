import java.util.ArrayList;
import java.util.List;

public class BST extends Tree {
    public BST(){
        super();
        setFanout(2);
    }

    @Override
    public List<Integer> lookup(String expr) {
        List<Integer> res = new ArrayList<Integer>();
        if(this.root == null){
            return null;
        }

        int key = Integer.parseInt(expr);
        if(this.root.getKeyList().get(0) == key){
            res.add(key);
            return res;
            // return this.root;
        }

        Node nodeIter = this.root;
        while(nodeIter != null){
            if(nodeIter.getKeyList().get(0) == key){
                res.add(nodeIter.getKeyList().get(0));
                return res;
                // return nodeIter;
            }

            if(key < nodeIter.getKeyList().get(0)){
                nodeIter = nodeIter.getChildPtrList().get(0);
            } else if(key >= nodeIter.getKeyList().get(0)){
                nodeIter = nodeIter.getChildPtrList().get(1);
            }
        }

        return null;
    }

    @Override
    public void insert(int key){
        if(this.root == null){
            this.root = new Node(getFanout(), false);
            this.root.getKeyList().set(0, key);
            return;
        }

        Node nodeNew = new Node(getFanout(), false);;
        nodeNew.getKeyList().set(0, key);

        Node nodeIter = this.root;
        Node nodeIterPar = this.root;

        while(nodeIter != null){
            nodeIterPar = nodeIter;
            if(key < nodeIter.getKeyList().get(0)){
                nodeIter = nodeIter.getChildPtrList().get(0);
            } else if(key >= nodeIter.getKeyList().get(0)){
                nodeIter = nodeIter.getChildPtrList().get(1);
            }
        }

        if(key < nodeIterPar.getKeyList().get(0)){
            nodeIterPar.getChildPtrList().set(0, nodeNew);
        } else if(key >= nodeIterPar.getKeyList().get(0)){
            nodeIterPar.getChildPtrList().set(1, nodeNew);
        }
    }

    /**
     * update node left or right child node
     * @param node
     */
    private void update(Node node){
        if(node.getChildPtrList().get(1) != null){
            // try to find min node of right side of root to replace root
            Node nodeIter = node.getChildPtrList().get(1);
            Node nodeIterPar = nodeIter;

            if(nodeIter.getChildPtrList().get(0) != null){
                do {
                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getChildPtrList().get(0);
                } while(nodeIter.getChildPtrList().get(0) != null);

                node.getKeyList().set(0, nodeIter.getKeyList().get(0));
                nodeIterPar.getChildPtrList().set(0, nodeIter.getChildPtrList().get(1));
            } else {
                node.getKeyList().set(0, nodeIter.getKeyList().get(0));
                node.getChildPtrList().set(1, nodeIter.getChildPtrList().get(1));
            }
        } else if(node.getChildPtrList().get(0) != null){
            // try to find max node of left side of root to replace root
            Node nodeIter = node.getChildPtrList().get(0);
            Node nodeIterPar = nodeIter;

            if(nodeIter.getChildPtrList().get(1) != null){
                do {
                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getChildPtrList().get(1);
                } while(nodeIter.getChildPtrList().get(1) != null);

                node.getKeyList().set(0, nodeIter.getKeyList().get(0));
                nodeIterPar.getChildPtrList().set(1, nodeIter.getChildPtrList().get(0));
            } else {
                node.getKeyList().set(0, nodeIter.getKeyList().get(0));
                node.getChildPtrList().set(0, nodeIter.getChildPtrList().get(0));
            }
        }
    }

    @Override
    public void delete(String expr) {
        if(this.root == null){
            return;
        }

        int key = Integer.parseInt(expr);
        if(this.root.getKeyList().get(0) == key){
            if(this.root.getChildPtrList().get(0) == null && // leaf node
                this.root.getChildPtrList().get(1) == null){
                this.root = null;
            } else {
                update(this.root);
            }
            return;
        }

        Node nodeIter = this.root;
        Node nodeIterPar = nodeIter;

        do {
            if(nodeIter.getKeyList().get(0) == key){
                if(nodeIter.getChildPtrList().get(0) == null && // leaf node
                    nodeIter.getChildPtrList().get(1) == null){
                    if(nodeIter.getKeyList().get(0) < nodeIterPar.getKeyList().get(0)){
                        nodeIterPar.getChildPtrList().set(0, null);
                    } else {
                        nodeIterPar.getChildPtrList().set(1, null);
                    }
                } else {
                    update(nodeIter);
                }
                break;
            }

            if(key < nodeIter.getKeyList().get(0)){
                nodeIterPar = nodeIter;
                nodeIter = nodeIter.getChildPtrList().get(0);
            } else if(key >= nodeIter.getKeyList().get(0)){
                nodeIterPar = nodeIter;
                nodeIter = nodeIter.getChildPtrList().get(1);
            }
        } while(nodeIter != null);
    }
}
