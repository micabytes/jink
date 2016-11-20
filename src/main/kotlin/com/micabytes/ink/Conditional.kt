package com.micabytes.ink


import java.math.BigDecimal
import java.util.ArrayList
import java.util.Random

internal class Conditional @Throws(InkParseException::class)
constructor(lineNumber: Int,
            content: String,
            parent: Container?) : Container(lineNumber, content, parent) {
  /*
  private var selection: Int = 0

  private class ConditionalOptions internal constructor(internal val condition: String) : Content() {
    internal val lines: MutableList<Content> = ArrayList()

    init {
      type = ContentType.CONDITIONAL_OPTION
    }
  }

  init {
    lineNumber = l
    type = ContentType.CONDITIONAL
    children = ArrayList<Content>()
    var str = line.substring(1).trim({ it <= ' ' })
    if (!str.isEmpty()) {
      if (!str.endsWith(":"))
        throw InkParseException("Error in conditional block; condition not ended by \':\'. Line number: $lineNumber")
      if (str.startsWith(CONDITIONAL_DASH))
        str = str.substring(1).trim({ it <= ' ' })
      val condition = str.substring(0, str.length - 1).trim({ it <= ' ' })
      verifySequenceCondition(condition)
      if (type == ContentType.CONDITIONAL)
        children.add(ConditionalOptions(condition))
    }
    parent = current
    parent!!.add(this)
  }

  @Throws(InkParseException::class)
  fun parseLine(l: Int, line: String) {
    val str = if (line.endsWith(InkParser.CONDITIONAL_END))
      line.substring(0, line.indexOf(InkParser.CONDITIONAL_END))
    else
      line
    if (type == ContentType.CONDITIONAL) {
      if (str.startsWith(CONDITIONAL_DASH) && !str.startsWith(Symbol.DIVERT)) {
        if (!str.endsWith(CONDITIONAL_COLON))
          throw InkParseException("Error in conditional block; condition not ended by \':\'. LineNumber: $l")
        val condition = str.substring(1, str.length - 1).trim({ it <= ' ' })
        children.add(ConditionalOptions(condition))
      } else {
        InkParser.parseLine(l, str, this)
      }
    } else {
      if (str.startsWith(CONDITIONAL_DASH) && !str.startsWith(Symbol.DIVERT)) {
        val first = str.substring(1).trim({ it <= ' ' })
        children.add(ConditionalOptions(""))
        InkParser.parseLine(l, first, this)
      } else {
        InkParser.parseLine(l, str, this)
      }
    }
  }

  private fun verifySequenceCondition(str: String) {
    if (STOPPING.equals(str, ignoreCase = true))
      type = ContentType.SEQUENCE_STOP
    if (SHUFFLE.equals(str, ignoreCase = true))
      type = ContentType.SEQUENCE_SHUFFLE
    if (CYCLE.equals(str, ignoreCase = true))
      type = ContentType.SEQUENCE_CYCLE
    if (ONCE.equals(str, ignoreCase = true))
      type = ContentType.SEQUENCE_ONCE
  }

  override val size: Int
    get() {
      if (selection >= children.size)
        return 1
      val opt = children[selection] as ConditionalOptions
      return opt.lines.size
    }

  override fun get(i: Int): Content {
    if (selection >= children.size)
      return Content(lineNumber, "", this)
    val opt = children[selection] as ConditionalOptions
    return opt.lines[i]
  }

  override fun indexOf(c: Content): Int {
    if (selection >= children.size)
      return 0
    val opt = children[selection] as ConditionalOptions
    return opt.lines.indexOf(c)
  }

  @SuppressWarnings("RefusedBequest")
  override fun add(item: Content) {
    val cond = children[children.size - 1] as ConditionalOptions
    cond.lines.add(item)
  }

  @Throws(InkRunTimeException::class)
  override fun initialize(story: Story, c: Content) {
    evaluate(story)
    super.initialize(story, c)
  }

  @SuppressWarnings("OverlyComplexMethod")
  @Throws(InkRunTimeException::class)
  private fun evaluate(story: Story) {
    when (type) {
      ContentType.CONDITIONAL -> {
        for (c in children) {
          val opt = c as ConditionalOptions
          if (children.indexOf(c) == children.size - 1 && ELSE == opt.condition) {
            selection = children.indexOf(c)
            return
          } else {
            val eval = Variable.evaluate(opt.condition, story)
            if (eval is Boolean) {
              if (eval) {
                selection = children.indexOf(c)
                return
              }
            } else {
              val `val` = eval as BigDecimal
              if (`val`.toInt() > 0) {
                selection = children.indexOf(c)
                return
              }
            }
          }
        }
        // Failed
        selection = children.size
      }
      ContentType.SEQUENCE_CYCLE -> selection = count % children.size
      ContentType.SEQUENCE_ONCE -> selection = count
      ContentType.SEQUENCE_SHUFFLE -> selection = Random().nextInt(children.size)
      ContentType.SEQUENCE_STOP -> selection = if (count >= children.size) children.size - 1 else count
      else -> story.logException(InkRunTimeException("Invalid conditional type."))
    }
  }

  companion object {
    private val STOPPING = "stopping"
    private val SHUFFLE = "shuffle"
    private val CYCLE = "cycle"
    private val ONCE = "once"
    private val CONDITIONAL_DASH = "-"
    private val CONDITIONAL_COLON = ":"
    private val ELSE = "else"

    fun isConditionalHeader(str: String): Boolean {
      return str.startsWith(StoryText.CBRACE_LEFT) && !str.contains(StoryText.CBRACE_RIGHT)
    }
  }
*/

}
