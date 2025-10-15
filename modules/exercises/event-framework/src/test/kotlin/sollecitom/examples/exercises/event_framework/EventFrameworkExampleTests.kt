package sollecitom.examples.exercises.event_framework

import assertk.assertThat
import assertk.assertions.isTrue
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
    fun `publishing events to an event stream`() = test {

        val events = eventFramework()
        val streamReference = streamReference<TestEvent>()

        val stream = events.streams[streamReference]

        assertThat(true).isTrue()
    }

    private inline fun <reified EVENT : Event> streamReference(id: String = newId.ulid.monotonic().stringValue): EventStream.Reference<EVENT> = EventStream.Reference(id = id, eventClass = EVENT::class)

    private fun eventFramework(): EventFramework {
        return InMemoryEventFramework()
    }
}

interface Event {

}

data class TestEvent(val value: String) : Event

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

    private class Stream<EVENT : Event>(private val id: String, val eventClass: KClass<EVENT>) : EventStream<EVENT> {

    }
}

interface EventFramework {

    val streams: EventStreamOperations
}

interface EventStreamOperations {

    fun <EVENT : Event> withId(id: String, eventClass: KClass<EVENT>): EventStream<EVENT>
}

interface EventStream<EVENT : Event> {

    data class Reference<EVENT : Event>(val id: String, val eventClass: KClass<EVENT>)
}

operator fun <EVENT : Event> EventStreamOperations.get(reference: EventStream.Reference<EVENT>) = withId(reference.id, reference.eventClass)