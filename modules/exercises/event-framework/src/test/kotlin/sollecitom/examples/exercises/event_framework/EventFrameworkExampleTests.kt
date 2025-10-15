package sollecitom.examples.exercises.event_framework

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.test.utils.execution.utils.test
import kotlin.reflect.KClass

@TestInstance(PER_CLASS)
private class EventFrameworkExampleTests : CoreDataGenerator by CoreDataGenerator.testProvider {

    @Test
    fun `publishing events to an event stream for the same key`() = test {

        val events = eventFramework()
        val streamReference = streamReference<TestEvent>()
        val accountId = "123"
        val event1 = TestEvent(accountId = accountId, value = "1")
        val event2 = TestEvent(accountId = accountId, value = "2")

        val stream = events.streams[streamReference]

        val (event1Offset) = stream.forKey(accountId).append(event1)
        val (event2Offset) = stream.forKey(accountId).append(event2)

        assertThat(event1Offset.value).isEqualTo(0L)
        assertThat(event2Offset.value).isEqualTo(1L)
    }

    private inline fun <reified EVENT : Event> streamReference(id: String = newId.ulid.monotonic().stringValue): EventStream.Reference<EVENT> = EventStream.Reference(id = id, eventClass = EVENT::class)

    private fun eventFramework(): EventFramework {
        return InMemoryEventFramework()
    }
}

interface Event {

}

data class TestEvent(val accountId: String, val value: String) : Event

class InMemoryEventFramework : EventFramework {

    private val streamsMap = mutableMapOf<String, Stream<*>>()

    override val streams: EventStreamOperations = Streams()

    private inner class Streams : EventStreamOperations {

        @Suppress("UNCHECKED_CAST")
        override fun <EVENT : Event> withId(id: String, eventClass: KClass<EVENT>): EventStream<EVENT> {

            val stream = streamsMap.getOrPut(id) { Stream(id, eventClass) } as Stream<EVENT>
            if (stream.eventClass !== eventClass) {
                throw IllegalArgumentException("Stream with ID '$id' already exist, but its eventClass '${stream.eventClass}' is different from the one requested '$eventClass'")
            }
            return stream
        }
    }

    private class Stream<EVENT : Event>(override val id: String, val eventClass: KClass<EVENT>) : EventStream<EVENT> {

        private val partitions = mutableMapOf<String, Partition<EVENT>>()

        override fun forKey(key: String) = partitions.getOrPut(key) { Partition(key) }

        private class Partition<EVENT : Event>(override val key: String) : EventStream.Partition<EVENT> {

            private val events = mutableListOf<EVENT>()

            override suspend fun append(event: EVENT) = synchronized(this) {
                events += event
                val offset = (events.size - 1).toLong().let { EventStream.Offset(it) }
                PublishedEventData(offset = offset)
            }
        }

        private data class PublishedEventData(override val offset: EventStream.Offset) : PublishedEvent
    }
}

interface EventFramework {

    val streams: EventStreamOperations
}

interface EventStreamOperations {

    fun <EVENT : Event> withId(id: String, eventClass: KClass<EVENT>): EventStream<EVENT>
}

interface EventStream<EVENT : Event> {

    val id: String

    data class Reference<EVENT : Event>(val id: String, val eventClass: KClass<EVENT>)

    fun forKey(key: String): Partition<EVENT>

    interface Partition<EVENT : Event> {

        val key: String

        suspend fun append(event: EVENT): PublishedEvent // TODO return the offset and partitioning information?
    }

    data class Offset(val value: Long)
}

interface PublishedEvent {

    val offset: EventStream.Offset

    operator fun component1(): EventStream.Offset
}

operator fun <EVENT : Event> EventStreamOperations.get(reference: EventStream.Reference<EVENT>) = withId(reference.id, reference.eventClass)