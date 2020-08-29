package co.da.tenacity

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.random.Random

val logger = KotlinLogging.logger {}

class NumMatcher {
  fun match(guess: Int): Boolean {
    val nbr = Random.nextInt(10)
    if (guess != nbr) {
      throw IllegalStateException("$guess does not match $nbr.")
    }
    return true
  }

  fun matchOrLess(guess: Int): Boolean? {
    val nbr = Random.nextInt(10)
    if (guess > nbr) {
      throw IllegalStateException("$guess is greater than $nbr.")
    }

    return if (guess == nbr) true else null

  }

  fun range(min: Int, max: Int): Boolean {
    val nbr = Random.nextInt(10)
    if (nbr < min || nbr > max) {
      throw IllegalStateException("$nbr is not between $min and $max.")
    }
    return true
  }

  fun rangeOrMore(min: Int, max: Int): Boolean? {
    val nbr = Random.nextInt(10)
    if (nbr < max) throw IllegalStateException("$nbr is not between $min and $max.")
    if (nbr > min) return null
    return true
  }

  fun sometimesWorks(fail: Int) {
    sometimesWorks(fail, 10)
  }

  fun sometimesWorks(fail: Int, limit: Int) {
    val nbr = Random.nextInt(limit)
    if (nbr <= fail) throw java.lang.IllegalStateException("No dice")
  }
}

class SumMatcher(val until: Int) {
  fun match(first: Int, second: Int, third: Int): Boolean {
    val total = first + second + third
    val answer = Random.nextInt(until)
    if (total != answer) {
      throw java.lang.IllegalStateException("$total does not match $answer.")
    }
    return true
  }

  fun matchOrLess(first: Int, second: Int, third: Int): Boolean? {
    val total = first + second + third
    val answer = Random.nextInt(until)
    if (total < answer) {
      throw java.lang.IllegalStateException("$total is less than $answer.")
    }

    return if (total == answer) true else null
  }

  fun sum(first: Int, second: Int): Int {
    val total = first + second
    val answer = Random.nextInt(until)
    if (total != answer) {
      throw java.lang.IllegalStateException("$total does not match $answer.")
    }
    return answer
  }

  fun sumOrLess(first: Int?, second: Int?): Int? {
    logger.info { "first: $first" }
    logger.info { "second: $second" }
    val total = (first ?: 0) + (second ?: 0)
    logger.info { "total: $total" }
    val answer = Random.nextInt(until)
    logger.info { "answer: $answer" }
    if (total < answer) {
      throw IllegalStateException("$total does not match $answer.")
    }
    return if (total == answer) answer else null
  }

}

class PriceIsRight(val max: Double) {

  fun checkPrice(guess: Double): Double {
    val rightPrice = Random.nextDouble(max)
    if (guess > rightPrice) {
      throw IllegalStateException("guess ($guess) must be equal to or less than the actual price ($rightPrice)")
    }

    logger.info { "guess: $guess" }
    logger.info { "rightPrice: $rightPrice" }
    return rightPrice - guess
  }

  fun checkPriceNullIfMatch(guess: Double?): Double? {
    val rightPrice = Random.nextDouble(max)
    if (guess!! > rightPrice) {
      throw IllegalStateException("guess ($guess) must be equal to or less than the actual price ($rightPrice)")
    }

    logger.info { "guess: $guess" }
    logger.info { "rightPrice: $rightPrice" }
    val diff = rightPrice - guess!!
    return if (diff > 0) diff else null
  }
}

class SometimesWorks(private val modulus: Int) {


  fun getDateTime(): LocalDateTime {
    val localDateTime = LocalDateTime.now()
    val zoneId = ZoneId.systemDefault()
    val epoch = localDateTime.atZone(zoneId).toEpochSecond()
    if (epoch % modulus == 0L) {
      return localDateTime
    }
    throw IllegalStateException("$modulus does not divide evenly in $epoch")
  }

  fun getDateTimeOrNull(): LocalDateTime? {
    val localDateTime = LocalDateTime.now()
    val zoneId = ZoneId.systemDefault()
    val epoch = localDateTime.atZone(zoneId).toEpochSecond()
    if (epoch % modulus == 0L) {
      return localDateTime
    }
    if (epoch % modulus == 1L) {
      return null
    }
    throw java.lang.IllegalStateException("$modulus does not divide evenly in $epoch")
  }
}

