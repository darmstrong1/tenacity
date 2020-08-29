package co.da.tenacity

/**
 * Class that calls functions tenaciously. Encapsulates WaitConfiguration object and errorPredicate.
 */
class TenaciousCaller(val waitConfig: WaitConfiguration, val errorPredicate: (Throwable) -> Boolean) {

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param func the function to call
   * @param t the T object to pass to the function
   * @return an R object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T, R> apply(t: T, func: (T) -> R): R =
    apply(waitConfig, errorPredicate, t, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param func the function to call.
   * @param t the T object to pass to the function
   * @param u the U object to pass to the function
   * @return an R object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T, U, R> apply(t: T, u: U, func: (T, U) -> R): R =
    apply(waitConfig, errorPredicate, t, u, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param t the T object to pass to the function
   * @param u the U object to pass to the function
   * @param v the V object to pass to the function
   * @param w the W object to pass to the function
   * @param func the function to call.
   * @return an R object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T, U, V, W, R> apply(t: T, u: U, v: V, w: W, func: (T, U, V, W) -> R): R =
    apply(waitConfig, errorPredicate, t, u, v, w, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param t the T object to pass to the function
   * @param u the U object to pass to the function
   * @param v the V object to pass to the function
   * @param func the function to call.
   * @return an R object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T, U, V, R> apply(t: T, u: U, v: V, func: (T, U, V) -> R): R =
    apply(waitConfig, errorPredicate, t, u, v, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param func the function to call.
   * @param t the T object to pass to the function
   * @return an T object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T> applyUnary(t: T, func: (T) -> T): T =
    applyUnary(waitConfig, errorPredicate, t, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param func the function to call.
   * @param t1 the first T object to pass to the function
   * @param t1 the second T object to pass to the function
   * @return an T object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T> applyBinary(t1: T, t2: T, func: (T, T) -> T): T =
    applyBinary(waitConfig, errorPredicate, t1, t2, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param func the function to call.
   * @return an T object
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T> get(func: () -> T): T = get(waitConfig, errorPredicate, func)

  /**
   * Calls the passed in function tenaciously according to the WaitConfiguration object.
   * @param func the function to call.
   * @param t a T object to pass to the function
   * @param waitConfig the WaitConfiguration object
   * @param errorPredicate the error predicate object that determines whether to retry
   * @throws UnrecoverableException if an unrecoverable exception occurs or the max number of retries according to the
   * configuration is reached.
   */
  fun <T> accept(t: T, func: (T) -> Unit) = accept(waitConfig, errorPredicate, t, func)

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
  fun <T, U> accept(t: T, u: U, func: (T, U) -> Unit) = accept(waitConfig, errorPredicate, t, u, func)
}