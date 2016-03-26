
package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Story {
  @NonNls private static final String GLUE = "<>";
  @NonNls static final String DIVERT = "->";
  @NonNls private static final String DIVERT_END = "END";
  public static final char LT = '<';
  public static final char GT = '>';

  private final HashMap<String, Container> namedContainers = new HashMap<>();
  private Container currentContainer;
  private int currentCounter;
  private final ArrayList<Container> currentChoices = new ArrayList<>();
  private boolean running;

  void initialize() {
    if (currentContainer.type == ContentType.KNOT) {
      if (currentContainer.getContent(0).isStitch())
        currentContainer = (Container) currentContainer.getContent(0);
    }
    running = true;
  }

  public boolean hasNext() {
    return currentContainer != null && currentCounter < currentContainer.getContentSize();
  }

  public String next() throws InkRunTimeException {
    if (!hasNext())
      throw new InkRunTimeException("Did you forget to run canContinue()?");
    String ret = "";
    Content content = getContent();
    boolean processing = true;
    while (processing) {
      processing = false;
      ret += resolveContent(content);
      incrementContent(content);
      if (currentContainer != null) {
        Content nextContent = getContent();
        if (nextContent != null) {
          if (nextContent.text.startsWith(GLUE))
            processing = true;
          if (nextContent.isChoice() && !nextContent.isFallbackChoice())
            processing = true;
          if (nextContent.isStitch())
            processing = true;
          if (nextContent.type == ContentType.TEXT && nextContent.text.startsWith(DIVERT)) {
            Container divertTo = getDivertTarget(nextContent);
            if (divertTo != null && divertTo.getContent(0).text.startsWith(GLUE))
              processing = true;
          }
        }
        if (ret.endsWith(GLUE) && nextContent != null)
          processing = true;
        content = nextContent;
      }
    }
    return cleanUpText(ret);
  }

  private void incrementContent(Content content) throws InkRunTimeException {
    if (content.isDivert()) {
      currentContainer = getDivertTarget(content);
      if (currentContainer != null)
        currentContainer.increment();
      else
        running = false;
      currentCounter = 0;
      currentChoices.clear();
      return;
    }
    currentCounter++;
    if (currentCounter >= currentContainer.getContentSize() && currentChoices.isEmpty()) {
      if (currentContainer.isChoice() || currentContainer.isGather()) {
        Container c = currentContainer;
        Container p = c.parent;
        while (p != null) {
          int i = p.getContentIndex(c) + 1;
          while (i < p.getContentSize()) {
            Content n = p.getContent(i);
            if (n.isGather()) {
              currentContainer = (Container) n;
              currentContainer.increment();
              currentCounter = 0;
              currentChoices.clear();
              return;
            }
            if (n.isChoice() && currentContainer.isGather()) {
              currentContainer = p;
              currentCounter = i;
              currentChoices.clear();
              return;
            }
            i++;
          }
          c = p;
          p = c.parent;
        }
        currentContainer = null;
        currentCounter = 0;
        currentChoices.clear();
        return;
      }
    }
    else {
      Content next = getContent();
      if (next != null && next.isFallbackChoice() && currentChoices.isEmpty()) {
        currentContainer = (Container) next;
        currentContainer.increment();
        currentCounter = 0;
        currentChoices.clear();
        return;
      }
    }
  }

  private Content getContent() throws InkRunTimeException {
    if (!running)
      return null;
    if (currentContainer == null && running)
      throw new InkRunTimeException("Current text container is NULL.");
    if (currentCounter >= currentContainer.getContentSize())
      return null;
    return currentContainer.getContent(currentCounter);
  }

  private String resolveContent(Content content) throws InkRunTimeException {
    if (content.type == ContentType.TEXT) {
      String ret = content.isDivert() ? resolveDivert(content) : content.getText(this);
      content.increment();
      return ret;
    }
    if (content.isChoice()) {
      addChoice((Choice) content);
    }
    return "";
  }

  private String resolveDivert(Content content) throws InkRunTimeException {
    String ret = content.getText(this);
    ret = ret.substring(0, ret.indexOf(DIVERT)).trim();
    ret += GLUE;
    return ret;
  }

  private Container getDivertTarget(Content content) throws InkRunTimeException {
    String d = content.text.substring(content.text.indexOf(DIVERT) + 2).trim();
    if (d.equals(DIVERT_END))
      return null;
    Container divertTo = namedContainers.get(d);
    if (divertTo == null) {
      String fd = getFullId(d);
      divertTo = namedContainers.get(fd);
      if (divertTo == null)
        throw new InkRunTimeException("Attempt to divert to non-defined " + d + " or " + fd + " in line " + content.lineNumber);
    }
    if (divertTo.type == ContentType.KNOT && divertTo.getContent(0).isStitch())
      divertTo = (Container) divertTo.getContent(0);
    return divertTo;
  }

  private String getFullId(String id) {
    if (id.equals(DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    Container p = currentContainer.parent;
    return p != null ? p.id + InkParser.DOT + id : id;
  }

  private void addChoice(Choice choice) throws InkRunTimeException {
    // Check conditions
    if (!choice.evaluateConditions(this))
      return;
    // Resolve
    if (choice.getChoiceText(this).isEmpty()) {
      if (currentChoices.isEmpty()) {
        currentContainer = choice;
        currentCounter = 0;
      }
      // else nothing - this is a fallback choice and we ignore it
    } else {
      currentChoices.add(choice);
    }
  }

  public List<String> nextChoice() throws InkRunTimeException {
    ArrayList<String> ret = new ArrayList<>();
    while (hasNext()) {
      String text = next();
      if (!text.isEmpty())
        ret.add(text);
    }
    return ret;
  }

  public void choose(int i) throws InkRunTimeException {
    if (i < currentChoices.size()) {
      currentContainer = currentChoices.get(i);
      currentContainer.increment();
      currentCounter = 0;
      currentChoices.clear();
    } else
      throw new InkRunTimeException("Trying to select a choice that does not exist");
  }

  public int getChoiceSize() {
    return currentChoices.size();
  }

  public Choice getChoice(int i) {
    return (Choice) currentChoices.get(i);
  }


  private static String cleanUpText(@NonNls String str) {
    return str.replaceAll(GLUE, " ") // clean up glue
              .replaceAll("\\s+", " ") // clean up white space
              .trim();
  }

  public BigDecimal getValue(String s) throws InkRunTimeException {
    if (namedContainers.containsKey(s)) {
      Container container = namedContainers.get(s);
      return BigDecimal.valueOf(container.getCount());
    }
    String pathId = getValueId(s);
    if (namedContainers.containsKey(pathId)) {
      Container container = namedContainers.get(pathId);
      return BigDecimal.valueOf(container.getCount());
    }
    throw new InkRunTimeException("Could not identify the variable " + s + " or " + pathId);
  }

  private String getValueId(String id) {
    if (id.equals(DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    return currentContainer != null ? currentContainer.id + InkParser.DOT + id : id;
  }

  void add(Container container) {
    if (container.getId() != null)
      namedContainers.put(container.getId(), container);
    // Set starting knot
    if (currentContainer == null)
      currentContainer = container;
  }

  public boolean isEnded() {
    return currentContainer == null;
  }


}