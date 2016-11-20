package com.micabytes.ink

import java.math.BigDecimal
import java.util.ArrayList

internal class Choice(lineNumber: Int,
                      content: String,
                      parent: Container?) : Container(lineNumber, content, parent) {
  override val level: Int
  private var conditions: ArrayList<String> = ArrayList()
  private val repeatable = (content[0] == InkParser.CHOICE_PLUS)

  init {
    val notation = content[0]
    var lvl = 2
    var s = content.substring(1).trim({ it <= ' ' })
    while (s.get(0) == notation) {
      lvl++
      s = s.substring(1).trim({ it <= ' ' })
    }
    level = lvl
    parent?.let { add(this) }
    addLine(s)
  }

  private fun addLine(str: String) {
    var s = str.trim { it <= ' ' }
    /*
    if (s.startsWith("(")) {
      id = s.substring(s.indexOf(StoryText.BRACE_LEFT) + 1, s.indexOf(StoryText.BRACE_RIGHT)).trim({ it <= ' ' })
      val p = parent
      assert(p != null)
      id = p!!.id + InkParser.DOT + id
      s = s.substring(s.indexOf(StoryText.BRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    */
    if (s.startsWith(StoryText.CBRACE_LEFT) && conditions == null)
      conditions = ArrayList<String>()
    while (s.startsWith("{")) {
      val c = s.substring(s.indexOf(StoryText.CBRACE_LEFT) + 1, s.indexOf(StoryText.CBRACE_RIGHT)).trim({ it <= ' ' })
      conditions.add(c)
      s = s.substring(s.indexOf(StoryText.CBRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    // TODO: text = getChoiceText(s)
    val result = getResultText(s)
    if (!result.isEmpty()) {
      //noinspection ResultOfObjectAllocationIgnored
      Content(lineNumber, result, this)
    }
  }

  @Throws(InkRunTimeException::class)
  fun getChoiceText(story: Story): String {
    return StoryText.getText(content, count, story)
  }

  @Throws(InkRunTimeException::class)
  fun evaluateConditions(story: Story): Boolean {
    if (count > 0 && type == ContentType.CHOICE_ONCE)
      return false
    if (conditions == null)
      return true
    for (condition in conditions!!) {
      try {
        val obj = Variable.evaluate(condition, story) ?: return false
        if (obj is Boolean && !obj)
          return false
        if (obj is BigDecimal && obj.toInt() <= 0)
          return false
      } catch (e: InkRunTimeException) {
        story.logException(e)
        return false
      }

    }
    return true
  }

  companion object {

    fun isChoiceHeader(str: String): Boolean {
      if (str.length < 2) return false
      return str[0] == InkParser.CHOICE_DOT || str[0] == InkParser.CHOICE_PLUS
    }

    private fun getChoiceText(str: String): String {
      if (str.contains("]")) {
        return str.substring(0, str.indexOf(StoryText.SBRACE_RIGHT)).replace(StoryText.SBRACE_LEFT, "").trim({ it <= ' ' })
      }
      return str.trim { it <= ' ' }
    }

    private fun getResultText(str: String): String {
      if (str.contains("]")) {
        return str.replace("\\[.*\\]".toRegex(), "").trim({ it <= ' ' })
      }
      return str.trim { it <= ' ' }
    }
  }

}
