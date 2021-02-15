package co.da.tenacity

import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

/**
 * WaitConfiguration configures the duration of time to wait and the iterations before either increasing the time to
 * wait or giving up. timeUnit is the TimeUnit of intervals in which to wait. startInterval is the starting interval to
 * wait. endInterval is the maximum interval to wait. iterations are the number of times to wait for a set amount of
 * time before increasing the time. If infinite is true, the iterations are set to Long.MAX_VALUE when the maximum
 * interval is reached. If false, the iterations stays at its set value.
 *
 */
/**
 * WaitConfiguration defaults infinite to true in its primary constructor.
 */
data class WaitConfiguration(
  val timeUnit: TimeUnit,
  val startInterval: Long,
  val endInterval: Long,
  val iterations: Long,
  val infinite: Boolean = true,
  val logFirstStackTrace: Boolean = true,
) {
  /**
   * startInterval and endInterval are the same. Infinite is set to false.
   */
  constructor(timeUnit: TimeUnit, interval: Long, iterations: Long) : this(
    timeUnit,
    interval,
    interval,
    iterations,
    false,
      true,
  )

  /**
   * startInterval and endInterval are the same. Iteratons are set to Long.MAX_VALUE, making this infinite.
   */
  constructor(timeUnit: TimeUnit, interval: Long) : this(timeUnit, interval, interval, Long.MAX_VALUE)

  init {
    require(startInterval > 0) { "startInterval must be greater than 0." }
    require(endInterval >= startInterval) { "endInterval must be greater than or equal to startInterval." }
    require(iterations > 0) { "iterations must be greater than 0." }
  }
}