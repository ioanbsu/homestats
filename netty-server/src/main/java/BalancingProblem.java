/**
 * Created by ivanbahdanau on 11/15/15.
 */
public class BalancingProblem {

    private static final String LEFT_PARENTHESIS = "(";
    private static final String RIGHT_PARENTHESIS = ")";

    public boolean isBalanced(final String stringToCheck, final String left, final String right) {
        int leftIndex = stringToCheck.lastIndexOf(left);
        if (leftIndex == -1) {
            return !stringToCheck.contains(right);
        }
        int rightIndex = stringToCheck.indexOf(right, leftIndex + 1);
        if (rightIndex == -1) {
            return false;
        }
        return isBalanced(stringToCheck.substring(0,
          leftIndex) + stringToCheck.substring(rightIndex + 1), left, right);
    }

    public static void main(String[] args) {
        BalancingProblem bp = new BalancingProblem();
        System.out.println("(thing()some)and(test)() => " + bp.isBalanced("(thing()some)and(test)()", LEFT_PARENTHESIS,
          RIGHT_PARENTHESIS));
        System.out.println("4 +(4 - 4) * 6 => " + bp.isBalanced("4 +(4 - 4) * 6", LEFT_PARENTHESIS, RIGHT_PARENTHESIS));
        System.out.println("() => " + bp.isBalanced("()", LEFT_PARENTHESIS, RIGHT_PARENTHESIS));
        System.out.println(")( => " + bp.isBalanced(")(", LEFT_PARENTHESIS, RIGHT_PARENTHESIS));
        System.out.println("()end => " + bp.isBalanced("()end", LEFT_PARENTHESIS, RIGHT_PARENTHESIS));
        System.out.println("start() => " + bp.isBalanced("start()", LEFT_PARENTHESIS, RIGHT_PARENTHESIS));
        System.out.println(
          "()()()()))(())((()))(( => " + bp.isBalanced("()()()()))(())((()))((", LEFT_PARENTHESIS, RIGHT_PARENTHESIS));

    }

}
