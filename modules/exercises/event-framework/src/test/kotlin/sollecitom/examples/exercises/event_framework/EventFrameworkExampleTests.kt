package sollecitom.examples.exercises.event_framework

import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.test.utils.execution.utils.test

@TestInstance(PER_CLASS)
private class EventFrameworkExampleTests : CoreDataGenerator by CoreDataGenerator.testProvider {

    @Test
    fun `publishing events to a partitioned event stream`() = test {

        assertThat(true).isTrue()
    }
}