package co.da.tenacity

import mu.KotlinLogging

val logger = KotlinLogging.logger {}

val TERMINAL_PREDICATE: (Throwable) -> Boolean = { false }

/**
 * Exception thrown within tenacious functions to indicate that the limit of iterations is reached. When thrown, the
 * tenacious function will advance to the next iteration if there is one or try one last time and on failure, throw an
 * UnrecoverableException to indicate that it will not try again.
 */
private class RecoverableException : RuntimeException()

/**
 * Exception that indicates an exception occurred that is not recoverable. Tenacious calls will stop retrying when they
 * encounter an UnrecoverableException.
 */
class UnrecoverableException(cause: Throwable) : RuntimeException(cause)

/*
 * Logs when a recoverable exception is caught and sleeps for the time configured in the WaitConfiguration.
 */
private fun logAndWait(i: Long, cfg: WaitConfiguration, e: Exception) {
  if (cfg.logFirstStackTrace && i == 1L) {
    logger.info(e) {
      "A recoverable exception occurred. $e${System.lineSeparator()}" +
          "\tRetrying in ${cfg.startInterval} ${cfg.timeUnit}."
    }
  } else {
    logger.info {
      "A recoverable exception occurred. $e${System.lineSeparator()}" +
          "\tRetrying in ${cfg.startInterval} ${cfg.timeUnit}."
    }
  }
  cfg.timeUnit.sleep(cfg.startInterval)
}

/*
 * If error predicate returns true, exception is recoverable and it logs this and waits. Otherwise, it throws
 * an UnrecoverableException to indicate it will not try any more.
 */
private fun handleException(i: Long, cfg: WaitConfiguration, errPredicate: (Throwable) -> Boolean, e: Exception) {
  if (errPredicate(e)) {
    logAndWait(i, cfg, e)
  } else {
    throw UnrecoverableException(e)
  }
}

/*
 * Advances the wait configuration by multiplying the startInterval by 2. If that is greater than endInterval, it
 * sets startInterval to endInterval. If infinite is true, it sets iterations to Long.MAX_VALUE so that the next
 * set of retries will be practically infinite as long as it returns a recoverable exception.
 */
private fun advanceWaitConfig(cfg: WaitConfiguration): WaitConfiguration {
  var iterations = cfg.iterations
  var startInterval = cfg.startInterval * 2
  if (startInterval >= cfg.endInterval) {
    startInterval = cfg.endInterval
    if (cfg.infinite) {
      iterations = Long.MAX_VALUE
    }
  }
  return WaitConfiguration(cfg.timeUnit, startInterval, cfg.endInterval, iterations, cfg.infinite, false)
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call
 * @param t the T object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @return an R object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T, R> apply(waitConfig: WaitConfiguration, errorPredicate: (Throwable) -> Boolean, t: T, func: (T) -> R): R {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T, R> apply(t: T, func: (T) -> R): R {

    for (i in 1..cfg.iterations) {
      try {
        return func(t)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(t, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param t the T object to pass to the function
 * @param u the U object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @return an R object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T, U, R> apply(
  waitConfig: WaitConfiguration,
  errorPredicate: (Throwable) -> Boolean,
  t: T,
  u: U,
  func: (T, U) -> R
): R {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T, U, R> apply(t: T, u: U, func: (T, U) -> R): R {

    for (i in 1..cfg.iterations) {
      try {
        return func(t, u)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(t, u, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param t the T object to pass to the function
 * @param u the U object to pass to the function
 * @param v the V object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @return an R object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T, U, V, R> apply(
  waitConfig: WaitConfiguration,
  errorPredicate: (Throwable) -> Boolean,
  t: T,
  u: U,
  v: V,
  func: (T, U, V) -> R
): R {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T, U, V, R> apply(t: T, u: U, v: V, func: (T, U, V) -> R): R {

    for (i in 1..cfg.iterations) {
      try {
        return func(t, u, v)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(t, u, v, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @param t the T object to pass to the function
 * @param u the U object to pass to the function
 * @param v the V object to pass to the function
 * @param w the W object to pass to the function
 * @param func the function to call.
 * @return an R object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T, U, V, W, R> apply(
  waitConfig: WaitConfiguration,
  errorPredicate: (Throwable) -> Boolean,
  t: T,
  u: U,
  v: V,
  w: W,
  func: (T, U, V, W) -> R
): R {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T, U, V, W, R> apply(t: T, u: U, v: V, w: W, func: (T, U, V, W) -> R): R {

    for (i in 1..cfg.iterations) {
      try {
        return func(t, u, v, w)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(t, u, v, w, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param t the T object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @return an T object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T> applyUnary(waitConfig: WaitConfiguration, errorPredicate: (Throwable) -> Boolean, t: T, func: (T) -> T): T {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T> apply(t: T, func: (T) -> T): T {

    for (i in 1..cfg.iterations) {
      try {
        return func(t)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(t, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param t1 the first T object to pass to the function
 * @param t1 the second T object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @return an T object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T> applyBinary(
  waitConfig: WaitConfiguration,
  errorPredicate: (Throwable) -> Boolean,
  t1: T,
  t2: T,
  func: (T, T) -> T
): T {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T> apply(t1: T, t2: T, func: (T, T) -> T): T {

    for (i in 1..cfg.iterations) {
      try {
        return func(t1, t2)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(t1, t2, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @return an T object
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T> get(waitConfig: WaitConfiguration, errorPredicate: (Throwable) -> Boolean, func: () -> T): T {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T> apply(func: () -> T): T {

    for (i in 1..cfg.iterations) {
      try {
        return func()
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return apply(func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param t a T object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T> accept(waitConfig: WaitConfiguration, errorPredicate: (Throwable) -> Boolean, t: T, func: (T) -> Unit) {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T> accept(t: T, func: (T) -> Unit) {

    for (i in 1..cfg.iterations) {
      try {
        return func(t)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return accept(t, func)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}

/**
 * Calls the passed in function tenaciously according to the WaitConfiguration object.
 * @param func the function to call.
 * @param t a T object to pass to the function
 * @param u a U object to pass to the function
 * @param waitConfig the WaitConfiguration object
 * @param errorPredicate the error predicate object that determines whether to retry
 * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
 * configuration is reached.
 */
fun <T, U> accept(
  waitConfig: WaitConfiguration,
  errorPredicate: (Throwable) -> Boolean,
  t: T,
  u: U,
  func: (T, U) -> Unit
) {

  var cfg = waitConfig
  var errPredicate = errorPredicate

  fun <T> accept(func: (T, U) -> Unit, t: T, u: U) {

    for (i in 1..cfg.iterations) {
      try {
        return func(t, u)
      } catch (e: Exception) {
        handleException(i, cfg, errPredicate, e)
      }
    }
    // Iterations complete, so throw a RecoverableException to indicate that we should go to the next WaitConfiguration.
    throw RecoverableException()
  }

  while (true) {
    try {
      return accept(func, t, u)
    } catch (e: RecoverableException) {
      if (cfg.startInterval < cfg.endInterval) {
        cfg = advanceWaitConfig(cfg)
      } else {
        logger.warn("Retrying for the last time.")
        errPredicate = TERMINAL_PREDICATE
      }
    }
  }
}
