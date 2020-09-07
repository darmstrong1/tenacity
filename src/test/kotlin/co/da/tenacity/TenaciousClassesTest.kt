package co.da.tenacity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

internal class TenaciousClassesTest {

  val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 40, 20)
  val errorPredicate: (Throwable) -> Boolean = { t: Throwable -> t is IllegalStateException }

  @Test
  fun apply() {
    val numMatcher = NumMatcher()
    val random = Random(100)
    val tf = TenaciousFunction(waitConfig, errorPredicate, numMatcher::match)
    val status = tf.apply(random.nextInt(10))

    assertTrue(status)
  }

  @Test
  fun applyTwoArgs() {
    val numMatcher = NumMatcher()
    val random = Random(100)
    val tbf = TenaciousBiFunction(waitConfig, errorPredicate, numMatcher::range)
    val status = tbf.apply(random.nextInt(1, 5), random.nextInt(6, 10))

    assertTrue(status)
  }

  @Test
  fun applyThreeArgs() {
    val sumMatcher = SumMatcher(12)
    val random = Random(100)
    val ttf = TenaciousTriFunction(waitConfig, errorPredicate, sumMatcher::match)
    val status = ttf.apply(random.nextInt(4), random.nextInt(4), random.nextInt(4))

    assertTrue(status)
  }

  @Test
  fun applyFourArgs() {
    val tqf = TenaciousQuadFunction(waitConfig, errorPredicate) { t: String, u: String, v: String, w: String ->
      if (Random.nextInt(10) <= 5) throw IllegalStateException("error")
      listOf(t, u, v, w)
    }
    val t = "How"
    val u = "now"
    val v = "brown"
    val w = "cow"
    val expected = listOf(t, u, v, w)
    val actual = tqf.apply(t, u, v, w)
    assertEquals(expected, actual)
  }

  @Test
  fun applyUnary() {
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    val unaryOperator = TenaciousUnaryOperator(waitConfig, errorPredicate, priceIsRight::checkPrice)
    unaryOperator.apply(random.nextDouble(max))
  }

  @Test
  fun applyBinary() {
    val sumMatcher = SumMatcher(32)
    val random = Random(5359262)
    val first = random.nextInt(16)
    val second = random.nextInt(10)
    val binaryOperator = TenaciousBinaryOperator(waitConfig, errorPredicate, sumMatcher::sum)
    val answer = binaryOperator.apply(first, second)

    assertEquals(first + second, answer)
  }

  @Test
  fun get() {
    val sometimesWorks = SometimesWorks(3)
    val supplier = TenaciousSupplier(waitConfig, errorPredicate, sometimesWorks::getDateTime)
    val localDateTime = supplier.get()
    assertNotNull(localDateTime)
  }

  @Test
  fun accept() {
    val numMatcher = NumMatcher()
    val consumer = TenaciousConsumer(waitConfig, errorPredicate, numMatcher::sometimesWorks)
    consumer.accept(5)
  }

  @Test
  fun acceptTwoArgs() {
    val numMatcher = NumMatcher()
    val consumer = TenaciousBiConsumer(waitConfig, errorPredicate, numMatcher::sometimesWorks)
    consumer.accept(5, 10)
  }
}