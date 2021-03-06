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
package s_mach.concurrent.impl

import s_mach.concurrent.util.Latch

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import s_mach.concurrent._

class LatchImpl(val failMessage: String) extends Latch {
  private[this] val promise = Promise[Unit]()
  val future = promise.future

  override def set() : Unit = {
    if(promise.trySuccess(()) == false) {
      throw new IllegalStateException(failMessage)
    }
  }

  override def trySet() = promise.trySuccess(())

  override def isSet = promise.isCompleted

  override def onSet[A](f:  => A)(implicit ec: ExecutionContext) = {
    if(isSet) {
      Try(f) match {
        case Success(a) => Future.successful(a)
        case Failure(t) => Future.failed(t)
      }
      // Can't use this for 2.10 - it wasn't added until 2.11
      //Future.fromTry(Try(f))
    } else {
      val promiseA = Promise[A]()
      future.foreach { _ => promiseA.complete(Try(f)) }
      promiseA.future
    }
  }
  override def happensBefore[A](
    next: => Future[A]
  )(implicit ec:ExecutionContext) = future happensBefore next

  override def spinUntilSet() = while(isSet == false) { }
}
