package co.da.tenacity

/**
 * This file contains classes that encapsulate a WaitConfiguration object, an error predicate lambda with a signature
 * of (Throwable) -> Boolean, and a lambda. Each class has one public method that has the same signature as the lambda.
 * It calls the lambda tenaciously according to the WaitConfiguration and the error predicate.
 */


/**
 * Class that encapsulates a specific (T) -> R lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousFunction<T, R>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T) -> R
) {

  fun apply(t: T): R = apply(waitConfig, errorPredicate, t, func)
}

/**
 * Class that encapsulates a specific (T, U) -> R lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousBiFunction<T, U, R>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T, U) -> R
) {

  fun apply(t: T, u: U): R = apply(waitConfig, errorPredicate, t, u, func)
}

/**
 * Class that encapsulates a specific (T, U, V) -> R lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousTriFunction<T, U, V, R>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T, U, V) -> R
) {

  fun apply(t: T, u: U, v: V): R = apply(waitConfig, errorPredicate, t, u, v, func)
}

/**
 * Class that encapsulates a specific (T, U, V) -> R lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousQuadFunction<T, U, V, W, R>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T, U, V, W) -> R
) {

  fun apply(t: T, u: U, v: V, w: W): R = apply(waitConfig, errorPredicate, t, u, v, w, func)
}

/**
 * Class that encapsulates a specific (T) -> T lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousUnaryOperator<T>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T) -> T
) {

  fun apply(t: T): T = applyUnary(waitConfig, errorPredicate, t, func)
}

/**
 * Class that encapsulates a specific (T, T) -> T lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousBinaryOperator<T>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T, T) -> T
) {

  fun apply(t1: T, t2: T): T = applyBinary(waitConfig, errorPredicate, t1, t2, func)
}

/**
 * Class that encapsulates a specific () -> T lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousSupplier<T>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: () -> T
) {

  fun get(): T = get(waitConfig, errorPredicate, func)
}

/**
 * Class that encapsulates a specific (T) -> Unit lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousConsumer<T>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T) -> Unit
) {

  fun accept(t: T) = accept(waitConfig, errorPredicate, t, func)
}

/**
 * Class that encapsulates a specific (T, U) -> Unit lambda, a WaitConfiguration and an errorPredicate. It calls the lambda
 * tenaciously, exposing one method with the same signature as the lambda.
 */
class TenaciousBiConsumer<T, U>(
  val waitConfig: WaitConfiguration,
  val errorPredicate: (Throwable) -> Boolean,
  val func: (T, U) -> Unit
) {

  fun accept(t: T, u: U) = accept(waitConfig, errorPredicate, t, u, func)
}
