package io.github.edadma.squiggly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PipeTests extends AnyFreeSpec with Matchers with Testing {

  "pipe 1" in {
    test(null, """{{ [3, 4, 5] | take 2 }}""") shouldBe """[3, 4]"""
  }

  "pipe 2" in {
    test(null, """{{ 'asdf' | take 2 }}""") shouldBe """as"""
  }

}
