import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

public class ISAM extends Tree {
    private final int pageSize = 8;    // in practice at least 4KB

    private int maxSizePerPage;
    public int getMaxSizePerPage() {
        return maxSizePerPage;
    }
    public void setMaxSizePerPage(int maxSizePerPage) {
        this.maxSizePerPage = maxSizePerPage;
    }

    public ISAM(){

    }

    @Override
    public void init(int fanout) {
        setFanout(fanout);
        setMaxSizePerPage(pageSize / Integer.BYTES);

        // int[] initData = {15, 27, 37, 46, 55, 97, 10, 20, 33, 40, 51, 63};
        int[] initData = {15, 27, 37, 46, 55, 97, 10, 20, 33, 40, 51, 63, 101, 123, 145, 179, 183,202,300,302,222,456,789,999};
        int initDataLength = initData.length;
        Arrays.sort(initData);

        // int keySlotSize = fanout - 1; // fixme: should use page size instead, keySlotSize = pageSize / sizeof(one entry size)
        // int keySlotSize = getMaxSizePerPage();
        int keySlotSize = fanout - 1;
        
        // int keySlotSize = getMaxSizePerPage(); // fixme: should use page size instead, keySlotSize = pageSize / sizeof(one entry size)
        List<Integer> keyList;
        List<Node> dataFile = new ArrayList<Node>();

        // create data file
        for(int i = 0; i < initDataLength; i += keySlotSize){
            Node data = new Node(fanout, true, false, maxSizePerPage);
            keyList = data.getKeyList();

            for(int j = 0; j < keySlotSize && i + j < initDataLength; j++){
                keyList.add(initData[i + j]);
            }

            dataFile.add(data);

            // for(int j = 0; j < keyList.size(); j++){
            //     System.out.print(keyList.get(j) + ",");
            // }
            // System.out.println();
        }

        // create doubly linked list in data file
        if(dataFile.size() >= 2){
            for(int i = 0; i < dataFile.size(); i++){
                if(i == 0){
                    dataFile.get(i).setlNode(null);
                    if(dataFile.get(i + 1) != null){
                        dataFile.get(i).setrNode(dataFile.get(i + 1));
                    } else {
                        dataFile.get(i).setrNode(null);
                    }
                } else if(i == dataFile.size() - 1){
                    dataFile.get(i).setrNode(null);
                    if(dataFile.get(i - 1) != null){
                        dataFile.get(i).setlNode(dataFile.get(i - 1));
                    } else {
                        dataFile.get(i).setlNode(null);
                    }
                } else {
                    dataFile.get(i).setrNode(dataFile.get(i + 1));
                    dataFile.get(i).setlNode(dataFile.get(i - 1));
                }
            }
        }

        // create index file
        Queue<Node> indexFile = new LinkedList<Node>();
        List<Node> indexChildPtrList;
        List<Integer> indexKeyList;
        Node index = null;

        // 第一層 index 用 data file 來建構
        if(dataFile.size() < fanout){   // dataFile 分割後比 fanout 來得少只需要一個 root 就可以了
            index = new Node(fanout, false, false, -1);
            indexChildPtrList = index.getChildPtrList();
            indexKeyList = index.getKeyList();

            indexChildPtrList.set(0, dataFile.get(0));
            for(int j = 1; j < dataFile.size(); j++){
                indexChildPtrList.set(j, dataFile.get(j));
                indexKeyList.add(dataFile.get(j).getKeyList().get(0));
            }

            indexFile.add(index);
        } else {                        // dataFile 分割後比 fanout 來得多所以需要多幾層 index
            for(int i = 0; i < dataFile.size(); i += fanout){
                index = new Node(fanout, false, false, -1);
                indexChildPtrList = index.getChildPtrList();
                indexKeyList = index.getKeyList();

                for(int j = 0; i + j < dataFile.size() && j < fanout; j++){
                    indexChildPtrList.set(j, dataFile.get(i + j));
                }

                // to be fixed
                if(i == 0){
                    indexKeyList.add(dataFile.get(i + 1).getKeyList().get(0));
                    for(int j = 2; i + j < dataFile.size() && j < fanout; j++){
                        indexKeyList.add(dataFile.get(i + j).getKeyList().get(0));
                    }
                } else {
                    for(int j = 0; i + j < dataFile.size() && j < fanout - 1; j++){
                        indexKeyList.add(dataFile.get(i + j).getKeyList().get(0));
                    }
                }

                indexFile.add(index);
            }
        }

        // 第二或以上層用 index file 來建構，直到 root, Note: 第二或以上層只會用到兩個 pointers
        Node index1 = null, index2 = null;
        int smallestKey = -1;
        while((index1 = indexFile.poll()) != null &&
                (index2 = indexFile.poll()) != null){
            
            index = new Node(fanout, false, false, -1);
            indexChildPtrList = index.getChildPtrList();
            indexKeyList = index.getKeyList();

            Node smallestRightNode = index2;
            while(smallestRightNode.getChildPtrList() != null){
                smallestRightNode = smallestRightNode.getChildPtrList().get(0);
            }
            smallestKey = smallestRightNode.getKeyList().get(0);

            indexKeyList.add(smallestKey);
            indexChildPtrList.set(0, index1);
            indexChildPtrList.set(1, index2);

            indexFile.add(index);
        }

        // create root
        Node root = index1;
        setRoot(root);
        // System.out.println("root key: " + root.getKeyList().get(0));
    }

