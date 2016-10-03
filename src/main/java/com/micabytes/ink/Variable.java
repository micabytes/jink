package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class Variable extends Content {
  @NonNls private static final String VAR_ = "VAR ";
  @NonNls private static final String TILDE_ = "~ ";
  @NonNls private static final String AND_WS = " and ";
  @NonNls private static final String OR_WS = " or ";
  @NonNls private static final String TRUE_LC = "true";
  @NonNls static final String TRUE_UC = "TRUE";
  @NonNls private static final String FALSE_LC = "false";
  @NonNls static final String FALSE_UC = "FALSE";
  @NonNls private static final String RETURN = Symbol.RETURN;
  @NonNls private static final String RETURNEQ = "return =";

  Variable(int l, String str, Container parent) {
    lineNumber = l;
    if (str.startsWith(VAR_)) {
      type = ContentType.VARIABLE_DECLARATION;
      text = str.substring(4).trim();
    } else if (str.startsWith(TILDE_)) {
      type = ContentType.VARIABLE_EXPRESSION;
      text = str.substring(2).trim();
      if (text.startsWith(RETURN)) {
        type = ContentType.VARIABLE_RETURN;
        text = text.replaceFirst(RETURN, RETURNEQ).trim();
      }
    }
    parent.add(this);
  }

  public static boolean isVariableHeader(String str) {
    return str.startsWith(VAR_) || str.startsWith(TILDE_);
  }

  public void evaluate(Story story) throws InkRunTimeException {
    if (type == ContentType.VARIABLE_DECLARATION)
      declareVariable(story);
    else
      calculate(story);
  }

  private static final Pattern EQ_SPLITTER = Pattern.compile("[=]+");

  @SuppressWarnings("OverlyComplexMethod")
  private void declareVariable(Story story) throws InkRunTimeException {
    String[] tokens = EQ_SPLITTER.split(text);
    if (tokens.length != 2)
      throw new InkRunTimeException("Invalid variable declaration. Expected variables, values, and operators after \'=\'.");
    String variable = tokens[0].trim();
    String value = tokens[1].trim();
    if (value.equals(TRUE_LC))
      story.putVariable(variable, Boolean.TRUE);
    else if (value.equals(FALSE_LC))
      story.putVariable(variable, Boolean.FALSE);
    else if (isInteger(value) || isFloat(value))
      story.putVariable(variable, new BigDecimal(value));
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
      if (value.contains(Symbol.DIVERT))
        throw new InkRunTimeException("Line number" + lineNumber + ": String expressions cannot contain diverts (->)");
      story.putVariable(variable, value);
    } else if (value.startsWith(Symbol.DIVERT)) {
      String address = value.substring(2).trim();
      Container directTo = story.getContainer(address);
      if (directTo != null)
        story.putVariable(variable, directTo);
      else
        throw new InkRunTimeException("DeclareVariable " + variable + " declared as equals to an invalid address " + address);
    }
    else {
      story.putVariable(variable, evaluate(value, story));
    }
  }

  private void calculate(Story story) throws InkRunTimeException {
    String[] tokens = EQ_SPLITTER.split(text);
    if (tokens.length == 1) {
      evaluate(tokens[0], story);
      return;
    }
    if (tokens.length > 2)
      throw new InkRunTimeException("Invalid variable expression. Expected variables, values, and operators after \'=\' in line " + lineNumber);
    String variable = tokens[0].trim();
    String value = tokens[1].trim();
    if (!story.hasVariable(variable))
      throw new InkRunTimeException("CalculateVariable " + variable + " is not defined in variable expression on line " + lineNumber);
    if (value.equals(TRUE_LC))
      story.putVariable(variable, Boolean.TRUE);
    else if (value.equals(FALSE_LC))
      story.putVariable(variable, Boolean.FALSE);
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
      story.putVariable(variable, value);
    } else if (value.startsWith(Symbol.DIVERT)) {
      String address = value.substring(3).trim();
      Container directTo = story.getContainer(address);
      if (directTo != null)
        story.putVariable(variable, directTo);
      else
        throw new InkRunTimeException("Variable " + variable + " declared to equals invalid address " + address);
    } else {
      story.putVariable(variable, evaluate(value, story));
    }
  }

  public static Object evaluate(String str, VariableMap variables) throws InkRunTimeException {
    if (str == null)
      return Boolean.TRUE;
    // TODO: Note that this means that spacing will mess up expressions; needs to be fixed
    String ev = null;
    try {
      ev = str.replaceAll(AND_WS, " && ").replaceAll(OR_WS, " || ").replaceAll(TRUE_LC, TRUE_UC).replaceAll(FALSE_LC, FALSE_UC);
      Expression ex = new Expression(ev);
      return ex.eval(variables);
    }
    catch (Expression.ExpressionException e) {
      throw new InkRunTimeException("Error evaluation expression " + ev,  e);
    }
  }

  private static boolean isInteger(String str) {
    // Slow and dirty solution
    try {
      //noinspection ResultOfMethodCallIgnored
      Integer.parseInt(str);
    } catch (NumberFormatException ignored) {
      return false;
    }
    return true;
  }

  private static boolean isFloat(String str) {
    // Slow and dirty solution
    try {
      //noinspection ResultOfMethodCallIgnored
      Float.parseFloat(str);
    } catch (NumberFormatException ignored) {
      return false;
    }
    return true;
  }

}
