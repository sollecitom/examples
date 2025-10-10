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
        val streamReference = EventStream.Reference(id = newId.ulid.monotonic().stringValue, eventClass = TestEvent::class)

        val stream = events.streamWithReference(streamReference)

        assertThat(true).isTrue()
    }

    private fun eventFramework(): EventFramework {
        return InMemoryEventFramework()
    }
}

interface Event {

}

data class TestEvent(val value: String) : Event

class InMemoryEventFramework : EventFramework {

    private val streams = mutableMapOf<String, Stream<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <EVENT : Event> streamWithId(id: String, eventClass: KClass<EVENT>): EventStream<EVENT> = synchronized(id) {

        val stream = streams.getOrPut(id) { Stream(id, eventClass) } as Stream<EVENT>
        if (stream.eventClass !== eventClass) {
            throw IllegalArgumentException("Stream with ID '$id' already exist, but its eventClass '${stream.eventClass}' is different from the one requested '$eventClass'")
        }
        stream
    }

    private class Stream<EVENT : Event>(private val id: String, val eventClass: KClass<EVENT>) : EventStream<EVENT> {

    }
}

interface EventFramework {

    fun <EVENT : Event> streamWithId(id: String, eventClass: KClass<EVENT>): EventStream<EVENT>
}

interface EventStream<EVENT : Event> {

    data class Reference<EVENT : Event>(val id: String, val eventClass: KClass<EVENT>)
}

fun <EVENT : Event> EventFramework.streamWithReference(reference: EventStream.Reference<EVENT>) = streamWithId(reference.id, reference.eventClass)