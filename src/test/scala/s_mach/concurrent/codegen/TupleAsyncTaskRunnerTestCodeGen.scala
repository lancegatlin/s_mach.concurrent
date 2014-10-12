package s_mach.concurrent.codegen

object TupleAsyncTaskRunnerTestCodeGen {
  val header = """/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____        __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/"""

  def genAllFile(basePath: String, maxN: Int) : Unit = {
    (2 to 22).foreach { n =>
      genToFile(s"$basePath/Tuple${n}AsyncTaskRunnerTest.scala",n)
    }
  }

  def genToFile(path: String, n: Int) : Unit = {
    val contents =
s"""
$header
package s_mach.concurrent

/* WARNING: Generated code. To modify see s_mach.concurrent.codegen.TupleAsyncTaskRunnerTestCodeGen */

import scala.util.{Random, Success, Failure}
import org.scalatest.{FlatSpec, Matchers}
import s_mach.concurrent.TestBuilder._
import util._

class Tuple${n}AsyncTaskRunnerTest extends FlatSpec with Matchers with ConcurrentTestCommon {
${genT0(n)}
${genT1(n)}
${genT2(n)}
}

"""

    import java.io._
    val out = new PrintWriter(new BufferedWriter(new FileWriter(path)))
    out.println(contents)
    out.close()
  }

  abstract class Test(n: Int) {
    val xi = (0 until n)
    val lcs = ('a' to 'z').map(_.toString).take(n)
    val ucs = ('A' to 'Z').map(_.toString).take(n)
    val allUcs = ucs.mkString(",")
    val allLcs = lcs.mkString(",")
    val allF = lcs.map(lc => s"f$lc").mkString(",")
    def gen : String
  }

  def genT0(n: Int) = new Test(n) {
    def gen = {
s"""
  "Tuple${n}AsyncTaskRunner-t0" must "wait on all Futures to complete concurrently" in {
    val results =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val items = IndexedSeq.fill($n)(Random.nextInt)
        ${xi.map(i => s"val f${lcs(i)} = success(items($i))").mkString("\n        ")}

        val result = async.par.run($allF)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should be(Success((${xi.map(i => s"items($i)").mkString(",")})))
        isConcurrentSchedule(Vector(${xi.map(i => s"items($i)").mkString(",")}), sched)
      }

    val concurrentPercent = results.count(_ == true) / results.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

 """
    }
  }.gen

  def genT1(n: Int) = new Test(n) {
    def gen = {
s"""
  "TupleAsyncTaskRunner-t1" must "complete immediately after any Future fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")
      val endLatch = Latch()

      val fb = fail(2)
      // Note1: without hooking the end latch here there would be a race condition here between success 1,3,4,5,6
      // and end. The latch is used to create a serialization schedule that can be reliably tested
      // Note2: Due to this design, a bug in merge that does not complete immediately on failure will cause a
      // deadlock here instead of a failing test
      val fa = endLatch happensBefore success(1)
      ${(2 until n).map(i => s"val f${lcs(i)} = endLatch happensBefore success(${i+1})").mkString("\n      ")}

      val result = async.par.run($allF)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")
      endLatch.set()
      waitForActiveExecutionCount(0)

      result.getTry shouldBe a [Failure[_]]
      result.getTry.failed.get shouldBe a [ConcurrentThrowable]

      sched.happensBefore("start","fail-2") should equal(true)
      sched.happensBefore("fail-2","end") should equal(true)

      (1 to $n).filter(_ != 2).foreach { i =>
        sched.happensBefore("end", s"success-${'$'}i") should equal(true)
      }
    }
  }
 """
    }
  }.gen

  def genT2(n: Int) = new Test(n) {
    def gen = {
s"""
  "TupleAsyncTaskRunner-t2" must "throw ConcurrentThrowable which can wait for all failures" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val failures = Random.shuffle(Seq(${xi.map(_ + 1).mkString(",")})).take(2)
      def call(i: Int) = if(failures.contains(i)) {
        fail(i)
      } else {
        success(i)
      }
      val result = async.par.run(${xi.map(i => s"call(${i+1})").mkString(",")})

      waitForActiveExecutionCount(0)

      val thrown = result.failed.get.asInstanceOf[ConcurrentThrowable]

      // Even though there are two worker threads, it technically is a race condition to see which failure happens
      // first. This actually happens in about 1/1000 runs where it appears worker one while processing fail-1 stalls
      // and worker 2 is able to complete success-2, success-3 and fail-4 before fail-1 finishes
      thrown.firstFailure.toString.startsWith("java.lang.RuntimeException: fail-") should equal(true)
      thrown.allFailure.get.map(_.toString) should contain theSameElementsAs(
        failures.map(failIdx => new RuntimeException(s"fail-${'$'}failIdx").toString)
      )
    }
  }
 """
    }
  }.gen
}
