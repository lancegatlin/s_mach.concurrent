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

/**
 * A trait for controlling the throttle value of some operation
 * */
trait ThrottleControl {
  /** @return the throttle period in nanoseconds */
  def throttle_ns: Long
  /**
   * Set the throttle setting
   * @return the previous throttle setting
   * */
  def throttle_ns(throttle_ns: Long) : Long
  /**
   * Modify the throttle setting
   * @return the previous throttle setting
   * */
  def adjustThrottle_ns(adjust_ns: Long) : Long
}
