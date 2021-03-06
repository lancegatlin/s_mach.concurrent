/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____       __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2017 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.util

import scala.concurrent._
import scala.util.{Success, Failure, Try}
import s_mach.concurrent._

/**
 * A trait for a barrier that can be used by callers to synchronize on an event
 * by waiting for the barrier to be set.
 */
trait Barrier {
  /** @return TRUE if the barrier is set */
  def isSet : Boolean

  /** @return register a callback that is triggered after the barrier is set. If
    *         the barrier is already set then f is executed immediately */
  def onSet[A](f: => A)(implicit ec: ExecutionContext): Future[A]

  /** @return a Future that completes once the barrier is set */
  def future : Future[Unit]

  /** @return a Future of next that only evaluates after the barrier is set */
  def happensBefore[A](
    next: => Future[A]
  )(implicit ec:ExecutionContext) : Future[A]

  /** Loop until set */
  def spinUntilSet() : Unit
}

object Barrier {
  /** A barrier that has already been set */
  val set : Barrier = new Barrier {
    override def isSet = true
    override def onSet[A](
      f: => A
    )(implicit ec: ExecutionContext) = {
      Try(f) match {
        case Success(a) => Future.successful(a)
        case Failure(t) => Future.failed(t)
      }
      // Can't use this for 2.10 - it wasn't added until 2.11
//      Future.fromTry(Try(f))
    }
    override def future = Future.unit
    override def happensBefore[A](
      next: => Future[A]
    )(implicit ec: ExecutionContext) = next
    override def spinUntilSet() = { }
  }
}