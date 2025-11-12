package sollecitom.examples.exercises.rpg

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
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

        private fun DungeonsAndDragons.newStrengthCheck(difficultyClass: Int): DungeonsAndDragons.Challenge = DungeonsAndDragons.AttributeChallenge(difficultyClass = difficultyClass, attribute = DungeonsAndDragons.Attribute.STRENGTH)

        private fun DungeonsAndDragons.newPlayer(strength: Int = 10, equippedItems: Set<DungeonsAndDragons.Wearable> = emptySet()): DungeonsAndDragons.Player = DungeonsAndDragonsPlayerImplementation(strength = strength, equippedItems = equippedItems)

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

    override fun bonusToAttribute(attribute: DungeonsAndDragons.Attribute) = when (attribute) {
        DungeonsAndDragons.Attribute.STRENGTH -> strength
        DungeonsAndDragons.Attribute.DEXTERITY -> dexterity
        DungeonsAndDragons.Attribute.CONSTITUTION -> constitution
        DungeonsAndDragons.Attribute.INTELLIGENCE -> intelligence
        DungeonsAndDragons.Attribute.WISDOM -> wisdom
        DungeonsAndDragons.Attribute.CHARISMA -> charisma
    }
}

interface D20 {

    fun roll(): Int
}

data class LoadedD20(private val result: Int) : D20 {

    override fun roll() = result
}

class DungeonsAndDragonsPlayerImplementation(private val strength: Int, private val equippedItems: Set<DungeonsAndDragons.Wearable>) : DungeonsAndDragons.Player {

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
        val bonus = bonus()
        return (score + bonus).modifier()
    }

    private fun DungeonsAndDragons.Attribute.bonus() = equipmentBonus()

    private fun DungeonsAndDragons.Attribute.equipmentBonus() = equippedItems.sumOf { it.bonusToAttribute(this) }

    private fun DungeonsAndDragons.Wearable.bonusToAttribute(attribute: DungeonsAndDragons.Attribute): Int = when (this) {
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