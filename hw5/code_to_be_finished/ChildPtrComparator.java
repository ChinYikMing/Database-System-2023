import java.util.Comparator;
import java.util.List;

public class ChildPtrComparator implements Comparator<Node> {
    @Override
    public int compare(final Node n1, final Node n2) {

        // if(n1 == null && n2 != null){
        //     List<Integer> n2KeyList = n2.getKeyList();
        //     if(n2KeyList.size() == 0){
        //         return -1;
        //     }

        //     return n2KeyList.get(0).compareTo(null);
        // } else if(n1 != null && n2 == null){
        //     List<Integer> n1KeyList = n1.getKeyList();
        //     if(n1KeyList.size() == 0){
        //         return 1;
        //     }

        //     return n1KeyList.get(0).compareTo(null);
        // } else {
        //     List<Integer> n1KeyList = n1.getKeyList();
        //     List<Integer> n2KeyList = n2.getKeyList();

        //     if(n1KeyList.size() == 0 && n2KeyList.size() == 0){
        //         return 1;
        //     } else if(n1KeyList.size() == 0 && n2KeyList.size() != 0){
        //         return -1;
        //     } else if(n1KeyList.size() != 0 && n2KeyList.size() == 0){
        //         return 1;
        //     }
        
        //     return n1KeyList.get(0).compareTo(n2KeyList.get(0));
        // }
        
        List<Integer> n1KeyList = n1.getKeyList();
        List<Integer> n2KeyList = n2.getKeyList();
        if(n1KeyList != null && n2KeyList == null){
            return n1KeyList.get(0).compareTo(null);
        } else if(n1KeyList == null && n2KeyList != null){
            return n2KeyList.get(0).compareTo(null);
        }
        
        return n1KeyList.get(0).compareTo(n2KeyList.get(0));
    }
}