    public void attach(){

    }

    public void bulkLoad(){

    }

    @Override
    public void insert(int key){
        Node nodeIter = this.root;
        List<Node> childPtrList = null;
        List<Integer> keyList = null;

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
        while((getFanout() - 1) == nodeIter.getKeyList().size()){ // full => overflow
            if(nodeIter.getOverflowPtr() == null)
                break;
            nodeIter = nodeIter.getOverflowPtr();
        }

        // overflow page overflow
        if((getFanout() - 1) == nodeIter.getKeyList().size()){
            Node overflowPage = new Node(getFanout(), false, true, maxSizePerPage);
            nodeIter.setOverflowPtr(overflowPage);
            nodeIter = overflowPage;
        }

        keyList = nodeIter.getKeyList();
        keyList.add(key);
    }

    @Override
    public List<Integer> lookup(String expr) {
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
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) == key){
                            result.add(keyList.get(i));
                        }
                    }

                    nodeIter = nodeIter.getOverflowPtr();
                }
            } else if(oper.equals(">")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key){
                            result.add(keyList.get(i));
                        }
                    }

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(right != null){

                    nodeIter = right;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            result.add(keyList.get(i));
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    right = right.getrNode();
                }
            } else if(oper.equals(">=")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key){
                            result.add(keyList.get(i));
                        }
                    }

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(right != null){

                    nodeIter = right;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            result.add(keyList.get(i));
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    right = right.getrNode();
                }
            } else if(oper.equals("<")){

                left = nodeIter.getlNode();
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) < key){
                            result.add(keyList.get(i));
                        }
                    }

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(left != null){

                    nodeIter = left;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            result.add(keyList.get(i));
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    left = left.getlNode();
                }
            } else if(oper.equals("<=")){

                left = nodeIter.getlNode();
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) <= key){
                            result.add(keyList.get(i));
                        }
                    }

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(left != null){

                    nodeIter = left;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            result.add(keyList.get(i));
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    left = left.getlNode();
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

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) > key1){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done == true)
                        break;

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(right != null){
                    nodeIter = right;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) < key2){
                                result.add(keyList.get(i));
                            }
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    right = right.getrNode();
                }
            } else if(oper1.equals("<") && oper2.equals("<=")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) > key1){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done == true)
                        break;

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(right != null){
                    nodeIter = right;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) <= key2){
                                result.add(keyList.get(i));
                            }
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    right = right.getrNode();
                }
            } else if(oper1.equals("<=") && oper2.equals("<")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) >= key1){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done == true)
                        break;

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(right != null){
                    nodeIter = right;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) < key2){
                                result.add(keyList.get(i));
                            }
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    right = right.getrNode();
                }
            } else if(oper1.equals("<=") && oper2.equals("<=")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key2){
                            done = true;
                            break;
                        }

                        if(keyList.get(i) >= key1){
                            result.add(keyList.get(i));
                        }
                    }

                    if(done == true)
                        break;

                    nodeIter = nodeIter.getOverflowPtr();
                }

                while(right != null){
                    nodeIter = right;
                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();

                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) <= key2){
                                result.add(keyList.get(i));
                            }
                        }

                        nodeIter = nodeIter.getOverflowPtr();
                    }

                    right = right.getrNode();
                }
            }
        }

        return result;
    }

    @Override
    public void delete(String expr){
        StringTokenizer st = new StringTokenizer(expr, " ");

        String oper;
        int key;

        String oper1, oper2;
        int key1, key2;

        Node nodeIter = this.root;
        List<Node> childPtrList = null;
        List<Integer> keyList = null;

        Node right = null, left = null;
        Node nodeIterPar = null;
        boolean found = false;
        boolean clear = false;
        List<Integer> idxList = new ArrayList<Integer>();

        if(st.countTokens() == 2){  // one side format
            oper = st.nextToken();
            key = Integer.parseInt(st.nextToken());
            // System.out.println("oper: " + oper);
            // System.out.println("key: " + key);

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
            nodeIterPar = nodeIter;
            if(oper.equals("=")){
                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) == key){
                            keyList.remove(i);

                            if(nodeIter.getIsOverflow() && keyList.size() == 0){
                                nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                                System.out.println("here");
                            }

                            found = true;
                            break;
                        }
                    }

                    if(found)
                        break;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                }
            } else if(oper.equals(">")){
                right = nodeIter.getrNode();

                while(nodeIter != null){

                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(right != null){

                    nodeIter = right;
                    nodeIter.setOverflowPtr(null);

                    keyList = nodeIter.getKeyList();
                    keyList.clear();

                    right = right.getrNode();
                }
            } else if(oper.equals(">=")){
                right = nodeIter.getrNode();

                while(nodeIter != null){

                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(right != null){
                    nodeIter = right;
                    nodeIter.setOverflowPtr(null);

                    keyList = nodeIter.getKeyList();
                    keyList.clear();

                    right = right.getrNode();
                }
            } else if(oper.equals("<")){
                left = nodeIter.getlNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) < key){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(left != null){
                    nodeIter = left;
                    nodeIter.setOverflowPtr(null);

                    keyList = nodeIter.getKeyList();
                    keyList.clear();

                    left = left.getlNode();
                }
            } else if(oper.equals("<=")){
                left = nodeIter.getlNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();

                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) <= key){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(left != null){
                    nodeIter = left;
                    nodeIter.setOverflowPtr(null);

                    keyList = nodeIter.getKeyList();
                    keyList.clear();

                    left = left.getlNode();
                }
            }
        } else {  // between format
            key1 = Integer.parseInt(st.nextToken());
            oper1 = st.nextToken();
            st.nextToken(); // the 'key' token, skip it
            oper2 = st.nextToken();
            key2 = Integer.parseInt(st.nextToken());

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
            if(oper1.equals("<") && oper2.equals("<")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key1 && keyList.get(i) < key2){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(right != null){
                    nodeIter = right;

                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();
                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) > key1 && keyList.get(i) < key2){
                                idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                                // keyList.remove(i);
                            }
                        }

                        if(keyList.size() == idxList.size()){ // all should be deleted
                            keyList.clear();
                            if(nodeIter.getIsOverflow()){
                                nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                                clear = true;
                            }
                        } else {
                            for(int i = 0; i < idxList.size(); i++){
                                keyList.remove((int)idxList.get(i));
                            }
                        }
                        idxList.clear();

                        if(clear == false)
                            nodeIterPar = nodeIter;

                        nodeIterPar = nodeIter;
                        nodeIter = nodeIter.getOverflowPtr();
                        clear = false;
                    }

                    right = right.getrNode();
                }
            } else if(oper1.equals("<") && oper2.equals("<=")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) > key1 && keyList.get(i) <= key2){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(right != null){
                    nodeIter = right;

                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();
                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) > key1 && keyList.get(i) <= key2){
                                idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                                // keyList.remove(i);
                            }
                        }

                        if(keyList.size() == idxList.size()){ // all should be deleted
                            keyList.clear();
                            if(nodeIter.getIsOverflow()){
                                nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                                clear = true;
                            }
                        } else {
                            for(int i = 0; i < idxList.size(); i++){
                                keyList.remove((int)idxList.get(i));
                            }
                        }
                        idxList.clear();

                        if(clear == false)
                            nodeIterPar = nodeIter;

                        nodeIterPar = nodeIter;
                        nodeIter = nodeIter.getOverflowPtr();
                        clear = false;
                    }

                    right = right.getrNode();
                }

            } else if(oper1.equals("<=") && oper2.equals("<")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key1 && keyList.get(i) < key2){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(right != null){
                    nodeIter = right;

                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();
                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) >= key1 && keyList.get(i) < key2){
                                idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                                // keyList.remove(i);
                            }
                        }

                        if(keyList.size() == idxList.size()){ // all should be deleted
                            keyList.clear();
                            if(nodeIter.getIsOverflow()){
                                nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                                clear = true;
                            }
                        } else {
                            for(int i = 0; i < idxList.size(); i++){
                                keyList.remove((int)idxList.get(i));
                            }
                        }
                        idxList.clear();

                        if(clear == false)
                            nodeIterPar = nodeIter;

                        nodeIterPar = nodeIter;
                        nodeIter = nodeIter.getOverflowPtr();
                        clear = false;
                    }

                    right = right.getrNode();
                }

            } else if(oper1.equals("<=") && oper2.equals("<=")){
                right = nodeIter.getrNode();

                while(nodeIter != null){
                    keyList = nodeIter.getKeyList();
                    for(int i = 0; i < keyList.size(); i++){
                        if(keyList.get(i) >= key1 && keyList.get(i) <= key2){
                            idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                            // keyList.remove(i);
                        }
                    }

                    if(keyList.size() == idxList.size()){ // all should be deleted
                        keyList.clear();
                        if(nodeIter.getIsOverflow()){
                            nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                            clear = true;
                        }
                    } else {
                        for(int i = 0; i < idxList.size(); i++){
                            keyList.remove((int)idxList.get(i));
                        }
                    }
                    idxList.clear();

                    if(clear == false)
                        nodeIterPar = nodeIter;

                    nodeIterPar = nodeIter;
                    nodeIter = nodeIter.getOverflowPtr();
                    clear = false;
                }

                while(right != null){
                    nodeIter = right;

                    while(nodeIter != null){
                        keyList = nodeIter.getKeyList();
                        for(int i = 0; i < keyList.size(); i++){
                            if(keyList.get(i) >= key1 && keyList.get(i) <= key2){
                                idxList.add(i);      // later remove because keyList.remove(i) changes keyList.size() dynamically
                                // keyList.remove(i);
                            }
                        }

                        if(keyList.size() == idxList.size()){ // all should be deleted
                            keyList.clear();
                            if(nodeIter.getIsOverflow()){
                                nodeIterPar.setOverflowPtr(nodeIter.getOverflowPtr());
                                clear = true;
                            }
                        } else {
                            for(int i = 0; i < idxList.size(); i++){
                                keyList.remove((int)idxList.get(i));
                            }
                        }
                        idxList.clear();

                        if(clear == false)
                            nodeIterPar = nodeIter;

                        nodeIterPar = nodeIter;
                        nodeIter = nodeIter.getOverflowPtr();
                        clear = false;
                    }

                    right = right.getrNode();
                }
            }
        }

    }

    @Override
    public void quit() {

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
        Node overflowNodeIter = nodeIter.getOverflowPtr();
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
            emptySize = getFanout() - keyList.size() - 1;
            if(emptySize == 0){
                System.out.print("]");
            } else {
                if(emptySize == 1){
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

            while(overflowNodeIter != null){
                System.out.println();

                keyList = overflowNodeIter.getKeyList();

                emptySize = getFanout() - keyList.size() - 1;

                for(int i = 0; i < identation; i++){
                    System.out.print(" ");
                }

                if(keyList.size() == 0){
                    System.out.print("[");
                } else {
                    for(int i = 0; i < keyList.size(); i++){
                        if(i == 0) {
                            System.out.print("[");
                        } else if(i == keyList.size() - 1){
                            System.out.print(keyList.get(i));
                            break;
                        }
                        System.out.print(keyList.get(i));
                        System.out.print(",");
                    }
                }

                if(emptySize == 0){
                    System.out.print("]");
                } else {
                    if(emptySize == 1){
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

                overflowNodeIter = overflowNodeIter.getOverflowPtr();
            }
        }
        System.out.println();

        if(childPtrList != null){
            for(int i = 0; i < childPtrList.size(); i++){
                display(childPtrList.get(i), identation + 2);
            }
        }
    }
}
