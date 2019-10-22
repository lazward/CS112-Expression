package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is
	 * created and stored, even if it appears more than once in the expression. At
	 * this time, values for all variables and all array items are set to zero -
	 * they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr
	 *            The expression
	 * @param vars
	 *            The variables array list - already created by the caller
	 * @param arrays
	 *            The arrays array list - already created by the caller
	 */
	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		/**
		 * DO NOT create new vars and arrays - they are already created before being
		 * sent in to this method - you just need to fill them in.
		 **/

		String trimmedDown = expr.replaceAll("\\s+", "");
		trimmedDown = trimmedDown.replaceAll("[()\\]]", "");
		trimmedDown = trimmedDown.replaceAll("[-+*/]", " ");
		trimmedDown = trimmedDown.replaceAll("\\[", "[ ");

		String[] s = trimmedDown.split("\\s+");

		for (int i = 0; i < s.length; i++) {

			if (!Character.isDigit(s[i].charAt(0))) {

				if (s[i].indexOf('[') != -1) {

					Array a = new Array(s[i].substring(0, s[i].indexOf('[')));

					if (!arrays.contains(a)) {

						arrays.add(a);

					}

					for (int j = s[i].indexOf('['); j < s[i].length(); j++) {

						if (Character.isLetter(s[i].charAt(j))) {

							String v = "";
							int k = j;

							while (j < s[i].length() && Character.isLetter(s[i].charAt(j))) {

								v = s[i].substring(k, j + 1);
								j++;

							}

							Variable inside = new Variable(v);
							if (!vars.contains(inside)) {

								vars.add(inside);

							}

						}

					}

				} else {

					if (s[i].indexOf(']') != -1) {

						Variable a = new Variable(s[i].substring(0, s[i].indexOf(']')));
						vars.add(a);

					} else {
						Variable a = new Variable(s[i]);
						vars.add(a);

					}

				}

			}

		}

	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc
	 *            Scanner for values input
	 * @throws IOException
	 *             If there is a problem with the input
	 * @param vars
	 *            The variables array list, previously populated by
	 *            makeVariableLists
	 * @param arrays
	 *            The arrays array list - previously populated by makeVariableLists
	 */
	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars
	 *            The variables array list, with values for all variables in the
	 *            expression
	 * @param arrays
	 *            The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		// following line just a placeholder for compilation

		String expression = expr.replaceAll("\\s+", "");

		String numbers = "";

		int bracketCount = 0;

		int parenthesisCount = 0;

		float output = 0;

		for (int i = 0; i < expression.length(); i++) {

			String part = "";

			if (expression.charAt(i) == '(') {

				parenthesisCount++;

				int start = i + 1;
				int end = 0;

				for (i = start; i < expression.length(); i++) {

					if (expression.charAt(i) == '(') {

						parenthesisCount++;

					}

					if (expression.charAt(i) == ')') {

						parenthesisCount--;

						if (parenthesisCount == 0) {

							end = i;
							break;

						}

					}

				}

				float inside = evaluate(expression.substring(start, end), vars, arrays);

				numbers = numbers + String.format("%.12f", inside); // This is to compensate for scientific notation
																	// when converting floats to strings.

			}

			if (Character.isLetter(expression.charAt(i))) {

				int startOfVar = i;

				while (i < expression.length()) {

					if (Character.isLetter(expression.charAt(i))) {

						part = expression.substring(startOfVar, i + 1);

					} else {

						break;
					}

					i++;

				}

				if (i >= expression.length()) { // Is this the end?

					int numberToAdd = 0;

					for (int j = 0; j < vars.size(); j++) {

						if (vars.get(j).name.equals(part)) {

							numberToAdd = vars.get(j).value;
							break;

						}

					}

					numbers = numbers + String.valueOf(numberToAdd);

					break;

				}

				if (expression.charAt(i) == '[') {

					int start = i + 1;

					int end = 0;

					for (int j = start; j < expression.length(); j++) {

						if (expression.charAt(j) == ']') {

							if (bracketCount == 0) {

								end = j;

							} else {

								bracketCount--;

							}

						} else if (expression.charAt(j) == '[') {

							bracketCount++;

						}
					}

					int arrayIndex = 0;

					for (int l = 0; l < arrays.size(); l++) {

						Array get = arrays.get(l);

						if (get.name.equals(part)) {

							arrayIndex = l;
							break;

						}

					}

					int insideBracket = (int) evaluate(expression.substring(start, end), vars, arrays);

					float numberToAdd = arrays.get(arrayIndex).values[insideBracket];

					String addConverted = Float.toString(numberToAdd);

					numbers = numbers + addConverted;

					i = end;

					continue;

				} else {

					int numberToAdd = 0;

					for (int j = 0; j < vars.size(); j++) {

						if (vars.get(j).name.equals(part)) {

							numberToAdd = vars.get(j).value;
							break;

						}

					}

					numbers = numbers + String.valueOf(numberToAdd);

				}
			}

			if (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '+' || expression.charAt(i) == '-'
					|| expression.charAt(i) == '*' || expression.charAt(i) == '/') {

				numbers = numbers + expression.charAt(i);

			}

		}

		for (int i = 1; i < numbers.length(); i++) { // PEMDAS

			int j;
			int k;
			String firstNumberS = "";
			String secondNumberS = "";
			float firstNumber;
			float secondNumber;

			int cutoff = 0;

			switch (numbers.charAt(i)) {

			case '*': // Multiplication

				j = i - 1;
				k = i + 1;
				firstNumberS = "";
				secondNumberS = "";
				while (j >= 0) {

					if (numbers.charAt(j) == '-') { // Negative numbers won't be in the expressionession but it could
													// still
													// lead to negative outputs

						if (j > 0) {

							if (Character.isDigit(numbers.charAt(j - 1)) || Character.isLetter(numbers.charAt(j - 1))) {

							} else {

								firstNumberS = numbers.substring(j, i);
								j--;

							}

							break;

						} else if (j == 0) {

							firstNumberS = numbers.substring(j, i);
							j--;
							break;

						} else {

							break;

						}

					}

					if (!Character.isDigit(numbers.charAt(j)) && numbers.charAt(j) != '.') {

						break;

					}

					firstNumberS = numbers.substring(j, i);

					j--;

				}

				j++;

				if (numbers.charAt(k) == '-') {

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				while (k < numbers.length()) {

					if (!Character.isDigit(numbers.charAt(k)) && numbers.charAt(k) != '.') {

						break;

					}

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				firstNumber = Float.parseFloat(firstNumberS);
				secondNumber = Float.parseFloat(secondNumberS);

				float product = (firstNumber * secondNumber);

				String productS = String.format("%.12f", product);

				if (j == 0) {

					cutoff = productS.length();

					numbers = productS + numbers.substring(k);

				} else {

					String cut = numbers.substring(0, j) + productS;
					cutoff = cut.length();

					numbers = numbers.substring(0, j) + productS + numbers.substring(k);

				}

				i = cutoff - 1;

				break;
			case '/':

				j = i - 1;
				k = i + 1;
				firstNumberS = "";
				secondNumberS = "";
				while (j >= 0) {

					if (numbers.charAt(j) == '-') {

						if (j > 0) {

							if (Character.isDigit(numbers.charAt(j - 1)) || Character.isLetter(numbers.charAt(j - 1))) {

							} else {

								firstNumberS = numbers.substring(j, i);
								j--;

							}

							break;

						} else if (j == 0) {

							firstNumberS = numbers.substring(j, i);
							j--;
							break;

						} else {

							break;

						}

					}

					if (!Character.isDigit(numbers.charAt(j)) && numbers.charAt(j) != '.') {

						break;

					}

					firstNumberS = numbers.substring(j, i);

					j--;

				}

				j++;

				if (numbers.charAt(k) == '-') {

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				while (k < numbers.length()) {

					if (!Character.isDigit(numbers.charAt(k)) && numbers.charAt(k) != '.') {

						break;

					}

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				firstNumber = Float.parseFloat(firstNumberS);
				secondNumber = Float.parseFloat(secondNumberS);

				float quotient = (firstNumber / secondNumber);

				String quotientS = String.format("%.12f", quotient);

				if (j == 0) {

					cutoff = quotientS.length();

					numbers = quotientS + numbers.substring(k);

				} else {

					String cut = numbers.substring(0, j) + quotientS;
					cutoff = cut.length();

					numbers = numbers.substring(0, j) + quotientS + numbers.substring(k);

				}

				i = cutoff - 1;
				break;

			}

		}

		for (int o = 1; o < numbers.length() - 3; o++) { // Double negatives

			if (numbers.charAt(o) == '-' && numbers.charAt(o + 1) == '-') {

				if (Character.isDigit(numbers.charAt(o - 1)) && Character.isDigit(numbers.charAt(o + 2))) {

					numbers = numbers.substring(0, o) + "+" + numbers.substring(o + 2, numbers.length());

				} else {

					numbers = numbers.substring(0, o) + numbers.substring(o + 2, numbers.length());

				}

			}

		}

		for (int i = 1; i < numbers.length(); i++) { // This time we do addition / subtraction!

			int j;
			int k;
			String firstNumberS = "";
			String secondNumberS = "";
			float firstNumber;
			float secondNumber;

			int cutoff = 0;

			switch (numbers.charAt(i)) {

			case '+':

				j = i - 1;
				k = i + 1;
				firstNumberS = "";
				secondNumberS = "";
				while (j >= 0) {

					if (numbers.charAt(j) == '-') {

						if (j > 0) {

							if (Character.isDigit(numbers.charAt(j - 1)) || Character.isLetter(numbers.charAt(j - 1))) {

							} else {

								firstNumberS = numbers.substring(j, i);
								j--;

							}

							break;

						} else if (j == 0) {

							firstNumberS = numbers.substring(j, i);
							j--;
							break;

						} else {

							break;

						}

					}

					if (!Character.isDigit(numbers.charAt(j)) && numbers.charAt(j) != '.') {

						break;

					}

					firstNumberS = numbers.substring(j, i);

					j--;

				}

				j++;

				if (numbers.charAt(k) == '-') {

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				while (k < numbers.length()) {

					if (!Character.isDigit(numbers.charAt(k)) && numbers.charAt(k) != '.') {

						break;

					}

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				firstNumber = Float.parseFloat(firstNumberS);
				secondNumber = Float.parseFloat(secondNumberS);

				float sum = (firstNumber + secondNumber);

				String sumS = String.format("%.12f", sum);

				if (j == 0) {

					cutoff = sumS.length();

					numbers = sumS + numbers.substring(k);

				} else {

					String cut = numbers.substring(0, j) + sumS;
					cutoff = cut.length();

					numbers = numbers.substring(0, j) + sumS + numbers.substring(k);

				}

				i = cutoff - 1;

				break;
			case '-':

				j = i - 1;
				k = i + 1;
				firstNumberS = "";
				secondNumberS = "";
				while (j >= 0) {

					if (numbers.charAt(j) == '-') { // Is this even possible?...

						if (j > 0) {

							if (Character.isDigit(numbers.charAt(j - 1)) || Character.isLetter(numbers.charAt(j - 1))) {

							} else {

								firstNumberS = numbers.substring(j, i);
								j--;

							}

							break;

						} else if (j == 0) {

							firstNumberS = numbers.substring(j, i);
							j--;
							break;

						} else {

							break;

						}

					}

					if (!Character.isDigit(numbers.charAt(j)) && numbers.charAt(j) != '.') {

						break;

					}

					firstNumberS = numbers.substring(j, i);

					j--;

				}

				j++;

				if (numbers.charAt(k) == '-') {

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				while (k < numbers.length()) {

					if (!Character.isDigit(numbers.charAt(k)) && numbers.charAt(k) != '.') {

						break;

					}

					secondNumberS = numbers.substring(i + 1, k + 1);

					k++;

				}

				firstNumber = Float.parseFloat(firstNumberS);
				secondNumber = Float.parseFloat(secondNumberS);

				float difference = (firstNumber - secondNumber);

				String differenceS = String.format("%.12f", difference);

				if (j == 0) {

					cutoff = differenceS.length();

					numbers = differenceS + numbers.substring(k);

				} else {

					String cut = numbers.substring(0, j) + differenceS;
					cutoff = cut.length();

					numbers = numbers.substring(0, j) + differenceS + numbers.substring(k);

				}

				i = cutoff - 1;
				break;

			}

		}

		for (int n = 0; n < numbers.length() - 1; n++) {

			while (numbers.charAt(n) == '-' && numbers.charAt(n + 1) == '-') {

				numbers = numbers.substring(0, n) + numbers.substring(n + 2, numbers.length());

			}

		}

		output = Float.parseFloat(numbers);

		return output;
	}
}
