import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.swing.tree.DefaultMutableTreeNode;

public class BPT extends Tree {
    private int pageSize = 8; // in practice at least 4KB

    private int maxSizePerPage = -1;
    public int getMaxSizePerPage() {
        return maxSizePerPage;
    }
    public void setMaxSizePerPage(int maxSizePerPage) {
        this.maxSizePerPage = maxSizePerPage;
    }

    private int halfFull = -1;
    public int getHalfFull() {
        return halfFull;
    }
    public void setHalfFull(int halfFull) {
        this.halfFull = halfFull;
    }

    private int successorKey = -1;
    public int getSuccessorKey() {
        return successorKey;
    }
    public void setSuccessorKey(int successorKey) {
        this.successorKey = successorKey;
    }

    private int key = -1;
    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }

    public BPT(){

    }

    private <E> E getLast(Collection<E> c) {
        E last = null;
        for(E e : c) last = e;
        return last;
    }

    private <E> E getSecondLast(Collection<E> c) {
        E last = getLast(c);
        c.remove(last);
        E secondLast = getLast(c);
        c.add(last);
        return secondLast;
    }

    @Override
    /**
     * @param fanout fanout is order in BPT, real fanout = order x 2 + 1, also is half full capacity
     * @return void
     */
    public void init(int fanout) { 
        setFanout((fanout << 1) + 1);
        setMaxSizePerPage(fanout << 1);
        setHalfFull(fanout);
        setRoot(null);
    }

    @Override
    public void insert(int key){
        Node root = getRoot();

        if(root == null){
            Node newPage = new Node(getFanout(), true, false, maxSizePerPage);
            newPage.getKeyList().add(key);
            setRoot(newPage);
            return;
        } else {
            StringBuffer keyExper = new StringBuffer("= " + Integer.toString(key));
            List<Integer> res = lookup(keyExper.toString());
            if(res.size() != 0){ // key exists
                System.out.println("Duplicated key");
                return;
            }

            // invariant: key not exists
            List<Node> _visited = new ArrayList<Node>();
            Node nodeIter = root;
            List<Node> childPtrList = null;
            List<Integer> keyList = null;
            Set<Node> visited = null;

            _visited.add(nodeIter);
            childPtrList = nodeIter.getChildPtrList();
            keyList = nodeIter.getKeyList();
            while(!nodeIter.getIsLeaf()){
                childPtrList = nodeIter.getChildPtrList();
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(i == (keyList.size() - 1)){
                        if(key < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                        } else if(key >= keyList.get(i)){
                            nodeIter = childPtrList.get(i + 1);
                        }
                    } else {
                        if(key < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                            _visited.add(nodeIter);
                            break;
                        } 
                    }
                }

                _visited.add(nodeIter);
            }
            visited = new LinkedHashSet<Node>(_visited);
            keyList = nodeIter.getKeyList();

            if(getMaxSizePerPage() == nodeIter.getKeyList().size()){ // full => overflow
                Node newRoot = split(visited, key, null, null);
                if(newRoot != null){
                    setRoot(newRoot);
                }
                return;
            }

            // invariant: not full
            keyList.add(key);
            Collections.sort(keyList);
        }
    }

    private Node split(Set<Node> visited, int key, Node n1, Node n2){
        Node nodeNew = null;
        if(visited.size() == 0){
            nodeNew = new Node(getFanout(), false, false, maxSizePerPage);
            nodeNew.getKeyList().add(key);
            nodeNew.getChildPtrList().set(0, n1);
            nodeNew.getChildPtrList().set(1, n2);
            return nodeNew;
        }

        // if(visited.get(visited.size() - 1).getIsLeaf()){           // leaf node split
        if(getLast(visited).getIsLeaf()){           // leaf node split
            // Node nodeToSplit = visited.remove(visited.size() - 1);
            Node nodeToSplit = getLast(visited);
            visited.remove(nodeToSplit);
            nodeToSplit.getKeyList().add(key);
            Collections.sort(nodeToSplit.getKeyList());

            nodeNew = new Node(getFanout(), true, false, maxSizePerPage);
            for(int i = halfFull; i < nodeToSplit.getKeyList().size(); i++){
                nodeNew.getKeyList().add(nodeToSplit.getKeyList().get(i));
            }
            nodeNew.setlNode(nodeToSplit);
            nodeNew.setrNode(nodeToSplit.getrNode());

            if(nodeToSplit.getrNode() != null)
                nodeToSplit.getrNode().setlNode(nodeNew);

            int toDeleteKeyCnt = nodeToSplit.getKeyList().size() - halfFull;
            for(int i = 0; i < toDeleteKeyCnt; i++){
                nodeToSplit.getKeyList().remove(nodeToSplit.getKeyList().size() - 1);
            }
            nodeToSplit.setrNode(nodeNew);

            key = nodeNew.getKeyList().get(0); // key to copy up

            n1 = nodeToSplit;
            n2 = nodeNew;

            Collections.sort(nodeNew.getKeyList());
            Collections.sort(nodeToSplit.getKeyList());
             //// System.out.println("n1 key0: " + n1.getKeyList().get(0));
            // System.out.println("new Node key0: " + n2.getKeyList().get(0));
        } else {
            // Node par = visited.remove(visited.size() - 1);
            Node par = getLast(visited);
            visited.remove(par);
            int idx;
            int childPtrOnLeft;
            int childPtrIdx;

            List<Node> parBackupChildPtrList = new ArrayList<Node>();
            for(int i = 0; i < par.getChildPtrList().size(); i++){
                if(par.getChildPtrList().get(i) != null){
                    parBackupChildPtrList.add(par.getChildPtrList().get(i));
                }
            }

            if(getMaxSizePerPage() == par.getKeyList().size()){          //internal node overflow and split
                int oldKey = key; // used to borrowKey child pointer

                par.getKeyList().add(key);
                Collections.sort(par.getKeyList());

                nodeNew = new Node(getFanout(), false, false, maxSizePerPage);

                // update key
                for(int i = halfFull; i < par.getKeyList().size(); i++){
                    nodeNew.getKeyList().add(par.getKeyList().get(i));
                }

                key = nodeNew.getKeyList().remove(0); // key to push up

                int toDeleteKeyCnt = maxSizePerPage - halfFull + 1;
                for(int i = 0; i < toDeleteKeyCnt; i++){
                    par.getKeyList().remove(par.getKeyList().size() - 1);
                }

                // update ptr
                par.childPtrListReset(par.getChildPtrList()); // reset parent child pointer list because later will be borrowKeyd

                childPtrOnLeft = par.getKeyList().size() + 1;
                childPtrIdx = 0;

                idx = nodeNew.getKeyList().indexOf(oldKey);
                if(idx != -1){ // oldKey at newNode
                    int parBackupToRemoveIdx = parBackupChildPtrList.indexOf(n1);
                    if(idx == 0){ // oldKey at first
                        parBackupChildPtrList.remove(parBackupToRemoveIdx); // do not need to backup because it has splited to n1 and n2

                        for(int i = 0; i < childPtrOnLeft; i++){
                            par.getChildPtrList().set(i, parBackupChildPtrList.get(i));
                        }
                        Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                        nodeNew.getChildPtrList().set(childPtrIdx, n1);
                        childPtrIdx++;
                        nodeNew.getChildPtrList().set(childPtrIdx, n2);
                        childPtrIdx++;

                        for(int i = childPtrOnLeft; i < parBackupChildPtrList.size(); i++){
                            nodeNew.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }
                        Collections.sort(nodeNew.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    } else if(idx == (nodeNew.getKeyList().size() - 1)){ // oldKey at last
                        parBackupChildPtrList.remove(parBackupChildPtrList.size() - 1); // last one do not need to backup because it has splited to n1 and n2

                        for(int i = 0; i < childPtrOnLeft; i++){
                            par.getChildPtrList().set(i, parBackupChildPtrList.get(i));
                        }
                        Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                        for(int i = childPtrOnLeft; i < parBackupChildPtrList.size(); i++){
                            nodeNew.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }

                        nodeNew.getChildPtrList().set(childPtrIdx, n1);
                        childPtrIdx++;
                        nodeNew.getChildPtrList().set(childPtrIdx, n2);
                        childPtrIdx++;
                        Collections.sort(nodeNew.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    } else { // oldKey at middle
                        // 60 70 7 72 73 74 75 76 77 78 79 80 81 82 83 84 120 122 123 200 500 501 93 94 95  => 121 case middle order = 3
                        int toRemoveMiddleIdx = -1;
                        for(int i = 0; i < parBackupChildPtrList.size(); i++){
                            if(parBackupChildPtrList.get(i).getKeyList().contains(n1.getKeyList().get(0))){ // key is unique
                                toRemoveMiddleIdx = i;
                                break;
                            }
                        }
                        parBackupChildPtrList.remove(toRemoveMiddleIdx); // middle one do not need to backup because it has splited to n1 and n2

                        for(int i = 0; i < childPtrOnLeft; i++){
                            par.getChildPtrList().set(i, parBackupChildPtrList.get(i));
                        }
                        Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                        for(int i = childPtrOnLeft; i < childPtrOnLeft + idx; i++){
                            nodeNew.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }

                        nodeNew.getChildPtrList().set(childPtrIdx, n1);
                        childPtrIdx++;
                        nodeNew.getChildPtrList().set(childPtrIdx, n2);
                        childPtrIdx++;

                        for(int i = childPtrOnLeft + idx; i < parBackupChildPtrList.size(); i++){
                            nodeNew.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }
                        Collections.sort(nodeNew.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    }
                } else { // oldKey at par
                    idx = par.getKeyList().indexOf(oldKey);
                    if(idx == 0){ // oldKey at first
                        parBackupChildPtrList.remove(0); // first one do not need to backup because it has splited to n1 and n2

                        par.getChildPtrList().set(childPtrIdx, n1);
                        childPtrIdx++;
                        par.getChildPtrList().set(childPtrIdx, n2);
                        childPtrIdx++;

                        for(int i = 0; i < childPtrOnLeft - 2; i++){
                            par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }
                        Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                        for(int i = 0, j = childPtrOnLeft - 2; j < parBackupChildPtrList.size(); i++, j++){
                            nodeNew.getChildPtrList().set(i, parBackupChildPtrList.get(j));
                        }
                        Collections.sort(nodeNew.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    } else if(idx == (nodeNew.getKeyList().size() - 1)){ // oldKey at last
                        parBackupChildPtrList.remove(parBackupChildPtrList.size() - 1); // last one do not need to backup because it has splited to n1 and n2

                        for(int i = 0; i < childPtrOnLeft - 2; i++){
                            par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }

                        par.getChildPtrList().set(childPtrIdx, n1);
                        childPtrIdx++;
                        par.getChildPtrList().set(childPtrIdx, n2);
                        childPtrIdx++;
                        Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                        for(int i = 0, j = childPtrOnLeft - 2; j < parBackupChildPtrList.size(); i++, j++){
                            nodeNew.getChildPtrList().set(i, parBackupChildPtrList.get(j));
                        }
                        Collections.sort(nodeNew.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    } else { // oldKey at middle
                        // 10 20 30 50 80 90 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 52 53 58  => 60 case middle order = 3
                        int toRemoveMiddleIdx = -1;
                        for(int i = 0; i < parBackupChildPtrList.size(); i++){
                            if(parBackupChildPtrList.get(i).getKeyList().contains(n1.getKeyList().get(0))){ // key is unique
                                toRemoveMiddleIdx = i;
                                break;
                            }
                        }
                        parBackupChildPtrList.remove(toRemoveMiddleIdx); // middle one do not need to backup because it has splited to n1 and n2

                        for(int i = 0; i < idx; i++){
                            par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }

                        par.getChildPtrList().set(childPtrIdx, n1);
                        childPtrIdx++;
                        par.getChildPtrList().set(childPtrIdx, n2);
                        childPtrIdx++;

                        for(int i = idx; i < childPtrOnLeft - 2; i++){
                            par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                            childPtrIdx++;
                        }
                        Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                        for(int i = 0, j = childPtrOnLeft - 2; j < parBackupChildPtrList.size(); i++, j++){
                            nodeNew.getChildPtrList().set(i, parBackupChildPtrList.get(j));
                        }
                        Collections.sort(nodeNew.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    }
                    // 51 29 73 105 15 31, order = 1 hw pdf case 
                    // 1 2 3 4 5 6 7 8 9 10 11 12 => 13, at nodeNew case
                    // 16 17 18 19 20 21 22 23 24 25 26 15 14 => 13, at par case
                    // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36
                    // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49
                }
                n1 = par;
                n2 = nodeNew;
            } else {                              // internal node has space
                par.getKeyList().add(key);
                Collections.sort(par.getKeyList());
                par.childPtrListReset(par.getChildPtrList());

                // update ptr
                childPtrIdx = 0;
                idx = par.getKeyList().indexOf(key);
                if(idx == 0){ // key at first
                    parBackupChildPtrList.remove(0); // first one do not need to backup because it has splited to n1 and n2

                    par.getChildPtrList().set(childPtrIdx, n1);
                    childPtrIdx++;
                    par.getChildPtrList().set(childPtrIdx, n2);
                    childPtrIdx++;

                    for(int i = 0; i < parBackupChildPtrList.size(); i++){
                        par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                        childPtrIdx++;
                    }
                    Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                } else if(idx == (par.getKeyList().size() - 1)){ // key at last
                    parBackupChildPtrList.remove(parBackupChildPtrList.size() - 1); // last one do not need to backup because it has splited to n1 and n2

                    for(int i = 0; i < parBackupChildPtrList.size(); i++){
                        par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                        childPtrIdx++;
                    }

                    par.getChildPtrList().set(childPtrIdx, n1);
                    childPtrIdx++;
                    par.getChildPtrList().set(childPtrIdx, n2);
                    childPtrIdx++;
                    Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                } else { // key at middle
                    int toRemoveMiddleIdx = -1;
                    for(int i = 0; i < parBackupChildPtrList.size(); i++){
                        if(parBackupChildPtrList.get(i).getKeyList().contains(n1.getKeyList().get(0))){ // key is unique
                            toRemoveMiddleIdx = i;
                            break;
                        }
                    }
                    parBackupChildPtrList.remove(toRemoveMiddleIdx); // middle one do not need to backup because it has splited to n1 and n2

                    for(int i = 0; i < idx; i++){
                        par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                        childPtrIdx++;
                    }

                    par.getChildPtrList().set(childPtrIdx, n1);
                    childPtrIdx++;
                    par.getChildPtrList().set(childPtrIdx, n2);
                    childPtrIdx++;

                    for(int i = idx; i < parBackupChildPtrList.size(); i++){
                        par.getChildPtrList().set(childPtrIdx, parBackupChildPtrList.get(i));
                        childPtrIdx++;
                    }
                    Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                }

                return null;   // no need to update root
            }
        }

        return split(visited, key, n1, n2);
    }


    public void bulkLoad(){

    }

    @Override
    public List<Integer> lookup(String expr) {
        if(this.root == null){
            return null;
        }

        List<Integer> result = new ArrayList<Integer>();
        StringTokenizer st = new StringTokenizer(expr, " ");

        String oper;
        int key;

        String oper1, oper2;
        int key1, key2;

        Node nodeIter = null;
        List<Node> childPtrList = null;
        List<Integer> keyList = null;
        Node right = null, left = null;

        if(st.countTokens() == 2){
            oper = st.nextToken();
            key = Integer.parseInt(st.nextToken());
            // System.out.println("oper: " + oper);
            // System.out.println("key: " + key);

            nodeIter = this.root;
            childPtrList = null;
            keyList = null;

            while(!nodeIter.getIsLeaf()){
                childPtrList = nodeIter.getChildPtrList();
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(i == (keyList.size() - 1)){
                        if(key < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                        } else if(key >= keyList.get(i)){
                            nodeIter = childPtrList.get(i + 1);
                        }
                    } else {
                        if(key < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                            break;
                        } 
                    }
                }
            }

            // reach data file
            if(oper.equals("=")){
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) == key){
                        result.add(keyList.get(i));
                    }
                }
            } else if(oper.equals(">")){
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) > key){
                        result.add(keyList.get(i));
                    }
                }

                nodeIter = nodeIter.getrNode();
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        result.add(keyList.get(i));
                    }

                    nodeIter = nodeIter.getrNode();
                }
            } else if(oper.equals(">=")){
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) >= key){
                        result.add(keyList.get(i));
                    }
                }

                nodeIter = nodeIter.getrNode();
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        result.add(keyList.get(i));
                    }

                    nodeIter = nodeIter.getrNode();
                }
            } else if(oper.equals("<")){

                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) < key){
                        result.add(keyList.get(i));
                    }
                }

                nodeIter = nodeIter.getlNode();
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        result.add(keyList.get(i));
                    }

                    nodeIter = nodeIter.getlNode();
                }
            } else if(oper.equals("<=")){
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) <= key){
                        result.add(keyList.get(i));
                    }
                }

                nodeIter = nodeIter.getlNode();
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        result.add(keyList.get(i));
                    }

                    nodeIter = nodeIter.getlNode();
                }
            }
        } else {
            key1 = Integer.parseInt(st.nextToken());
            oper1 = st.nextToken();
            st.nextToken(); // the 'key' token, skip it
            oper2 = st.nextToken();
            key2 = Integer.parseInt(st.nextToken());

            nodeIter = this.root;
            childPtrList = null;
            keyList = null;

            while(!nodeIter.getIsLeaf()){
                childPtrList = nodeIter.getChildPtrList();
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(i == (keyList.size() - 1)){
                        if(key1 < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                        } else if(key1 >= keyList.get(i)){
                            nodeIter = childPtrList.get(i + 1);
                        }
                    } else {
                        if(key1 < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                            break;
                        } 
                    }
                }
            }

            // reach data file
            boolean done = false;
            if(oper1.equals("<") && oper2.equals("<")){
                right = nodeIter.getrNode();

                keyList = nodeIter.getKeyList();
                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) >= key2){
                        break;
                    }

                    if(keyList.get(i) > key1){
                        result.add(keyList.get(i));
                    }
                }

                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36

                while(right != null){
                    nodeIter = right;

                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) < key2){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done)
                        break;

                    right = right.getrNode();
                }
            } else if(oper1.equals("<") && oper2.equals("<=")){
                right = nodeIter.getrNode();

                keyList = nodeIter.getKeyList();
                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) > key2){
                        break;
                    }

                    if(keyList.get(i) > key1){
                        result.add(keyList.get(i));
                    }
                }

                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36

                while(right != null){
                    nodeIter = right;

                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) <= key2){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done)
                        break;

                    right = right.getrNode();
                }
            } else if(oper1.equals("<=") && oper2.equals("<")){
                right = nodeIter.getrNode();

                keyList = nodeIter.getKeyList();
                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) >= key2){
                        break;
                    }

                    if(keyList.get(i) >= key1){
                        result.add(keyList.get(i));
                    }
                }

                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36

                while(right != null){
                    nodeIter = right;

                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) < key2){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done)
                        break;

                    right = right.getrNode();
                }
            } else if(oper1.equals("<=") && oper2.equals("<=")){
                right = nodeIter.getrNode();

                keyList = nodeIter.getKeyList();
                for(int i = 0; i < keyList.size(); i++){
                    if(keyList.get(i) > key2){
                        break;
                    }

                    if(keyList.get(i) >= key1){
                        result.add(keyList.get(i));
                    }
                }

                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36

                while(right != null){
                    nodeIter = right;

                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) <= key2){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done)
                        break;

                    right = right.getrNode();
                }
            }
        }

        return result;
    }

    private void updateIndexWithNewKey(Set<Node> visited, int oldKey, int newKey){
        List<Node> _visited = new ArrayList<Node>(visited);
        List<Integer> keyList = null;

        for(int i = _visited.size() - 2; i >= 0; i--){ // -2 because last node is leaf node so ignore it
            keyList = _visited.get(i).getKeyList();
            for(int j = 0; j < keyList.size(); j++){
                if(keyList.get(j) == oldKey){
                    keyList.set(j, newKey);
                }
            }
        }
    }

    @Override
    public void delete(String expr){
        if(root == null)
            return;

        List<Integer> res = lookup(expr);
        if(res.size() == 0){ 
            System.out.println("key not exists");
            return;
        }

        StringTokenizer st = new StringTokenizer(expr, " ");
        String oper;
        int key;
        Node nodeIter = this.root;
        List<Node> childPtrList = null;
        List<Integer> keyList = null;
        List<Node> _visited = new ArrayList<Node>();
        Set<Node> visited = null;

        if(st.countTokens() == 2){  // one side format
            oper = st.nextToken();
            key = Integer.parseInt(st.nextToken());

            _visited.add(nodeIter);
            while(!nodeIter.getIsLeaf()){
                childPtrList = nodeIter.getChildPtrList();
                keyList = nodeIter.getKeyList();

                for(int i = 0; i < keyList.size(); i++){
                    if(i == (keyList.size() - 1)){
                        if(key < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                        } else if(key >= keyList.get(i)){
                            nodeIter = childPtrList.get(i + 1);
                        }
                    } else {
                        if(key < keyList.get(i)){
                            nodeIter = childPtrList.get(i);
                            _visited.add(nodeIter);
                            break;
                        } 
                    }

                    _visited.add(nodeIter);
                }
            }
            visited = new LinkedHashSet<Node>(_visited);
            keyList = nodeIter.getKeyList();

            // reach leaf node
            setKey(key);
            if(oper.equals("=")){
                if(visited.size() == 1){ // only one data page
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) == key){
                            keyList.remove(i);
                            break;
                        }
                    }
                } else {
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) == key){
                            if(key == Collections.max(keyList) ||
                                key == Collections.min(keyList)){

                                if(keyList.size() >= 2){
                                    if(i == (keyList.size() - 1)){
                                        setSuccessorKey(keyList.get(i - 1));
                                        // newKey = keyList.get(i - 1);
                                    } else {
                                        setSuccessorKey(keyList.get(i + 1));
                                        // newKey = keyList.get(i + 1);
                                    }
                                } else if(keyList.size() == 1){ // for order = 1
                                    Node nodeIterPar = getSecondLast(visited);
                                    int nodeIterParChildSize = 0;
                                    int childIdx = nodeIterPar.getChildPtrList().indexOf(nodeIter);

                                    for(int j = 0; j < nodeIterPar.getChildPtrList().size(); j++){
                                        if(nodeIterPar.getChildPtrList().get(j) != null){
                                            nodeIterParChildSize++;
                                        }
                                    }

                                    if(childIdx == 0){ // first
                                        setSuccessorKey(nodeIterPar.getChildPtrList().get(childIdx + 1).getKeyList().get(0));
                                        // newKey = nodeIterPar.getChildPtrList().get(childIdx + 1).getKeyList().get(0);
                                    } else if(childIdx == (nodeIterParChildSize - 1)){ // last
                                        setSuccessorKey(nodeIterPar.getChildPtrList().get(childIdx - 1).getKeyList().get(0));
                                        // newKey = nodeIterPar.getChildPtrList().get(childIdx - 1).getKeyList().get(0);
                                    } else {

                                    }
                                }
                                // System.out.println("new key: " + newKey);
                            }
                            keyList.remove(i);
                            break;
                        }
                    }
                }
            }

            System.out.println("successor key: " + getSuccessorKey());
            // updateIndexWithNewKey(visited, key, getSuccessorKey());

            // underflow => borrowKey, redistribution and merge if needs
            // check doubly linked list pointer to ensure there are index pages exist, so we can update index
            // 如果只有 root 的話，全部都是 data 而沒有 index，那就不用 borrowKey, redistribution and merge 了
            if((nodeIter.getlNode() != null || nodeIter.getrNode() != null) && 
                keyList.size() <= (halfFull - 1)){ 
                Node sibling = null;
                Node nodeIterPar = getSecondLast(visited);

                if((sibling = borrowKey(nodeIter, nodeIterPar, false)) != null){
                    Node newRoot = merge(visited, nodeIter, sibling);
                    if(newRoot != null){
                        setRoot(newRoot);
                    }
                }
            }
        }
    }

    // private Node merge(List<Node> visited, Node n, Node nSib){
    private Node merge(Set<Node> visited, Node n, Node nSib){
        if(visited.size() <= 0){
            return null;
        }

        Node par;
        int nIdx;
        int sibIdx;

        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 50 51 52 53 54 55 56 57 58 59 60 61
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 50 51 52 70 71 72 73 74 75 76 77 78
        if(n.getIsLeaf()){ // leaf node merge
            visited.remove(getLast(visited)); // same as node n
            par = getLast(visited);
            visited.remove(par);
            nIdx = par.getChildPtrList().indexOf(n);
            sibIdx = par.getChildPtrList().indexOf(nSib);

            for(int i = 0; i < nSib.getKeyList().size(); i++){
                n.getKeyList().add(nSib.getKeyList().get(i));
            }
            Collections.sort(n.getKeyList());

            //   2 3 5 7 13 14 15, 5 7 3 13

            if(sibIdx > nIdx){ // sibling on right side
                // par.getChildPtrList().remove(sibIdx);
                par.getChildPtrList().set(sibIdx, null);
                Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                par.getKeyList().remove(sibIdx - 1);
                Collections.sort(par.getKeyList());

                // update doubly linked list ptr
                n.setrNode(nSib.getrNode());
                if(nSib.getrNode() != null)
                    nSib.getrNode().setlNode(n);

            } else { // sibling on left side
                // par.getChildPtrList().remove(sibIdx);
                par.getChildPtrList().set(sibIdx, null);
                Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                par.getKeyList().remove(nIdx - 1);
                Collections.sort(par.getKeyList());

                // update doubly linked list ptr
                n.setlNode(nSib.getlNode());
                if(nSib.getlNode() != null)
                    nSib.getlNode().setrNode(n);
            }

            if(visited.size() >= 1){ 
                return merge(visited, par, null); // null means sibling should get from visited parent and only internal node merge will be null
            } else {
                int childPtrListSize = 0;
                for(int i = 0; i < par.getChildPtrList().size(); i++){
                    if(par.getChildPtrList().get(i) != null){
                        childPtrListSize++;
                    }
                }

                if(childPtrListSize == 1)
                    return n;
            }
        } else { // internal node merge
            if(n.getKeyList().size() >= halfFull){
                return null;
            }

            System.out.println("internal node merge");

            par = n;
            Node parPar = getLast(visited);
            visited.remove(parPar);
            Node parSib = null;
            int parIdx = parPar.getChildPtrList().indexOf(par);
            int keyToMove = -1;
            int parLastEmptyChildPtrIdx = -1;
            int childPtrListSize = 0;
            int parSibChildPtrListSize = 0;
            for(int i = 0; i < parPar.getChildPtrList().size(); i++){
                if(parPar.getChildPtrList().get(i) != null){
                    childPtrListSize++;
                }
            }

            boolean parParIsRoot = (visited.size() == 0);
            Node sibling = borrowKey(n, parPar, parParIsRoot);
            if(sibling == null){
                return null;
            }

            if(n.getKeyList().size() >= halfFull){
                return null;
            }

            if(parIdx == 0){ // par at first
                if(maxSizePerPage == 2){ // order = 1
                    parSib = parPar.getChildPtrList().get(parIdx + 1);
                    keyToMove = parPar.getKeyList().remove(parIdx);

                    parPar.getChildPtrList().set(parIdx + 1, null);

                    par.getKeyList().add(keyToMove);
                    for(int i = 0; i < parSib.getKeyList().size(); i++){
                        par.getKeyList().add(parSib.getKeyList().get(i));
                    }

                    for(int i = 0; i < parSib.getChildPtrList().size(); i++){
                        if(parSib.getChildPtrList().get(i) != null){
                            parSibChildPtrListSize++;
                        }
                    }

                    for(int i = 0; i < par.getChildPtrList().size(); i++){
                        if(par.getChildPtrList().get(i) != null){
                            parLastEmptyChildPtrIdx = i;
                        }
                    }

                    parLastEmptyChildPtrIdx++;
                    for(int i = 0; i < parSibChildPtrListSize; i++){
                        par.getChildPtrList().set(parLastEmptyChildPtrIdx, parSib.getChildPtrList().get(i));
                        parLastEmptyChildPtrIdx++;
                    }

                    Collections.sort(par.getKeyList());
                    Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    Collections.sort(parSib.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    Collections.sort(parPar.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                    if(parParIsRoot && parPar.getKeyList().size() == 0){
                        return par;
                    }
                } else {
                    parSib = parPar.getChildPtrList().get(parIdx + 1);
                    keyToMove = parPar.getKeyList().remove(parIdx);
                    parPar.getChildPtrList().set(parIdx + 1, null);
                    Collections.sort(parPar.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                    par.getKeyList().add(keyToMove);
                    for(int i = 0; i < parSib.getKeyList().size(); i++){
                        par.getKeyList().add(parSib.getKeyList().get(i));
                    }
                    Collections.sort(par.getKeyList());

                    for(int i = 0; i < parSib.getChildPtrList().size(); i++){
                        if(parSib.getChildPtrList().get(i) != null){
                            parSibChildPtrListSize++;
                        }
                    }

                    for(int i = 0; i < par.getChildPtrList().size(); i++){
                        if(par.getChildPtrList().get(i) != null){
                            parLastEmptyChildPtrIdx = i;
                        }
                    }

                    parLastEmptyChildPtrIdx++;
                    for(int i = 0; i < parSibChildPtrListSize; i++){
                        par.getChildPtrList().set(parLastEmptyChildPtrIdx, parSib.getChildPtrList().get(i));
                        parLastEmptyChildPtrIdx++;
                    }
                    Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                    if(parParIsRoot && parPar.getKeyList().size() == 0){
                        return par;
                    }

                }

                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 
            } else if(parIdx == (childPtrListSize - 1)){ // par at last
                int parChildPtrListSize = 0;
                int parSibLastEmptyChildPtrIdx = 0;

                parSib = parPar.getChildPtrList().get(parIdx - 1);
                // parPar.getChildPtrList().remove(parIdx - 1);
                keyToMove = parPar.getKeyList().remove(parIdx - 1);

                parPar.getChildPtrList().set(parIdx, null);
                Collections.sort(parPar.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                // Note: par is right, parSib is left
                parSib.getKeyList().add(keyToMove);
                for(int i = 0; i < par.getKeyList().size(); i++){
                    parSib.getKeyList().add(par.getKeyList().get(i));
                }
                Collections.sort(parSib.getKeyList());

                for(int i = 0; i < par.getChildPtrList().size(); i++){
                    if(par.getChildPtrList().get(i) != null){
                        parChildPtrListSize++;
                    }
                }

                for(int i = 0; i < parSib.getChildPtrList().size(); i++){
                    if(parSib.getChildPtrList().get(i) != null){
                        parSibLastEmptyChildPtrIdx = i;
                    }
                }

                parSibLastEmptyChildPtrIdx++;
                for(int i = 0; i < parChildPtrListSize; i++){
                    parSib.getChildPtrList().set(parSibLastEmptyChildPtrIdx, par.getChildPtrList().get(i));
                    parSibLastEmptyChildPtrIdx++;
                }
                Collections.sort(parSib.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                if(parParIsRoot && parPar.getKeyList().size() == 0){
                    return parSib;
                }

            } else { // par at middle
                System.out.println("middle internal node merge");
                Node parSibRight = parPar.getChildPtrList().get(parIdx + 1);
                Node parSibLeft = parPar.getChildPtrList().get(parIdx - 1);
                int keyListSizeRight = parSibRight.getKeyList().size();
                int keyListSizeLeft = parSibLeft.getKeyList().size();
                int keyListSize = par.getKeyList().size();

                if(keyListSize + keyListSizeLeft <= maxSizePerPage){
                    int parChildPtrListSize = 0;
                    int parSibLastEmptyChildPtrIdx = 0;

                    parSib = parPar.getChildPtrList().get(parIdx - 1);
                    keyToMove = parPar.getKeyList().remove(parIdx - 1);

                    // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31

                    parPar.getChildPtrList().set(parIdx, null);
                    Collections.sort(parPar.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                    // Note: par is right, parSib is left
                    parSib.getKeyList().add(keyToMove);
                    for(int i = 0; i < par.getKeyList().size(); i++){
                        parSib.getKeyList().add(par.getKeyList().get(i));
                    }
                    Collections.sort(parSib.getKeyList());

                    for(int i = 0; i < par.getChildPtrList().size(); i++){
                        if(par.getChildPtrList().get(i) != null){
                            parChildPtrListSize++;
                        }
                    }

                    for(int i = 0; i < parSib.getChildPtrList().size(); i++){
                        if(parSib.getChildPtrList().get(i) != null){
                            parSibLastEmptyChildPtrIdx = i;
                        }
                    }

                    parSibLastEmptyChildPtrIdx++;
                    for(int i = 0; i < parChildPtrListSize; i++){
                        parSib.getChildPtrList().set(parSibLastEmptyChildPtrIdx, par.getChildPtrList().get(i));
                        parSibLastEmptyChildPtrIdx++;
                    }
                    Collections.sort(parSib.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                } else if(keyListSize + keyListSizeRight <= maxSizePerPage){
                    parSib = parPar.getChildPtrList().get(parIdx + 1);
                    keyToMove = parPar.getKeyList().remove(parIdx);
                    // parPar.getChildPtrList().remove(parIdx + 1);
                    parPar.getChildPtrList().set(parIdx + 1, null);
                    Collections.sort(parPar.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));

                    par.getKeyList().add(keyToMove);
                    for(int i = 0; i < parSib.getKeyList().size(); i++){
                        par.getKeyList().add(parSib.getKeyList().get(i));
                    }

                    for(int i = 0; i < parSib.getChildPtrList().size(); i++){
                        if(parSib.getChildPtrList().get(i) != null){
                            parSibChildPtrListSize++;
                        }
                    }

                    for(int i = 0; i < par.getChildPtrList().size(); i++){
                        if(par.getChildPtrList().get(i) != null){
                            parLastEmptyChildPtrIdx = i;
                        }
                    }

                    parLastEmptyChildPtrIdx++;
                    for(int i = 0; i < parSibChildPtrListSize; i++){
                        par.getChildPtrList().set(parLastEmptyChildPtrIdx, parSib.getChildPtrList().get(i));
                        parLastEmptyChildPtrIdx++;
                    }
                    Collections.sort(par.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                }
            }

            // if(visited.size() == 1){  // first level index pages
            // } else { // other level index pages, keep merge until balanced
            // }

            if(visited.size() >= 1){
                return merge(visited, getLast(visited), null); // null means sibling should get from visited parent and only internal node merge will be null
            }


            // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33
        }

        return null;
    }

    /**
     * @param n node to borrow key from other node
     * @param nPar node parent
     * @return node sibling if needs merge else null
     */
    private Node borrowKey(Node n, Node nPar, boolean nParIsRoot){
        List<Node> childPtrList = nPar.getChildPtrList();
        List<Integer> keyList = nPar.getKeyList();
        int idx = childPtrList.indexOf(n);
        int keyToBorrow;
        Node sibling = null;
        Node siblingRight = null;
        Node siblingLeft = null;
        boolean leftCan = false;
        boolean rightCan = false;

        int childPtrListSize = 0;
        for(int i = 0; i < childPtrList.size(); i++){
            if(childPtrList.get(i) != null){
                childPtrListSize++;
            }
        }

        if(n.getIsLeaf()){
            if(idx == 0){
                sibling = n.getrNode();
            } else if(idx == (childPtrListSize - 1)){
                sibling = n.getlNode();
            } else {
                siblingRight = n.getrNode();
                siblingLeft = n.getlNode();
            }

            if(idx == 0){ // n at first
                System.out.println("first");

                if(sibling.getKeyList().size() - 1 < halfFull){
                    return sibling;
                }

                keyToBorrow = sibling.getKeyList().remove(0);     // this key should be copied up
                n.getKeyList().add(keyToBorrow);
                keyList.set(idx, sibling.getKeyList().get(0));
            } else if(idx == (childPtrListSize - 1)){ // n at last
                System.out.println("last");
                if(sibling.getKeyList().size() - 1 < halfFull){
                    return sibling;
                }

                keyToBorrow = sibling.getKeyList().remove(sibling.getKeyList().size() - 1);  // this key should be copied up
                n.getKeyList().add(0, keyToBorrow);
                keyList.set(idx - 1, keyToBorrow);
            } else { // n at middle, borrow from left then right
                System.out.println("middle");

                if(siblingLeft != null){
                    sibling = siblingLeft;
                    if(sibling.getKeyList().size() - 1 < halfFull){
                        leftCan = false;
                    } else {
                        leftCan = true;
                    }
                }

                if(siblingRight != null){
                    sibling = siblingRight;
                    if(sibling.getKeyList().size() - 1 < halfFull){
                        rightCan = false;
                    } else {
                        rightCan = true;
                    }
                }

                if(leftCan == false && rightCan == false){
                    return siblingLeft;
                    // return siblingRight; // also can but i choose left first
                } else if((leftCan == true && rightCan == false) ||
                            (leftCan == true && rightCan == true)){
                    sibling = siblingLeft;
                    keyToBorrow = sibling.getKeyList().remove(sibling.getKeyList().size() - 1);
                    n.getKeyList().add(0, keyToBorrow);
                    keyList.set(idx - 1, keyToBorrow);

                } else if(leftCan == false && rightCan == true){
                    sibling = siblingRight;
                    keyToBorrow = sibling.getKeyList().remove(0);
                    n.getKeyList().add(keyToBorrow);
                    if(idx == 1){
                        keyList.set(idx - 1, sibling.getKeyList().get(0));
                    } else {
                        keyList.set(idx, sibling.getKeyList().get(0));
                    }
                }
            }

            Collections.sort(keyList);
            Collections.sort(sibling.getKeyList());
            Collections.sort(n.getKeyList());
        } else {
            int keyToPullDown = -1;
            int nLastEmptyChildPtrIdx = 0;

            if(idx == 0){
                sibling = nPar.getChildPtrList().get(idx + 1);
            } else if(idx == (childPtrListSize - 1)){
                sibling = nPar.getChildPtrList().get(idx - 1);
            } else {
                siblingRight = nPar.getChildPtrList().get(idx + 1);
                siblingLeft = nPar.getChildPtrList().get(idx - 1);
            }

            if(idx == 0){ // n at first
                System.out.println("internal first");

                if(sibling.getKeyList().size() - 1 < halfFull){
                    return sibling;
                }

                for(int i = 0; i < n.getChildPtrList().size(); i++){
                    if(n.getChildPtrList().get(i) != null){
                        nLastEmptyChildPtrIdx++;
                    }
                }

                keyToBorrow = sibling.getChildPtrList().get(0).getKeyList().get(0);
                n.getKeyList().add(keyToBorrow);
                keyList.set(idx, sibling.getKeyList().remove(0));
                n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(0));
                sibling.getChildPtrList().set(0, null);

                Collections.sort(keyList);
                Collections.sort(sibling.getKeyList());
                Collections.sort(n.getKeyList());
                Collections.sort(n.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                Collections.sort(sibling.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
            } else if(idx == (childPtrListSize - 1)){ // n at last
                System.out.println("internal last");

                if(sibling.getKeyList().size() - 1 < halfFull){
                    return sibling;
                }

                for(int i = 0; i < n.getChildPtrList().size(); i++){
                    if(n.getChildPtrList().get(i) != null){
                        nLastEmptyChildPtrIdx++;
                    }
                }

                keyToPullDown = keyList.get(idx - 1);
                keyToBorrow = sibling.getKeyList().remove(sibling.getKeyList().size() - 1);
                n.getKeyList().add(0, keyToPullDown);
                // keyList.set(idx - 1, keyToBorrow);
                keyList.set(idx - 1, keyToPullDown);
                n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(sibling.getChildPtrList().size() - 1));
                sibling.getChildPtrList().set(sibling.getChildPtrList().size() - 1, null);

                Collections.sort(keyList);
                Collections.sort(sibling.getKeyList());
                Collections.sort(n.getKeyList());
                Collections.sort(n.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                Collections.sort(sibling.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
            } else { // n at middle, borrow from left then right
                System.out.println("internal middle");

                if(siblingLeft != null){
                    sibling = siblingLeft;
                    if(sibling.getKeyList().size() - 1 < halfFull){
                        leftCan = false;
                    } else {
                        leftCan = true;
                    }
                }

                if(siblingRight != null){
                    sibling = siblingRight;
                    if(sibling.getKeyList().size() - 1 < halfFull){
                        rightCan = false;
                    } else {
                        rightCan = true;
                    }
                }

                if((leftCan == false && rightCan == false)){
                    return siblingLeft;
                } else if((leftCan == true && rightCan == false) || 
                            (leftCan == true && rightCan == true)){
                    for(int i = 0; i < n.getChildPtrList().size(); i++){
                        if(n.getChildPtrList().get(i) != null){
                            nLastEmptyChildPtrIdx++;
                        }
                    }

                    sibling = siblingLeft;
                    int sibingLastChildPtrIdx = 0;
                    for(int i = 0; i < sibling.getChildPtrList().size(); i++){
                        if(sibling.getChildPtrList().get(i) != null){
                            sibingLastChildPtrIdx++;
                        }
                    }

                    keyToBorrow = sibling.getKeyList().remove(sibling.getKeyList().size() - 1);
                    n.getKeyList().add(0, keyToBorrow);
                    keyList.set(idx - 1, keyToBorrow);
                    n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(sibingLastChildPtrIdx - 1));
                    sibling.getChildPtrList().set(sibingLastChildPtrIdx - 1, null);

                    Collections.sort(keyList);
                    Collections.sort(sibling.getKeyList());
                    Collections.sort(n.getKeyList());
                    Collections.sort(n.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    Collections.sort(sibling.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                } else if(leftCan == false && rightCan == true){
                    if(maxSizePerPage == 2){ // order = 1
                        for(int i = 0; i < n.getChildPtrList().size(); i++){
                            if(n.getChildPtrList().get(i) != null){
                                nLastEmptyChildPtrIdx++;
                            }
                        }

                        sibling = siblingRight;
                        keyToBorrow = sibling.getKeyList().remove(0);
                        keyToPullDown = nPar.getKeyList().remove(idx);
                        n.getKeyList().add(keyToPullDown);
                        nPar.getKeyList().add(keyToBorrow);
                        n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(0));
                        sibling.getChildPtrList().set(0, null);

                        Collections.sort(keyList);
                        Collections.sort(sibling.getKeyList());
                        Collections.sort(n.getKeyList());
                        Collections.sort(n.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                        Collections.sort(sibling.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    } else {
                        for(int i = 0; i < n.getChildPtrList().size(); i++){
                            if(n.getChildPtrList().get(i) != null){
                                nLastEmptyChildPtrIdx++;
                            }
                        }

                        sibling = siblingRight;
                        keyToBorrow = sibling.getKeyList().remove(0);
                        n.getKeyList().add(keyToBorrow);
                        // keyList.set(idx - 1, keyToBorrow);

                        System.out.println("get key: " + getKey());
                        System.out.println("key to bottow: " + keyToBorrow);
                        boolean change = false;
                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) == getKey()){
                                keyList.set(i, getSuccessorKey());
                                change = true;
                                break;
                            }
                        }

                        if(!change){
                            keyList.set(idx - 1, keyToBorrow);
                            n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(0));
                            sibling.getChildPtrList().set(0, null);
                        } else {
                            keyToPullDown = keyList.get(idx);
                            keyList.set(idx, keyToBorrow);
                            n.getKeyList().set(idx, keyToPullDown);
                            n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(0));
                            sibling.getChildPtrList().set(0, null);
                        }

                        // n.getChildPtrList().set(nLastEmptyChildPtrIdx, sibling.getChildPtrList().get(0));
                        // sibling.getChildPtrList().set(0, null);

                        Collections.sort(keyList);
                        Collections.sort(sibling.getKeyList());
                        Collections.sort(n.getKeyList());
                        Collections.sort(n.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                        Collections.sort(sibling.getChildPtrList(), Comparator.nullsLast(new ChildPtrComparator()));
                    }
                }
            }
        }

        return null;
    }

    public DefaultMutableTreeNode displayGUI(Node root, DefaultMutableTreeNode par) {
        // pre-order

        if(root == null){
            return null;
        }

        Node nodeIter = root;
        List<Integer> keyList = nodeIter.getKeyList();
        List<Node> childPtrList = nodeIter.getChildPtrList();
        StringBuffer strbuf = new StringBuffer();

        int emptySize = getFanout() - keyList.size() - 1;

        if(keyList.size() == 0){
            strbuf.append("[");
        } else {
            for(int i = 0; i < keyList.size(); i++){
                if(i == 0) {
                    if(nodeIter.getIsLeaf()){
                        strbuf.append("[");
                    } else {
                        strbuf.append("(");
                    }
                } else if(i == keyList.size() - 1){
                    strbuf.append(Integer.toString(keyList.get(i)));
                    if(emptySize >= 2){
                        if(nodeIter.getIsLeaf()){
                            strbuf.append(",");
                        } else {
                            strbuf.append(":");
                        }
                    }
                    break;
                }
                strbuf.append(Integer.toString(keyList.get(i)));
                if(nodeIter.getIsLeaf()){
                    strbuf.append(",");
                } else {
                    strbuf.append(":");
                }
            }
        }

        if(!nodeIter.getIsLeaf()){
            if(emptySize == 0){
                strbuf.append(")");
            } else {
                if(emptySize == 1){
                    strbuf.append(":");
                    strbuf.append("_");
                    strbuf.append(")");
                } else {
                    for(int i = 0; i < emptySize; i++){
                        if(i == emptySize - 1){
                            strbuf.append("_");
                            strbuf.append(")");
                            break;
                        }
                        strbuf.append("_");
                        strbuf.append(":");
                    }
                }
            }
        } else {
            emptySize = getMaxSizePerPage() - keyList.size();
            if(emptySize == 0){
                strbuf.append("]");
            } else {
                if(emptySize == 1){
                    strbuf.append(",");
                    strbuf.append("_");
                    strbuf.append("]");
                } else {
                    for(int i = 0; i < emptySize; i++){
                        if(i == emptySize - 1){
                            strbuf.append("_");
                            strbuf.append("]");
                            break;
                        }
                        strbuf.append("_");
                        strbuf.append(",");
                    }
                }
            }
        }
        DefaultMutableTreeNode n, tmp;
        n = new DefaultMutableTreeNode(strbuf.toString());

        if(childPtrList != null){
            for(int i = 0; i < childPtrList.size(); i++){
                tmp = displayGUI(childPtrList.get(i), n);
                if(tmp != null)
                    n.add(tmp);
            }
        }

        return n;
    }

    @Override
    public void display(Node root, int identation) {
        // pre-order

        if(root == null){
            return;
        }

        for(int i = 0; i < identation; i++){
            System.out.print(" ");
        }

        Node nodeIter = root;
        List<Integer> keyList = nodeIter.getKeyList();
        List<Node> childPtrList = nodeIter.getChildPtrList();

        int emptySize = getFanout() - keyList.size() - 1;

        if(keyList.size() == 0){
            System.out.print("[");
        } else {
            for(int i = 0; i < keyList.size(); i++){
                if(i == 0) {
                    if(nodeIter.getIsLeaf()){
                        System.out.print("[");
                    } else {
                        System.out.print("(");
                    }
                } else if(i == keyList.size() - 1){
                    System.out.print(keyList.get(i));
                    if(emptySize >= 1){
                        if(nodeIter.getIsLeaf()){
                            System.out.print(",");
                        } else {
                            System.out.print(":");
                        }
                    }
                    break;
                }
                System.out.print(keyList.get(i));
                if(nodeIter.getIsLeaf()){
                    System.out.print(",");
                } else {
                    System.out.print(":");
                }
            }
        }

        if(!nodeIter.getIsLeaf()){
            if(emptySize == 0){
                System.out.print(")");
            } else {
                if(emptySize == 1){
                    // System.out.print(":");
                    System.out.print("_");
                    System.out.print(")");
                } else {
                    for(int i = 0; i < emptySize; i++){
                        if(i == emptySize - 1){
                            System.out.print("_");
                            System.out.print(")");
                            break;
                        }
                        System.out.print("_");
                        System.out.print(":");
                    }
                }
            }
        } else {
            emptySize = getMaxSizePerPage() - keyList.size();
            if(emptySize == 0){
                System.out.print("]");
            } else {
                if(emptySize == 1){
                    // System.out.print(",");
                    System.out.print("_");
                    System.out.print("]");
                } else {
                    for(int i = 0; i < emptySize; i++){
                        if(i == emptySize - 1){
                            System.out.print("_");
                            System.out.print("]");
                            break;
                        }
                        System.out.print("_");
                        System.out.print(",");
                    }
                }
            }
        }
        System.out.println();

        if(childPtrList != null){
            for(int i = 0; i < childPtrList.size(); i++){
                display(childPtrList.get(i), identation + 2);
            }
        }
    }

    @Override
    public void quit() {

    }
}
