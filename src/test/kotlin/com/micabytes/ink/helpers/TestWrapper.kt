package com.micabytes.ink.helpers

import com.micabytes.ink.StoryInterrupt
import com.micabytes.ink.StoryWrapper
import org.apache.commons.io.IOUtils
import java.io.InputStream

class TestWrapper : StoryWrapper {

  override fun getStoryObject(objId: String): Any {
    throw UnsupportedOperationException("not implemented")
  }

  override fun getInterrupt(s: String): StoryInterrupt {
    throw UnsupportedOperationException("not implemented")
  }

  override fun resolveTag(t: String) {
    throw UnsupportedOperationException("not implemented")
  }

  override fun logDebug(m: String) {
    // NOOP
  }

  override fun logError(m: String) {
    // NOOP
  }

  override fun logException(e: Exception) {
    // NOOP
  }

  override fun getStream(fileId: String): InputStream {
    if (fileId.equals("includeTest1", ignoreCase = true)) {
      val include1 = """|=== includeKnot ===
                        |This is an included knot.
                        """.trimMargin()
      return IOUtils.toInputStream(include1, "UTF-8")
    }
    throw UnsupportedOperationException("Included fileId $fileId not mocked")
  }

}