public class Test {
    public static void main(String args[]){
        Node n1 = new Node(2, true);
        Node n2 = new Node(2, true);
        Node n3 = new Node(2, true);
        Node n4 = new Node(2, true);
        Node n5 = new Node(2, true);
        Node n6 = new Node(2, true);

        n1.getKeyList().set(0, 10);
        n2.getKeyList().set(0, 20);
        n3.getKeyList().set(0, 33);
        n4.getKeyList().set(0, 40);
        n5.getKeyList().set(0, 51);
        n6.getKeyList().set(0, 63);

        n1.setrNode(n2);
        n2.setrNode(n3);
        n3.setrNode(n4);
        n4.setrNode(n5);
        n5.setrNode(n6);
        n6.setrNode(null);

        n6.setlNode(n5);
        n5.setlNode(n4);
        n4.setlNode(n3);
        n3.setlNode(n2);
        n2.setlNode(n1);
        n1.setlNode(null);

        Node nodeIter = n1;
        while(nodeIter != null){
            System.out.println(nodeIter.getKeyList().get(0));
            nodeIter = nodeIter.getrNode();
        }

        System.out.println("----------------------------");

        nodeIter = n6;
        while(nodeIter != null){
            System.out.println(nodeIter.getKeyList().get(0));
            nodeIter = nodeIter.getlNode();
        }
    }   
}
