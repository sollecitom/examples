package sollecitom.examples.exercises.rpg

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.test.utils.execution.utils.test

@TestInstance(PER_CLASS)
private class RolePlayingGamesTests : CoreDataGenerator by CoreDataGenerator.testProvider {

    // Tests To-Do List
    // a player fails to knock a door down
    // critical success beats every DC
    // fumble fails every DC

    @Test
    fun `a Dungeons & Dragons player knocks a door down`() = testWithGame(DungeonsAndDragons) {

        val challenge = newKnockDoorDownChallenge(difficultyClass = 13)
        val player = newPlayer(strength = 14)
        val dice = loadedD20(result = 11)

        val outcome = player.attempt(challenge, dice)

        assertThat(outcome).succeeded()
    }

    private fun <GAME : Any> testWithGame(game: GAME, action: suspend GAME.() -> Unit) = test {

        with(game) {
            action()
        }
    }

    private fun loadedD20(result: Int): D20 = LoadedD20(result)

    private fun DungeonsAndDragons.newKnockDoorDownChallenge(difficultyClass: Int = 10): DungeonsAndDragons.Challenge = newDungeonsAndDragonsStrengthCheck(difficultyClass)

    private fun newDungeonsAndDragonsStrengthCheck(difficultyClass: Int): DungeonsAndDragons.Challenge = DungeonsAndDragons.AttributeChallenge(difficultyClass = difficultyClass, attribute = DungeonsAndDragons.Attribute.STRENGTH)

    private fun DungeonsAndDragons.newPlayer(strength: Int = 10): DungeonsAndDragons.Player {

        return DungeonsAndDragonsPlayerImplementation(strength = strength)
    }
}

interface D20 {

    fun roll(): Int
}

data class LoadedD20(private val result: Int) : D20 {

    override fun roll() = result
}

class DungeonsAndDragonsPlayerImplementation(private val strength: Int) : DungeonsAndDragons.Player {

    init {
        require(strength >= 0) { "Strength must be greater or equal to zero" }
    }

    override fun attempt(challenge: DungeonsAndDragons.Challenge, dice: D20) = when (challenge) {

        is DungeonsAndDragons.AttributeChallenge -> attemptAttributeChallenge(challenge, dice)
    }

    private fun attemptAttributeChallenge(challenge: DungeonsAndDragons.AttributeChallenge, dice: D20): DungeonsAndDragons.Challenge.Outcome {

        val baseResult = dice.roll()
        if (baseResult == 1) {
            return DungeonsAndDragons.Challenge.Outcome.Fumble
        }
        if (baseResult == 20) {
            return DungeonsAndDragons.Challenge.Outcome.CriticalSuccess
        }
        val modifier = challenge.attribute.modifier()
        val result = baseResult + modifier
        return if (result >= challenge.difficultyClass) DungeonsAndDragons.Challenge.Outcome.Success else DungeonsAndDragons.Challenge.Outcome.Failure
    }

    private fun DungeonsAndDragons.Attribute.modifier(): Int {

        val score = when (this) {
            DungeonsAndDragons.Attribute.STRENGTH -> strength
            DungeonsAndDragons.Attribute.DEXTERITY -> TODO("not implemented yet")
            DungeonsAndDragons.Attribute.CONSTITUTION -> TODO("not implemented yet")
            DungeonsAndDragons.Attribute.INTELLIGENCE -> TODO("not implemented yet")
            DungeonsAndDragons.Attribute.WISDOM -> TODO("not implemented yet")
            DungeonsAndDragons.Attribute.CHARISMA -> TODO("not implemented yet")
        }
        return score.modifier()
    }

    private fun Int.modifier(): Int = (this - 10) / 2
}

private fun Assert<DungeonsAndDragons.Challenge.Outcome>.succeeded() = given { outcome ->

    assertThat(outcome).isEqualTo(DungeonsAndDragons.Challenge.Outcome.Success)
}

object DungeonsAndDragons {

    interface Player {

        fun attempt(challenge: Challenge, dice: D20): Challenge.Outcome
    }

    enum class Attribute {

        STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA
    }

    data class AttributeChallenge(override val difficultyClass: Int, val attribute: Attribute) : Challenge

    sealed interface Challenge {

        val difficultyClass: Int

        sealed interface Outcome {

            object Fumble : Outcome
            object Failure : Outcome
            object Success : Outcome
            object CriticalSuccess : Outcome
        }
    }
}