internal class TenacityKtTest {

  private val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 40, 20)
  private val errorPredicate: (Throwable) -> Boolean = { it is IllegalStateException }

  @Test
  fun `apply should succeed`() {
    val status = apply(waitConfig, errorPredicate, Random.nextInt(10)) {
      val nbr = Random.nextInt(10)
      if (nbr != it) throw IllegalStateException("$it does not match $nbr")
      true
    }

    assertTrue(status)
  }

  @Test
  fun `apply should retry once and fail`() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 1)
    val errorPredicate: (Throwable) -> Boolean = { it is IllegalStateException }
    val numMatcher = NumMatcher()
    assertThrows<UnrecoverableException> {
      apply(
        waitConfig,
        errorPredicate,
        Random.nextInt(11, 20),
        numMatcher::match
      )
    }
  }

  @Test
  fun `apply should fail`() {
    val errorPredicate: (Throwable) -> Boolean = { it is IllegalArgumentException }
    val numMatcher = NumMatcher()
    val random = Random(100)
    assertThrows<UnrecoverableException> {
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(11, 20),
        numMatcher::match
      )
    }
  }

  @Test
  fun `apply should return true or null`() {
    val numMatcher = NumMatcher()
    val random = Random(409)
    val status =
      apply(waitConfig, errorPredicate, random.nextInt(10), numMatcher::matchOrLess)

    logger.info("status: $status")
    assertTrue(status == true || status == null)
  }

  @Test
  fun `apply nullable return should fail`() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 5)
    val numMatcher = NumMatcher()
    val random = Random(409)
    assertThrows<UnrecoverableException> {
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(11, 20),
        numMatcher::matchOrLess
      )
    }
  }

  @Test
  fun `apply with two args should succeed`() {
    val numMatcher = NumMatcher()
    val random = Random(100)
    val status = apply(
      waitConfig,
      errorPredicate,
      random.nextInt(1, 5),
      random.nextInt(6, 10),
      numMatcher::range
    )

    assertTrue(status)
  }

  @Test
  fun `apply with two args should fail`() {
    val errorPredicate: (Throwable) -> Boolean = { it is IllegalArgumentException }
    val numMatcher = NumMatcher()
    val random = Random(100)
    assertThrows<UnrecoverableException> {
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(11, 15),
        random.nextInt(16, 20),
        numMatcher::range
      )
    }
  }

  @Test
  fun `apply with two args and nullable return should succeed`() {
    val numMatcher = NumMatcher()
    val random = Random(100)
    val status =
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(1, 5),
        random.nextInt(6, 10),
        numMatcher::rangeOrMore
      )

    assertTrue(status == true || status == null)
  }

  @Test
  fun `apply with two args and nullable return should fail`() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 5)
    val numMatcher = NumMatcher()
    val random = Random(100)
    assertThrows<UnrecoverableException> {
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(11, 15),
        random.nextInt(16, 20),
        numMatcher::rangeOrMore
      )
    }
  }

  @Test
  fun `apply with three args should succeed`() {
    val sumMatcher = SumMatcher(12)
    val random = Random(100)
    val status =
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(4),
        random.nextInt(4),
        random.nextInt(4),
        sumMatcher::match
      )

    assertTrue(status)
  }

  @Test
  fun `apply with three args should fail`() {
    val errorPredicate: (Throwable) -> Boolean = { it is IllegalArgumentException }
    val sumMatcher = SumMatcher(12)
    val random = Random(100)
    assertThrows<UnrecoverableException> {
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(4),
        random.nextInt(4),
        random.nextInt(4)
      ) { t, u, v -> throw IllegalAccessException("it was never gonna work") }
    }
  }

  @Test
  fun `apply with three args and nullable return should succeed`() {
    val sumMatcher = SumMatcher(12)
    val random = Random(100)
    val status =
      apply(
        waitConfig,
        errorPredicate,
        random.nextInt(4),
        random.nextInt(4),
        random.nextInt(4),
        sumMatcher::matchOrLess
      )

    assertTrue(status == true || status == null)
  }

  @Test
  fun `apply with four args should succeed`() {
    val moe = "Moe"
    val larry = "Larry"
    val curly = "Curly"
    val count = apply(waitConfig, errorPredicate, 4, moe, larry, curly) { t, u, v, w ->
      if (t <= Random.nextInt(10)) throw IllegalStateException("Remind me to kill you later")
      u.length + v.length + w.length
    }

    assertEquals(moe.length + larry.length + curly.length, count)
  }

  @Test
  fun `applyUnary should succeed`() {
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    applyUnary(
      waitConfig,
      errorPredicate,
      random.nextDouble(max),
      priceIsRight::checkPrice
    )
  }

  @Test
  fun `applyUnary should fail`() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 5)
    val max = 10.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    assertThrows<UnrecoverableException> {
      applyUnary(
        waitConfig,
        errorPredicate,
        random.nextDouble(max + 1, max + 10),
        priceIsRight::checkPrice
      )
    }
  }

  @Test
  fun `applyUnary with nullable return should succeed`() {
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    applyUnary(
      waitConfig,
      errorPredicate,
      random.nextDouble(max),
      priceIsRight::checkPriceNullIfMatch
    )
  }

  @Test
  fun `applyUnary with nullable return should fail`() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 1)
    val max = 90.5
    val priceIsRight = PriceIsRight(max)
    val random = Random(42)
    assertThrows<UnrecoverableException> {
      applyUnary(
        waitConfig,
        errorPredicate,
        random.nextDouble(max + 1, max + 10),
        priceIsRight::checkPriceNullIfMatch
      )
    }
  }

  @Test
  fun `applyBinary should succeed`() {
    val sumMatcher = SumMatcher(32)
    val random = Random(5359262)
    val first = random.nextInt(16)
    val second = random.nextInt(10)
    val answer =
      applyBinary(waitConfig, errorPredicate, first, second, sumMatcher::sum)

    assertEquals(first + second, answer)
  }

  @Test
  fun `applyBinary should fail`() {
    val waitConfig = WaitConfiguration(TimeUnit.MILLISECONDS, 10, 1)
    val sumMatcher = SumMatcher(32)
    val random = Random(5359262)
    val first = random.nextInt(33, 50)
    val second = random.nextInt(40, 50)
    assertThrows<UnrecoverableException> { applyBinary(waitConfig, errorPredicate, first, second, sumMatcher::sum) }
  }

  @Test
  fun `applyBinary nullable should succeed`() {
    val sumMatcher = SumMatcher(32)
    val random = Random(5359262)
    val first = random.nextInt(16)
    val second = random.nextInt(10)
    val answer =
      applyBinary(waitConfig, errorPredicate, first, second, sumMatcher::sumOrLess)

    assertTrue(first + second == answer || answer == null)
  }

  @Test
  fun `get should succeed`() {
    val sometimesWorks = SometimesWorks(3)
    val localDateTime = get(waitConfig, errorPredicate, sometimesWorks::getDateTime)
    assertTrue(localDateTime != null)
  }

  @Test
  fun `get should fail`() {
    val errorPredicate: (Throwable) -> Boolean = { it is IllegalArgumentException }
    val sometimesWorks = SometimesWorks(5555555)
    assertThrows<UnrecoverableException> { get(waitConfig, errorPredicate, sometimesWorks::getDateTime) }
  }

  @Test
  fun `get with nullable return should succeed`() {
    val sometimesWorks = SometimesWorks(3)
    val localDateTime =
      get(waitConfig, errorPredicate, sometimesWorks::getDateTimeOrNull)
    assertTrue(localDateTime is LocalDateTime || localDateTime == null)
  }

  @Test
  fun `accept should succeed`() {
    val numMatcher = NumMatcher()
    accept(waitConfig, errorPredicate, 5, numMatcher::sometimesWorks)
  }

  @Test
  fun `accept with two args should succeed`() {
    val numMatcher = NumMatcher()
    accept(waitConfig, errorPredicate, 5, 10, numMatcher::sometimesWorks)
  }
}