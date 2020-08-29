package co.da.tenacity

import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TenaciousCallerTest {

  val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 40, 20)
  val errorPredicate: (Throwable) -> Boolean = { t: Throwable -> t is IllegalStateException }

  @Test
  fun apply() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(100)
    val status = tenaciousCaller.apply(random.nextInt(10), numMatcher::match)

    assertTrue(status)
  }

  @Test
  fun applyFailsRetryOnce() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 1)
    val errorPredicate: (Throwable) -> Boolean = { t: Throwable -> t is IllegalStateException }
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(100)
    assertThrows<UnrecoverableException> { tenaciousCaller.apply(random.nextInt(11, 20), numMatcher::match) }
  }

  @Test
  fun applyNullable() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(409)
    val status = tenaciousCaller.apply(random.nextInt(10), numMatcher::matchOrLess)

    assertTrue(status == true || status == null)
  }

  @Test
  fun applyNullableFails() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 5)
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(409)
    assertThrows<UnrecoverableException> { tenaciousCaller.apply(random.nextInt(11, 20), numMatcher::matchOrLess) }
  }

  @Test
  fun applyTwoArgs() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(100)
    val status = tenaciousCaller.apply(random.nextInt(1, 5), random.nextInt(6, 10), numMatcher::range)

    assertTrue(status)
  }

  @Test
  fun applyTwoArgsFails() {
    val errorPredicate: (Throwable) -> Boolean = { t: Throwable -> t is IllegalArgumentException }
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(100)
    assertThrows<UnrecoverableException> {
      tenaciousCaller.apply(
        random.nextInt(11, 15),
        random.nextInt(16, 20),
        numMatcher::range
      )
    }
  }

  @Test
  fun applyTwoArgsNullable() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(100)
    val status = tenaciousCaller.apply(random.nextInt(1, 5), random.nextInt(6, 10), numMatcher::rangeOrMore)

    assertTrue(status == true || status == null)
  }

  @Test
  fun applyTwoArgsNullableFails() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 5)
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    val random = Random(100)
    assertThrows<UnrecoverableException> {
      tenaciousCaller.apply(
        random.nextInt(11, 15),
        random.nextInt(16, 20),
        numMatcher::rangeOrMore
      )
    }
  }

  @Test
  fun applyThreeArgs() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sumMatcher = SumMatcher(12)
    val random = Random(100)
    val status =
      tenaciousCaller.apply(random.nextInt(4), random.nextInt(4), random.nextInt(4), sumMatcher::match)

    assertTrue(status)
  }

  @Test
  fun applyThreeArgsFails() {
    val errorPredicate: (Throwable) -> Boolean = { t: Throwable -> t is IllegalArgumentException }
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    assertThrows<UnrecoverableException> {
      tenaciousCaller.apply(
        "doomed",
        "to",
        "fail"
      ) { t, u, v ->
        throw IllegalStateException("$t $u $v")
      }
    }
  }

  @Test
  fun applyThreeArgsNullable() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sumMatcher = SumMatcher(12)
    val random = Random(100)
    val status =
      tenaciousCaller.apply(
        random.nextInt(4),
        random.nextInt(4),
        random.nextInt(4),
        sumMatcher::matchOrLess
      )

    assertTrue(status == true || status == null)
  }

  @Test
  fun applyFourArgsFails() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    assertThrows<UnrecoverableException> {
      tenaciousCaller.apply("This", "will", "not", "work") { t, u, v, w ->
        throw IllegalArgumentException("$t $u $v $w")
      }
    }
  }

  @Test
  fun applyUnary() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    tenaciousCaller.applyUnary(random.nextDouble(max), priceIsRight::checkPrice)
  }

  @Test
  fun applyUnaryFails() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 5)
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val max = 10.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    assertThrows<UnrecoverableException> {
      tenaciousCaller.applyUnary(
        random.nextDouble(max + 1, max + 10),
        priceIsRight::checkPrice
      )
    }
  }

  @Test
  fun applyUnaryNullable() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    tenaciousCaller.applyUnary(random.nextDouble(max), priceIsRight::checkPriceNullIfMatch)
  }

  @Test
  fun applyUnaryNullableFails() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 1)
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    assertThrows<UnrecoverableException> {
      tenaciousCaller.applyUnary(
        random.nextDouble(max + 1, max + 10),
        priceIsRight::checkPriceNullIfMatch
      )
    }
  }

  @Test
  fun applyBinary() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sumMatcher = SumMatcher(32)
    val random = Random(5359262)
    val first = random.nextInt(16)
    val second = random.nextInt(10)
    val answer = tenaciousCaller.applyBinary(first, second, sumMatcher::sum)

    assertEquals(first + second, answer)
  }

  @Test
  fun applyBinaryFails() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 1)
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sumMatcher = SumMatcher(32)
    val random = Random(5359262)
    val first = random.nextInt(33, 50)
    val second = random.nextInt(40, 50)
    assertThrows<UnrecoverableException> { tenaciousCaller.applyBinary(first, second, sumMatcher::sum) }
  }

  @Test
  fun applyBinaryNullable() {
    logger.info { "waitConfig: $waitConfig" }
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sumMatcher = SumMatcher(32)
    val first = Random.nextInt(16)
    val second = Random.nextInt(10)
    val answer = tenaciousCaller.applyBinary(first, second, sumMatcher::sumOrLess)

    logger.info("first + second: ${first + second}")
    logger.info("answer: $answer")
    assertTrue(first + second == answer || answer == null)
  }

  @Test
  fun get() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sometimesWorks = SometimesWorks(3)
    val localDateTime = tenaciousCaller.get(sometimesWorks::getDateTime)
    logger.info { "localDateTime: $localDateTime" }
    assertTrue(localDateTime != null)
  }

  @Test
  fun getFails() {
    val errorPredicate: (Throwable) -> Boolean = { t: Throwable -> t is IllegalArgumentException }
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sometimesWorks = SometimesWorks(5555555)
    assertThrows<UnrecoverableException> { tenaciousCaller.get(sometimesWorks::getDateTime) }
  }

  @Test
  fun getNullable() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val sometimesWorks = SometimesWorks(3)
    val localDateTime = tenaciousCaller.get(sometimesWorks::getDateTimeOrNull)
    assertTrue(localDateTime is LocalDateTime || localDateTime == null)
  }

  @Test
  fun accept() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    tenaciousCaller.accept(5, numMatcher::sometimesWorks)
  }

  @Test
  fun acceptTwoArgs() {
    val tenaciousCaller = TenaciousCaller(waitConfig, errorPredicate)
    val numMatcher = NumMatcher()
    tenaciousCaller.accept(5, 10, numMatcher::sometimesWorks)
  }
}