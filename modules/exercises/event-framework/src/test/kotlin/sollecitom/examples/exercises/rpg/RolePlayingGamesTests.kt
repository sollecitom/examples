package sollecitom.examples.exercises.rpg

import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.libs.swissknife.test.utils.execution.utils.test

@TestInstance(PER_CLASS)
private class RolePlayingGamesTests {

    // Tests To-Do List
    // a player attempts to knock a door down

    @Test
    fun `something who knows yet`() = test {

        assertThat(true).isTrue()
    }
}