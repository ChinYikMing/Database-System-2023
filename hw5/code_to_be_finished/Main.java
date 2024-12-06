import java.util.*;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class Main{
    private static int ds;
    public static int getDs() {
        return ds;
    }

    public static void setDs(int ds) {
        Main.ds = ds;
    }

    private static int op;
    public static int getOp() {
        return op;
    }

    public static void setOp(int op) {
        Main.op = op;
    }

    private static int state = State.DS;
    public static int getState() {
        return state;
    }

    public static void setState(int state) {
        Main.state = state;
    }

    private static Tree bst = new BST();
    private static Tree bpt = new BPT();
    private static Tree isam = new ISAM();

    public static JFrame jf = new JFrame();
    public static JTree jt = new JTree();
    public static TreeModel tm = null;

    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);
        jf.setVisible(false);

        System.out.println(ConsoleColors.CYAN + "Welcome!" + ConsoleColors.RESET);
        printStateInfo();

        while(scanner.hasNextLine()){
            run(scanner.nextLine().trim());

            if(state == State.QUIT){
                System.out.println(ConsoleColors.CYAN + "Goodbye!" + ConsoleColors.RESET);
                break;
            }
        }
        scanner.close();
    }

    private static void printStateInfo(){
        System.out.println(ConsoleColors.PURPLE_BOLD);
        if(state == State.DS){
            System.out.println(ConsoleColors.RED_BOLD + "(enter q to quit)" + ConsoleColors.RESET);
            System.out.println("please select a data structure:");
            System.out.println("(1)b+tree (2)isam (3)bst");
        } else if(state == State.OP){
            System.out.println(ConsoleColors.RED_BOLD + "(enter c to change data structure or q to quit)" + ConsoleColors.RESET);
            if(ds == Ds.BPT){
                System.out.println("please select a operation on b+tree:");
                System.out.println("(1)insert (2)lookup (3)delete (4)display (5)display(GUI)");
            } else if(ds == Ds.ISAM){
                System.out.println("please select a operation on isam:");
                System.out.println("(1)insert (2)lookup (3)delete (4)display");
            } else if(ds == Ds.BST){
                System.out.println("please select a operation on bst:");
                System.out.println("(1)insert (2)lookup (3)delete (4)display");
            }
        } else if(state == State.KEY){
            System.out.println(ConsoleColors.RED_BOLD + "(enter c to change operation or cc to change data structure or q to quit)" + ConsoleColors.RESET);
            if(ds == Ds.BPT){
                if(op == Operation.INSERT){
                    System.out.println("please enter keys(separated by space, e.g., 1 3 5 7 9) to insert to b+tree:");
                } else if(op == Operation.LOOKUP){
                    System.out.println("please enter a key to lookup from b+tree:");
                } else if(op == Operation.DELETE){
                    System.out.println("please enter keys(separated by space, e.g., 1 3 5 7 9) to delete from b+tree");
                }
            } else if(ds == Ds.ISAM){
                if(op == Operation.INSERT){
                    System.out.println("please enter keys(separated by space, e.g., 1 3 5 7 9) to insert to isam:");
                } else if(op == Operation.LOOKUP){
                    System.out.println("please enter a key to lookup from isam:");
                } else if(op == Operation.DELETE){
                    System.out.println("please enter keys(separated by space, e.g., 1 3 5 7 9) to delete from isam:");
                }
            } else if(ds == Ds.BST){
                if(op == Operation.INSERT){
                    System.out.println("please enter keys(separated by space, e.g., 1 3 5 7 9) to insert to bst:");
                } else if(op == Operation.LOOKUP){
                    System.out.println("please enter a key to lookup from bst:");
                } else if(op == Operation.DELETE){
                    System.out.println("please enter keys(separated by space, e.g., 1 3 5 7 9) to delete from bst");
                }
            }
        } else if(state == State.FO){
            System.out.println(ConsoleColors.RED_BOLD + "(enter c to change data structure or q to quit)" + ConsoleColors.RESET);
            if(ds == Ds.BPT){
                System.out.println("please enter a num to set the order of b+tree:");
            } else if(ds == Ds.ISAM){
                System.out.println("fanout must be 3 or 4 or 5 or 7 or 9 or 13 or 25");
                System.out.println("please enter a num to set the fanout of isam:");
            }
        // } else if(state == State.PS){
        //     System.out.println(ConsoleColors.RED_BOLD + "(enter c to change data structure or cc to change fanout or q to quit)" + ConsoleColors.RESET);
        //     if(ds == Ds.BPT){
        //         System.out.println("please enter a num to set the page size of b+tree:");
        //     } else if(ds == Ds.ISAM){
        //         System.out.println("please enter a num to set the page size of isam:");
        //     }
        } else if(state == State.RANGEEXPR){
            System.out.println(ConsoleColors.RED_BOLD + "(enter c to change operation or cc to change data structure or q to quit)" + ConsoleColors.RESET);
            if(ds == Ds.BPT){
                if(op == Operation.LOOKUP){
                    System.out.println("please enter a expression for range searching in b+tree:");
                } else if(op == Operation.DELETE){
                    System.out.println("please enter a expression for deleting in b+tree:");
                }
            } else if(ds == Ds.ISAM){
                if(op == Operation.LOOKUP){
                    System.out.println("please enter a expression for range searching in isam:");
                } else if(op == Operation.DELETE){
                    System.out.println("please enter a expression for deleting in isam:");
                }
            }
            System.out.println("for example: = 6, > 6, >= 6, < 6, >= 6, 32 <= key < 100, 22 < key < 101");
        }
        System.out.println(ConsoleColors.RESET);
    }

    private static void usage(String input){
        System.out.println(ConsoleColors.RED_BOLD + "Invalid input: " + input + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "Usage: please only use suggested options or inputs" + ConsoleColors.RESET);
        // System.out.println("\tfor example as below");
        // System.out.println(ConsoleColors.YELLOW_BOLD + "\tplease select a data structure:" + ConsoleColors.RESET);
        // System.out.println("\t(1)b+tree (2)isam (3)bst (4)quit");
        // System.out.println(ConsoleColors.YELLOW_BOLD + "\tplease select a operation:" + ConsoleColors.RESET);
        // System.out.println("\t(1)insert (2)lookup (3)delete (4)display " +
        //                         "(5)change data structure (6)quit");
        // System.out.println(ConsoleColors.RED_BOLD + "\t(enter c to change operation or q to quit)" + ConsoleColors.RESET);
        // System.out.println(ConsoleColors.YELLOW_BOLD + "\tplease enter a key to xxx:" + ConsoleColors.RESET);
    }

    private static boolean inputValidation(String input, List<Integer> num_list){
        if(state == State.DS){
            if(input.equals("q")){
                    return true;
            } else {
                try {
                    int num = Integer.parseInt(input);
                    if(num != 1 && num != 2 && num != 3)
                        return false;
                    return true;
                } catch (NumberFormatException e) {
                    // System.out.println("input string is not numeric");
                }

                return false;
            }
        } else if(state == State.OP){
            if(input.equals("c") || input.equals("q")){
                    return true;
            } else {
                switch(ds){
                    case Ds.BPT:
                        try {
                            int num = Integer.parseInt(input);
                            if(num != 1 && num != 2 && num != 3 && num != 4 && num != 5)
                                return false;
                            return true;
                        } catch (NumberFormatException e) {
                            // System.out.println("input string is not numeric");
                        }
                        break;
                    case Ds.ISAM:
                        try {
                            int num = Integer.parseInt(input);
                            if(num != 1 && num != 2 && num != 3 && num != 4)
                                return false;
                            return true;
                        } catch (NumberFormatException e) {
                            // System.out.println("input string is not numeric");
                        }
                        break;
                    default:
                        try {
                            int num = Integer.parseInt(input);
                            if(num != 1 && num != 2 && num != 3 && num != 4)
                                return false;
                            return true;
                        } catch (NumberFormatException e) {
                            // System.out.println("input string is not numeric");
                        }
                        break;
                }
                return false;
            }
        } else if(state == State.KEY){
            if(input.equals("c") || input.equals("cc") || 
                input.equals("q")){
                    return true;
            } else {
                try {
                    StringTokenizer st = new StringTokenizer(input, " ");
                    while(st.hasMoreTokens()){
                        num_list.add(Integer.parseInt(st.nextToken()));
                    }
                    // Integer.parseInt(input);
                    return true;
                } catch (NumberFormatException e) {
                    // System.out.println("input string is not numeric");
                }

                return false;
            }
        } else if(state == State.FO){
                if(input.equals("c") || input.equals("q")){
                        return true;
                } else {
                    if(ds == Ds.ISAM){
                        try {
                            int fanout = Integer.parseInt(input);
                            if(fanout != 3 && fanout != 4 && fanout != 5 &&
                                fanout != 7 && fanout != 9 && fanout != 13 && fanout != 25)
                                return false;
                            return true;
                        } catch (NumberFormatException e) {
                            // System.out.println("input string is not numeric");
                        }
                        return false;
                    } else if(ds == Ds.BPT){
                        try {
                            int fanout = Integer.parseInt(input);
                            if(fanout <= 0)
                                return false;
                            return true;
                        } catch (NumberFormatException e) {
                            // System.out.println("input string is not numeric");
                        }
                        return false;
                    }
                }
        // } else if(state == State.PS){
        //     if(input.equals("c") || input.equals("cc") || 
        //         input.equals("q")){
        //             return true;
        //     } else {
        //         try {
        //             Integer.parseInt(input);
        //             return true;
        //         } catch (NumberFormatException e) {
        //             // System.out.println("input string is not numeric");
        //         }

        //         return false;
        //     }
        } else if(state == State.RANGEEXPR){
            if(input.equals("c") || input.equals("cc") || 
                input.equals("q")){
                    return true;
            } else {
                // String rangeExprRegex = "^[><=]=?\\s+-?\\d+(\\.\\d+)?$";
                String rangeExprRegex = "^[><=]=?\\s+-?\\d+(\\.\\d+)?|\\d+(\\.\\d+)?\\s+-?[><=]=?\\s+-?key\\s+-?[><=]=?\\s+-?\\d+(\\.\\d+)?$";
                boolean valid = Pattern.matches(rangeExprRegex, input);  

                return valid;
            }
        }

        return true;
    }

    private static void run(String input){
        DefaultMutableTreeNode n = null;
        TreeModel tm = null;

        if(state == State.DS){
            if(!inputValidation(input, null)){
                usage(input);
            } else {
                if(input.equals("q")){
                    setState(State.QUIT);
                } else {
                    setDs(Integer.parseInt(input));
                    switch(ds){
                        case Ds.BPT:
                        case Ds.ISAM:
                            setState(State.FO);
                            break;
                        case Ds.BST:
                            setState(State.OP);
                        default:
                            break;
                    }
                }
            }
        } else if(state == State.FO){
            if(!inputValidation(input, null)){
                usage(input);
            } else {
                if(input.equals("c")){
                    setState(State.DS);
                } else if(input.equals("q")){
                    setState(State.QUIT);
                } else {
                    // setState(State.PS);
                    setState(State.OP);
                    switch(ds){
                        case Ds.BPT:
                            // bpt.setFanout(Integer.parseInt(input));
                            bpt.init(Integer.parseInt(input));
                            break;
                        case Ds.ISAM:
                            isam.init(Integer.parseInt(input));
                            // isam.setFanout(Integer.parseInt(input));
                            break;
                        default:
                            break;
                    }
                }
            }
        // } else if(state == State.PS){
        //     if(!inputValidation(input, null)){
        //         usage(input);
        //     } else {
        //         if(input.equals("c")){
        //             setState(State.DS);
        //         } else if(input.equals("cc")){
        //             setState(State.FO);
        //         } else if(input.equals("q")){
        //             setState(State.QUIT);
        //         } else {
        //             int pageSize = Integer.parseInt(input);

        //             setState(State.OP);
        //             switch(ds){
        //                 case Ds.BPT:
        //                     ((BPT) bpt).setPageSize(pageSize);
        //                     bpt.init(bpt.getFanout());
        //                     break;
        //                 case Ds.ISAM:
        //                     ((ISAM) isam).setPageSize(Integer.parseInt(input));
        //                     isam.init(isam.getFanout());
        //                     break;
        //                 default:
        //                     break;
        //             }
        //         }
        //     }
        } else if(state == State.OP){
            if(!inputValidation(input, null)){
                usage(input);
            } else {
                if(input.equals("c")){
                    setState(State.DS);
                } else if(input.equals("q")){
                    setState(State.QUIT);
                } else {
                    setOp(Integer.parseInt(input));
                    switch(Integer.parseInt(input)){
                        case Operation.INSERT:
                            setState(State.KEY);
                            break;
                        case Operation.LOOKUP:
                            switch(ds){
                                case Ds.BPT:
                                case Ds.ISAM:
                                    setState(State.RANGEEXPR);
                                    break;
                                default:
                                    setState(State.KEY);
                                    break;
                            }
                            break;
                        case Operation.DELETE:
                            switch(ds){
                                case Ds.BPT:
                                case Ds.ISAM:
                                    setState(State.RANGEEXPR);
                                    break;
                                default:
                                    setState(State.KEY);
                                    break;
                            }
                            break;
                        case Operation.DISPLAY:
                            switch(ds){
                                case Ds.BPT:
                                    setState(State.OP);
                                    bpt.display(bpt.getRoot(), 0);
                                    break;
                                case Ds.ISAM:
                                    setState(State.OP);
                                    isam.display(isam.getRoot(), 0);
                                    break;
                                case Ds.BST:
                                    setState(State.OP);
                                    bst.display(bst.getRoot(), 0);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case Operation.DISPLAYGUI:
                            setState(State.OP);
                            n = ((BPT) bpt).displayGUI(bpt.getRoot(), null);
                            tm = new DefaultTreeModel(n);
                            jt.setModel(tm);

                            // expand all paths
                            for (int i = 0; i < jt.getRowCount(); i++) {
                                jt.expandRow(i);
                            }

                            jf.add(jt);
                            jf.setSize(500, 500);
                            jf.setVisible(true);
                            break;
                        case Operation.RANGESEARCH:
                            setState(State.RANGEEXPR);
                            break;
                        default:
                            break;
                    }
                }
            }
        } else if(state == State.KEY){
            List<Integer> num_list = new ArrayList<Integer>();

            if(!inputValidation(input, num_list)){
                usage(input);
            } else {
                if(input.equals("c")){
                    setState(State.OP);
                } else if(input.equals("cc")){
                    setState(State.DS);
                } else if(input.equals("q")){
                    setState(State.QUIT);
                } else {
                    setState(State.OP);
                    switch(op){
                        case Operation.INSERT:
                            switch(ds){
                                case Ds.BPT:
                                    for(Integer num : num_list){
                                        System.out.println("Insert key " + num + ":");
                                        bpt.insert(num);
                                        bpt.display(bpt.getRoot(), 0);
                                        System.out.println();
                                    }
                                    break;
                                case Ds.ISAM:
                                    for(Integer num : num_list){
                                        isam.insert(num);
                                    }
                                    break;
                                case Ds.BST:
                                    for(Integer num : num_list){
                                        bst.insert(num);
                                    }
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case Operation.LOOKUP:  // for bst
                            num_list = bst.lookup(input);
                            if(num_list != null){
                                System.out.println(ConsoleColors.GREEN_BOLD + "Found key " + input + ConsoleColors.RESET);
                            } else {
                                System.out.println(ConsoleColors.RED_BOLD + "Not found key " + input + ConsoleColors.RESET);
                            }
                            break;
                        case Operation.DELETE:  // for bst
                            bst.delete(input);
                            break;
                        default:
                            break;
                    }
                }
            }
        } else if(state == State.RANGEEXPR){
            if(!inputValidation(input, null)){
                usage(input);
            } else {
                List<Integer> result = null;

                if(input.equals("c")){
                    setState(State.OP);
                } else if(input.equals("cc")){
                    setState(State.DS);
                } else if(input.equals("q")){
                    setState(State.QUIT);
                } else {
                    setState(State.OP);
                    switch(op){
                        case Operation.LOOKUP:
                            switch(ds){
                                case Ds.BPT:
                                    result = bpt.lookup(input);
                                    break;
                                case Ds.ISAM:
                                    result = isam.lookup(input);
                                    break;
                                default:
                                    break;
                            }
                            if(result == null){
                                System.out.println(ConsoleColors.RED_BOLD);
                                System.out.println("Empty");
                                System.out.println(ConsoleColors.RESET);
                            } else if(result.size() != 0){
                                System.out.println(ConsoleColors.GREEN_BOLD);
                                System.out.print("Result: ");
                                for(int i = 0; i < result.size(); i++){
                                    if(i == result.size() - 1){
                                        System.out.print(result.get(i));
                                        break;
                                    }
                                    System.out.print(result.get(i) + ",");
                                }
                                System.out.println(ConsoleColors.RESET);
                            } else {
                                System.out.println(ConsoleColors.RED_BOLD);
                                System.out.println("No result!");
                                System.out.println(ConsoleColors.RESET);
                            }
                            break;
                        case Operation.DELETE:
                            switch(ds){
                                case Ds.BPT:
                                    bpt.delete(input);
                                    bpt.display(bpt.getRoot(), 0);
                                    System.out.println();
                                    break;
                                case Ds.ISAM:
                                    isam.delete(input);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        printStateInfo();
    }
}