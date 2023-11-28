import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Parser {
    static ConcurrentHashMap<String, String[]> rules = new ConcurrentHashMap<>();
    static Set<String> terminals = new HashSet<>();
    static ArrayList<String> terminal_idx = new ArrayList<>();
    static ArrayList<String> nterminal_idx = new ArrayList<>();
    static ArrayList<String> rules_idx = new ArrayList<>();
    static int[][] p_table;
    static ArrayList<String> left_recur = new ArrayList<>();

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int numOfRules = in.nextInt();
        in.nextLine();
        for (int i = 0; i < numOfRules; i++) {
            String rule = in.nextLine();
            String[] splited1 = rule.split("->");
            String[] splited2 = splited1[1].split("\\|");


            for (int j = 0; j < splited2.length; j++) {
                String s = splited2[j];
                rules_idx.add(splited1[0] + "->" + s);
                for (int k = 0; k < s.length(); k++) {
                    if (!Character.isUpperCase(s.charAt(k))) {
                        terminals.add(String.valueOf(s.charAt(k)));

                    }
                }
            }
            rules.put(splited1[0], splited2);
        }
        terminal_idx.addAll(terminals);
        nterminal_idx.addAll(rules.keySet());
        terminal_idx.remove("~");
        terminal_idx.add("$");
        System.out.println("*** Rules with their corresponding number *** ");
        for (int i = 0; i < rules_idx.size(); i++) {
            System.out.println("                " + i + ": " + rules_idx.get(i));
        }
        System.out.println();
        p_table = new int[rules.size()][terminals.size()];
        for (int j = 0; j < rules.size(); j++) {
            for (int k = 0; k < terminals.size(); k++) {
                p_table[j][k] = -1;
            }
        }
        System.out.println("*** Parse table of the given Grammar ***");
        System.out.println();
        createTable(findFirsts(), findFollows());
        System.out.println();
        System.out.print("Enter your Token input: ");

        String input = in.nextLine();
        System.out.println();
        parse(input);

    }

    private static void parse(String input) {
        Stack<String> stack = new Stack<>();
        stack.push("$");
        stack.push("S");
        Queue<String> buffer = new LinkedList<>();
        String[] split = input.split("");
        for (int i = 0; i < input.length(); i++) {
            buffer.add(split[i]);
        }
        buffer.add("$");

        while (true) {
            System.out.println("buffer: " + buffer);
            System.out.println("stack: " + stack);
            System.out.println();
            if (stack.size() == 1 && buffer.size() == 1) {
                System.out.println("input String can be parsed");
                System.exit(0);
            } else if (nterminal_idx.contains(stack.peek())) {
                int x = nterminal_idx.indexOf(stack.peek());
                int y = terminal_idx.indexOf(buffer.peek());
                if (x == -1 || y == -1) {
                    System.out.println("invalid input string");
                    System.exit(0);
                }
                if (p_table[x][y] != -1) {
                    String rule = rules_idx.get(p_table[x][y]);
                    String[] left_and_right = rule.split("->");
                    String[] splited_rule = left_and_right[1].split("");
                    stack.pop();
                    if (!splited_rule[0].equals("~")) {
                        for (int i = splited_rule.length - 1; i >= 0; i--) {
                            stack.push(splited_rule[i]);
                        }
                    }


                } else {
                    System.out.println("invalid input string");
                    System.exit(0);
                }
            } else {
                if (stack.peek().equals(buffer.peek())) {
                    stack.pop();
                    buffer.remove();
                } else {
                    System.out.println("invalid input string");
                    System.exit(0);
                }
            }
        }
    }

    private static void createTable(ConcurrentHashMap<String, Set<String>> firsts
            , ConcurrentHashMap<String, Set<String>> follows) {
        for (String s : rules.keySet()) {
            String[] rhs = rules.get(s);
            for (String r : rhs) {
                String[] res = first(r, new ArrayList<>());
                ArrayList<String> res_temp = new ArrayList<>(Arrays.asList(res));
                if (res_temp.contains("~")) {
                    if (res_temp.size() == 1) {
                        ArrayList<String> firstFollow = new ArrayList<>();
                        Set<String> follows_temp = follows.get(s);
                        firstFollow.addAll(follows_temp);
                        res_temp = firstFollow;
                    } else {
                        res_temp.remove("~");
                        res_temp.addAll(follows.get(s));
                    }


                }

                for (String result : res_temp) {
                    int x_indx = nterminal_idx.indexOf(s);
                    int y_indx = terminal_idx.indexOf(result);
                    if (p_table[x_indx][y_indx] == -1) {
                        p_table[x_indx][y_indx] = rules_idx.indexOf(s + "->" + r);
                    } else if (p_table[x_indx][y_indx] == rules_idx.indexOf(s + "->" + r)) {
                        continue;
                    } else {
                        System.out.println("this is not a LL1 grammar babayyyeeeee");
                        System.exit(0);
                    }
                }


            }
        }
        System.out.print("   ");
        for (int i = 0; i < terminal_idx.size(); i++) {
            System.out.print(terminal_idx.get(i) + "  ");

        }
        System.out.println();
        System.out.println();
        for (int i = 0; i < nterminal_idx.size(); i++) {
            System.out.print(nterminal_idx.get(i) + "  ");
            for (int j = 0; j < terminal_idx.size(); j++) {
                if (p_table[i][j] == -1) {
                    System.out.print("-  ");

                } else
                    System.out.print(p_table[i][j] + "  ");
            }
            System.out.println();
            System.out.println();
        }
    }


    private static ConcurrentHashMap<String, Set<String>> findFirsts() {
        ConcurrentHashMap<String, Set<String>> firsts = new ConcurrentHashMap<>();
        int j = 0;
        String[] result = null;
        for (String s : rules.keySet()) {
            String[] rhs = rules.get(s);
            Set<String> first = new HashSet<>();
            for (String r : rhs) {
                result = first(r, new ArrayList<>());
                for (int i = 0; i < result.length; i++) {
                    first.add(result[i]);
                }
                firsts.put(s, first);
            }
        }
        return firsts;
    }

    private static ConcurrentHashMap<String, Set<String>> findFollows() {
        ConcurrentHashMap<String, Set<String>> follows = new ConcurrentHashMap<>();
        int j = 0;
        String[] result = null;
        for (String f : rules.keySet()) {
            follows.put(f, new HashSet<>());
        }
        for (String s : rules.keySet()) {
            String[] rhs = rules.get(s);
            result = follow(s, null);
            Set<String> follow = new HashSet<>();
            if (result != null) {

                for (int i = 0; i < result.length; i++) {
                    follow.add(result[i]);
                }
                if (follows.get(s).size() == 0)
                    follows.put(s, follow);
                for (int i = 0; i < rhs.length; i++) {
                    String sub = rhs[i].substring(rhs[i].length() - 1);
                    Character my_char = sub.charAt(0);
                    if (Character.isUpperCase(my_char)) {
                        for (int k = 0; k < rules.get(sub).length; k++) {
                            if (rules.get(sub)[k].substring(rules.get(sub)[k].length() - 1).equals(s)) {
                                if (follows.get(sub).size() == 0)
                                    follows.put(sub, follows.get(s));
                            }
                        }
                    }
                }
            }
        }

        return follows;
    }


    private static String[] follow(String r, String prev) {
        Set<String> follows = new HashSet<>();
        String[] res = null;


        if (r.equals("S")) {
            follows.add("$");
        }

        for (String s : rules.keySet()) {
            String[] rhs = rules.get(s);
            for (String rhs_sub : rhs) {
                if (rhs_sub.contains(r)) {
                    int idx = rhs_sub.indexOf(r);
                    rhs_sub = rhs_sub.substring(idx + 1);

                    if (rhs_sub.length() != 0) {
                        res = first(rhs_sub, new ArrayList<>());
                        ArrayList<String> res_temp = new ArrayList<>();
                        for (int i = 0; i < res.length; i++) {
                            res_temp.add(res[i]);
                        }
                        if (res_temp.contains("~")) {
                            ArrayList<String> newAns = new ArrayList<>();
                            res_temp.remove("~");
                            String[] answers = follow(s, r);
                            newAns.addAll(res_temp);
                            if (answers != null) {
                                newAns.addAll(Arrays.asList(answers));
                            }
                            res = newAns.toArray(new String[0]);
                        }
                    } else if (!r.equals(s)) {
                        if (prev != null) {
                            if (!prev.equals(s)) {
                                res = follow(s, r);
                            }
                        } else
                            res = follow(s, r);
                    }
                    if (res != null) {
                        follows.addAll(Arrays.asList(res));
                    }

                }
            }
        }
        return follows.toArray(new String[0]);
    }

    private static String[] first(String rhs, ArrayList<String> left_recur) {
        String firstChar = String.valueOf(rhs.charAt(0));
        if (!Character.isUpperCase(rhs.charAt(0)) | rhs.charAt(0) == '~') {
            String[] res = {firstChar};
            return res;
        }

        if (rules.containsKey(firstChar)) {
            ArrayList<String> firsts = new ArrayList<>();
            String[] nextRules = rules.get(firstChar);
            if (Character.isUpperCase(firstChar.charAt(0)))
                left_recur.add(firstChar);
            for (String s : nextRules) {
                if (left_recur.contains(String.valueOf(s.charAt(0)))) {
                    System.out.println("Invalid Grammar(Left recursion)");
                    System.exit(0);
                }
                if (Character.isUpperCase(s.charAt(0)))
                    left_recur.add(String.valueOf(s.charAt(0)));
                String[] temp = first(s, left_recur);
                firsts.addAll(Arrays.asList(temp));
            }

            if (!firsts.contains("~")) {
                return firsts.toArray(new String[0]); // TODO
            } else {
                firsts.remove("~");
                if (rhs.length() > 1) {
                    String[] answers = first(rhs.substring(1), new ArrayList<>());
                    ArrayList<String> newAns = new ArrayList<>(firsts);
                    if (answers != null) {
                        newAns.addAll(Arrays.asList(answers));
                    }
                    return newAns.toArray(new String[0]);
                }
                firsts.add("~");
                return firsts.toArray(new String[0]);
            }


        }

        return null;
    }
}
