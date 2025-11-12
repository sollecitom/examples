package sollecitom.examples.exercises.rpg

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.examples.exercises.rpg.DungeonsAndDragons.Attribute
import sollecitom.examples.exercises.rpg.DungeonsAndDragons.Attribute.*
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.test.utils.execution.utils.test

@TestInstance(PER_CLASS)
private class RolePlayingGamesTests : CoreDataGenerator by CoreDataGenerator.testProvider {

    // Tests To-Do List
    // temporary attribute enhancing effects
    // skill checks

    @Nested
    inner class DungeonsAndDragonsTests {

        @Test
        fun `a player knocks a door down`() = testWithGame(DungeonsAndDragons) {

            val challenge = newKnockDoorDownChallenge(difficultyClass = 13)
            val player = newPlayer(strength = 14)

            val dice = loadedD20(result = 11)
            val outcome = player.attempt(challenge, dice) // 11 + 2 = 13 <= 13 => success

            assertThat(outcome).succeeded()
        }

        @Test
        fun `a player fails to knock a door down`() = testWithGame(DungeonsAndDragons) {

            val challenge = newKnockDoorDownChallenge(difficultyClass = 13)
            val player = newPlayer(strength = 14)

            val dice = loadedD20(result = 10)
            val outcome = player.attempt(challenge, dice) // 10 + 2 = 12 < 13 => failure

            assertThat(outcome).failed()
        }

        @Test
        fun `a player critically succeeds in knocking a door down`() = testWithGame(DungeonsAndDragons) {

            val challenge = newKnockDoorDownChallenge(difficultyClass = 35)
            val player = newPlayer(strength = 14)

            val dice = loadedD20(result = 20)
            val outcome = player.attempt(challenge, dice) // 20 + 2 = 22 < 35 => critical success

            assertThat(outcome).criticallySucceeded()
        }

        @Test
        fun `a player fumbles in knocking a door down`() = testWithGame(DungeonsAndDragons) {

            val challenge = newKnockDoorDownChallenge(difficultyClass = 5)
            val player = newPlayer(strength = 20)

            val dice = loadedD20(result = 1)
            val outcome = player.attempt(challenge, dice) // 1 + 5 = 6 >= 5 => fumble

            assertThat(outcome).fumbled()
        }

        @Test
        fun `a player knocks a door down with help from a magical item`() = testWithGame(DungeonsAndDragons) {

            val challenge = newKnockDoorDownChallenge(difficultyClass = 23)
            val giantStrengthGauntlets = newStrengthEnhancingMagicalItem(bonus = 6)
            val player = newPlayer(strength = 16, equippedItems = setOf(giantStrengthGauntlets))

            val dice = loadedD20(result = 17)
            val outcome = player.attempt(challenge, dice) // 17 + 3 + 3 (from +6 str bonus) = 23 <= 23 => success

            assertThat(outcome).succeeded()
        }

        private fun DungeonsAndDragons.newKnockDoorDownChallenge(difficultyClass: Int = 10): DungeonsAndDragons.Challenge = newStrengthCheck(difficultyClass)

        private fun DungeonsAndDragons.newStrengthCheck(difficultyClass: Int): DungeonsAndDragons.Challenge = DungeonsAndDragons.AttributeChallenge(difficultyClass = difficultyClass, attribute = STRENGTH)

        private fun DungeonsAndDragons.newPlayer(
            strength: Int = 10,
            dexterity: Int = 10,
            constitution: Int = 10,
            intelligence: Int = 10,
            wisdom: Int = 10,
            charisma: Int = 10,
            equippedItems: Set<DungeonsAndDragons.Wearable> = emptySet()
        ): DungeonsAndDragons.Player = DungeonsAndDragonsPlayerImplementation(
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            equippedItems = equippedItems
        )

        private fun DungeonsAndDragons.newStrengthEnhancingMagicalItem(bonus: Int): DungeonsAndDragons.Wearable = DungeonsAndDragonsAttributeEnhancingWearable(strength = bonus)

        private fun Assert<DungeonsAndDragons.Challenge.Outcome>.succeeded() = given { outcome ->

            assertThat(outcome).isEqualTo(DungeonsAndDragons.Challenge.Outcome.Success)
        }

        private fun Assert<DungeonsAndDragons.Challenge.Outcome>.failed() = given { outcome ->

            assertThat(outcome).isEqualTo(DungeonsAndDragons.Challenge.Outcome.Failure)
        }

        private fun Assert<DungeonsAndDragons.Challenge.Outcome>.criticallySucceeded() = given { outcome ->

            assertThat(outcome).isEqualTo(DungeonsAndDragons.Challenge.Outcome.CriticalSuccess)
        }

        private fun Assert<DungeonsAndDragons.Challenge.Outcome>.fumbled() = given { outcome ->

            assertThat(outcome).isEqualTo(DungeonsAndDragons.Challenge.Outcome.Fumble)
        }
    }

    private fun <GAME : Any> testWithGame(game: GAME, action: suspend GAME.() -> Unit) = test {

        with(game) {
            action()
        }
    }

    private fun loadedD20(result: Int): D20 = LoadedD20(result)
}

class DungeonsAndDragonsAttributeEnhancingWearable(private val strength: Int = 0, private val dexterity: Int = 0, private val constitution: Int = 0, private val intelligence: Int = 0, private val wisdom: Int = 0, private val charisma: Int = 0) : DungeonsAndDragons.Wearable.AttributeEnhancing {

    private val bonuses = mapOf(
        STRENGTH to strength,
        DEXTERITY to dexterity,
        CONSTITUTION to constitution,
        INTELLIGENCE to intelligence,
        WISDOM to wisdom,
        CHARISMA to charisma,
    )

    override fun bonusToAttribute(attribute: Attribute) = bonuses[attribute]!!
}

interface D20 {

    fun roll(): Int
}

data class LoadedD20(private val result: Int) : D20 {

    override fun roll() = result
}

class DungeonsAndDragonsPlayerImplementation(
    strength: Int,
    dexterity: Int,
    constitution: Int,
    intelligence: Int,
    wisdom: Int,
    charisma: Int,
    private val equippedItems: Set<DungeonsAndDragons.Wearable>
) : DungeonsAndDragons.Player {

    private val attributes = mapOf(
        STRENGTH to strength,
        DEXTERITY to dexterity,
        CONSTITUTION to constitution,
        INTELLIGENCE to intelligence,
        WISDOM to wisdom,
        CHARISMA to charisma
    )

    init {
        attributes.forEach { (attribute, score) -> require(score >= 0) { "Attribute $attribute cannot have a score lower than zero" } }
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

    private fun Attribute.modifier(): Int {

        val score = attributes[this]!!
        val bonus = bonus()
        return (score + bonus).modifier()
    }

    private fun Attribute.bonus() = equipmentBonus()

    private fun Attribute.equipmentBonus() = equippedItems.sumOf { it.bonusToAttribute(this) }

    private fun DungeonsAndDragons.Wearable.bonusToAttribute(attribute: Attribute): Int = when (this) {
        is DungeonsAndDragons.Wearable.AttributeEnhancing -> bonusToAttribute(attribute)
        else -> 0
    }

    private fun Int.modifier(): Int = (this - 10) / 2
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

            data object Fumble : Outcome
            data object Failure : Outcome
            data object Success : Outcome
            data object CriticalSuccess : Outcome
        }
    }

    interface Wearable {

        interface AttributeEnhancing : Wearable {

            fun bonusToAttribute(attribute: Attribute): Int
        }
    }
}