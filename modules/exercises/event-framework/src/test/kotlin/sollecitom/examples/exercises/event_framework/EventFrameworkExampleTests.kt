package sollecitom.examples.exercises.event_framework

import assertk.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.examples.exercises.event_framework.Sorting.Algorithm
import sollecitom.examples.exercises.event_framework.Sorting.Direction.ASCENDING
import sollecitom.examples.exercises.event_framework.Sorting.Direction.DESCENDING
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.test.utils.assertions.containsSameElementsAs
import sollecitom.libs.swissknife.test.utils.execution.utils.test
import java.util.*

@TestInstance(PER_CLASS)
private class EventFrameworkExampleTests : CoreDataGenerator by CoreDataGenerator.testProvider {

    @Test
    fun `sorting lists with a given algorithm in ascending order`() = test {

        val unordered = (0..100).shuffled()

        val sorted = unordered.sortedWith(algorithm = QuickSort)

        assertThat(sorted).containsSameElementsAs(unordered.sorted())
    }

    @Test
    fun `sorting lists with a given algorithm in descending order`() = test {

        val unordered = (0..100).shuffled()

        val sorted = unordered.sortedWith(direction = DESCENDING)

        assertThat(sorted).containsSameElementsAs(unordered.sortedDescending())
    }
}

interface Sorting {

    enum class Direction {

        ASCENDING, DESCENDING
    }

    interface Algorithm {

        operator fun <ELEMENT : Comparable<ELEMENT>> invoke(
            list: List<ELEMENT>,
            direction: Direction
        ): List<ELEMENT>
    }
}

object QuickSort : Algorithm {

    override fun <ELEMENT : Comparable<ELEMENT>> invoke(
        list: List<ELEMENT>,
        direction: Sorting.Direction
    ): List<ELEMENT> = list.randomOrNull()?.let { pivot ->

        val (lessThan, equalTo, greaterThan) = list.partitionedAround(pivot)
        val (first, second) = when (direction) {
            ASCENDING -> lessThan to greaterThan
            DESCENDING -> greaterThan to lessThan
        }
        invoke(first, direction) + equalTo + invoke(second, direction)
    } ?: list
}

private data class ListPartitions<ELEMENT : Comparable<ELEMENT>>(val lessThan: List<ELEMENT>, val equalTo: List<ELEMENT>, val greaterThan: List<ELEMENT>)

private fun <ELEMENT : Comparable<ELEMENT>> List<ELEMENT>.partitionedAround(pivot: ELEMENT): ListPartitions<ELEMENT> {

    val lessThan = LinkedList<ELEMENT>()
    val equalTo = LinkedList<ELEMENT>()
    val greaterThan = LinkedList<ELEMENT>()

    forEach { element ->
        val target = when {
            element < pivot -> lessThan
            element == pivot -> equalTo
            else -> greaterThan
        }
        target.add(element)
    }
    return ListPartitions(lessThan, equalTo, greaterThan)
}

private fun <ELEMENT : Comparable<ELEMENT>> List<ELEMENT>.sortedWith(algorithm: Algorithm = QuickSort, direction: Sorting.Direction = ASCENDING) = algorithm(this, direction)