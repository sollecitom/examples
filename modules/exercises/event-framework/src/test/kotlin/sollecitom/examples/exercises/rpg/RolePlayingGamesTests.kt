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
    // a player attempts to knock a door down

    @Test
    fun `a Dungeons & Dragons player attempts to knock a door down`() = test { // TODO refactor

        val challenge = newKnockDoorDownChallenge(difficultyClass = 13)
        val dAndDPlayer = newDungeonsAndDragonsPlayer(strength = 14)
        val dice = LoadedD20(result = 11)

        val outcome = dAndDPlayer.attempt(challenge, dice)

        assertThat(outcome).succeeded()
    }

    private fun newKnockDoorDownChallenge(difficultyClass: Int = 10): DungeonsAndDragonsChallenge = newDungeonsAndDragonsStrengthCheck(difficultyClass)

    private fun newDungeonsAndDragonsStrengthCheck(difficultyClass: Int): DungeonsAndDragonsChallenge = DungeonsAndDragonsAttributeChallenge(difficultyClass = difficultyClass, attribute = DungeonsAndDragonsAttribute.STRENGTH)

    private fun newDungeonsAndDragonsPlayer(strength: Int = 10): DungeonsAndDragonsPlayer {

        return DungeonsAndDragonsPlayerImplementation(strength = strength)
    }
}

interface D20 {

    fun roll(): Int
}

data class LoadedD20(private val result: Int) : D20 {

    override fun roll() = result
}

class DungeonsAndDragonsPlayerImplementation(private val strength: Int) : DungeonsAndDragonsPlayer {

    init {
        require(strength >= 0) { "Strength must be greater or equal to zero" }
    }

    override fun attempt(challenge: DungeonsAndDragonsChallenge, dice: D20): DungeonsAndDragonsChallenge.Outcome {

        return when (challenge) {
            is DungeonsAndDragonsAttributeChallenge -> attemptAttributeChallenge(challenge, dice)
        }
    }

    private fun attemptAttributeChallenge(challenge: DungeonsAndDragonsAttributeChallenge, dice: D20): DungeonsAndDragonsChallenge.Outcome {

        val baseResult = dice.roll()
        if (baseResult == 1) {
            return DungeonsAndDragonsChallenge.Outcome.Fumble
        }
        if (baseResult == 20) {
            return DungeonsAndDragonsChallenge.Outcome.CriticalSuccess
        }
        val modifier = challenge.modifier()
        val result = baseResult + modifier
        return if (result >= challenge.difficultyClass) DungeonsAndDragonsChallenge.Outcome.Success else DungeonsAndDragonsChallenge.Outcome.Failure
    }

    private fun DungeonsAndDragonsAttributeChallenge.modifier(): Int = modifierForAttribute(attribute = attribute)

    private fun modifierForAttribute(attribute: DungeonsAndDragonsAttribute): Int {

        val score = when (attribute) {
            DungeonsAndDragonsAttribute.STRENGTH -> this.strength
            DungeonsAndDragonsAttribute.DEXTERITY -> TODO("not implemented yet")
            DungeonsAndDragonsAttribute.CONSTITUTION -> TODO("not implemented yet")
            DungeonsAndDragonsAttribute.INTELLIGENCE -> TODO("not implemented yet")
            DungeonsAndDragonsAttribute.WISDOM -> TODO("not implemented yet")
            DungeonsAndDragonsAttribute.CHARISMA -> TODO("not implemented yet")
        }
        return attributeModifier(score)
    }

    private fun attributeModifier(score: Int): Int = (score - 10) / 2
}

private fun Assert<DungeonsAndDragonsChallenge.Outcome>.succeeded() = given { outcome ->

    assertThat(outcome).isEqualTo(DungeonsAndDragonsChallenge.Outcome.Success)
}

data class DungeonsAndDragonsAttributeChallenge(override val difficultyClass: Int, val attribute: DungeonsAndDragonsAttribute) : DungeonsAndDragonsChallenge

sealed interface DungeonsAndDragonsChallenge {

    val difficultyClass: Int

    sealed interface Outcome {

        object Fumble : Outcome
        object Failure : Outcome
        object Success : Outcome
        object CriticalSuccess : Outcome
    }
}

enum class DungeonsAndDragonsAttribute {

    STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA
}

interface DungeonsAndDragonsPlayer {

    fun attempt(challenge: DungeonsAndDragonsChallenge, dice: D20): DungeonsAndDragonsChallenge.Outcome
}