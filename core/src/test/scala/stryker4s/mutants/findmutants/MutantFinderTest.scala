package stryker4s.mutants.findmutants

import fs2.io.file.Path
import stryker4s.config.Config
import stryker4s.extension.FileExtensions.*
import stryker4s.extension.TreeExtensions.IsEqualExtension
import stryker4s.log.Logger
import stryker4s.scalatest.{FileUtil, LogMatchers}
import stryker4s.testutil.Stryker4sIOSuite

import java.nio.file.NoSuchFileException
import scala.meta.*
import scala.meta.parsers.ParseException

class MutantFinderTest extends Stryker4sIOSuite with LogMatchers {
  private val exampleClassFile = FileUtil.getResource("scalaFiles/ExampleClass.scala")

  describe("parseFile") {
    implicit val config: Config = Config.default
    it("should parse an existing file") {

      val sut = new MutantFinder()
      val file = exampleClassFile

      sut.parseFile(file).asserting { result =>
        val expected = """package stryker4s
                         |
                         |class ExampleClass {
                         |  def foo(num: Int) = num == 10
                         |
                         |  def createHugo = Person(22, "Hugo")
                         |}
                         |
                         |final case class Person(age: Int, name: String)
                         |""".stripMargin.parse[Source].get
        assert(result.isEqual(expected), result)
      }
    }

    it("should throw an exception on a non-parseable file") {
      val sut = new MutantFinder()
      val file = FileUtil.getResource("scalaFiles/nonParseableFile.notScala")

      sut.parseFile(file).attempt.asserting { result =>
        val expectedException = result.swap.getOrElse(fail()).asInstanceOf[ParseException]

        expectedException.shortMessage should be("illegal start of definition identifier")
      }
    }

    it("should fail on a nonexistent file") {
      val sut = new MutantFinder()
      val noFile = Path("this/does/not/exist.scala")

      sut.parseFile(noFile).assertThrows[NoSuchFileException]
    }

    it("should parse a scala-3 file") {
      import scala.meta.dialects.Scala3

      val scala3DialectConfig = config.copy(scalaDialect = Scala3)
      val sut = new MutantFinder()(scala3DialectConfig, implicitly[Logger])
      val file = FileUtil.getResource("scalaFiles/scala3File.scala")

      sut.parseFile(file).assertNoException
    }
  }

  describe("findMutants") {
    // it("should return empty list when given source has no possible mutations") {
    //   implicit val config: Config = Config.default

    //   val sut = new MutantFinder()
    //   val source = source"case class Foo(s: String)"

    //   val (result, _) = sut.findMutants(source)

    //   result should be(empty)
    // }

    // it("should contain a mutant when given source has a possible mutation") {
    //   implicit val config: Config = Config.default

    //   val sut = new MutantFinder()
    //   val source =
    //     source"""case class Bar(s: String) {
    //                 def foobar = s == "foobar"
    //               }"""

    //   val (result, _) = sut.findMutants(source)

    //   result should have length 2

    //   val firstMutant = result.head
    //   assert(firstMutant.original.isEqual(q"=="), firstMutant.original)
    //   assert(firstMutant.mutated.isEqual(q"!="), firstMutant.mutated)

    //   val secondMutant = result(1)
    //   assert(secondMutant.original.isEqual(Lit.String("foobar")), secondMutant.original)
    //   assert(secondMutant.mutated.isEqual(Lit.String("")), secondMutant.mutated)
    // }

    // it("should filter out excluded mutants") {
    //   implicit val conf: Config = Config.default.copy(excludedMutations = Set("LogicalOperator"))
    //   val sut = new MutantFinder()
    //   val source =
    //     source"""case class Bar(s: String) {
    //                 def and(a: Boolean, b: Boolean) = a && b
    //               }"""

    //   val (result, excluded) = sut.findMutants(source)
    //   excluded shouldBe 1
    //   result should have length 0
    // }

    // it("should filter out string mutants inside annotations") {
    //   implicit val config: Config = Config.default
    //   val sut = new MutantFinder()
    //   val source =
    //     source"""@Annotation("Class Annotation")
    //              final case class Bar(
    //                 @Annotation("Parameter Annotation") s: String = "s") {

    //                 @Annotation("Function Annotation")
    //                 def aFunction(@Annotation("Parameter Annotation 2") param: String = "s") = {
    //                   "aFunction"
    //                 }

    //                 @Annotation("Val Annotation") val x = { val l = "x"; l }
    //                 @Annotation("Var Annotation") var y = { val k = "y"; k }
    //               }
    //               @Annotation("Object Annotation")
    //               object Foo {
    //                 val value = "value"
    //               }
    //       """

    //   val (result, excluded) = sut.findMutants(source)
    //   excluded shouldBe 0
    //   result should have length 6
    // }

    // it("should filter out @SuppressWarnings annotated code") {
    //   implicit val config: Config = Config.default
    //   val sut = new MutantFinder()

    //   val source =
    //     source"""
    //              final case class Bar(
    //                 @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
    //                 s1: String = "filtered",
    //                 @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
    //                 notFiltered1: Boolean = false) {

    //                 def aFunction(@SuppressWarnings param: String = "notFiltered2") = {
    //                   "notFiltered3"
    //                 }

    //                 @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
    //                 val x = { val l = "s3"; l }
    //               }
    //               @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
    //               object Foo {
    //                 val value = "s5"
    //               }
    //       """

    //   val (result, excluded) = sut.findMutants(source)
    //   excluded shouldBe 3
    //   result should have length 3
    // }

    // it("should log unparsable regular expressions") {
    //   implicit val config: Config = Config.default

    //   val sut = new MutantFinder()
    //   val regex = Lit.String("[[]]")
    //   val source =
    //     source"""case class Bar() {
    //                 def foobar = new Regex($regex)
    //               }"""

    //   val result = sut.findMutants(source)

    //   // 1 empty-string found
    //   val mutant = result._1.loneElement
    //   assert(mutant.original.isEqual(regex), mutant.original)
    //   assert(mutant.mutated.isEqual(Lit.String("")), mutant.mutated)
    //   // 0 excluded
    //   result._2 shouldBe 0

    //   "[RegexMutator]: The Regex parser of weapon-regex couldn't parse this regex pattern: '[[]]'. Please report this issue at https://github.com/stryker-mutator/weapon-regex/issues. Inner error: [Error] Parser: Position 1:1, found \"[[]]\"" shouldBe loggedAsError
    // }
  }

  describe("logging") {
    it("should error log an unfound file") {
      implicit val config: Config = Config.default

      val sut = new MutantFinder()
      val noFile = FileUtil.getResource("scalaFiles/nonParseableFile.notScala")

      sut
        .parseFile(noFile)
        .assertThrows[ParseException]
        .asserting { _ =>
          s"Error while parsing file '${noFile.relativePath}', illegal start of definition identifier" should be(
            loggedAsError
          )
        }
    }
  }
}
