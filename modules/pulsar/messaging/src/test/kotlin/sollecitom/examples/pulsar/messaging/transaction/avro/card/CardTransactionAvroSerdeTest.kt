package sollecitom.examples.pulsar.messaging.transaction.avro.card

import sollecitom.libs.pillar.avro.serialization.test.utils.AcmeAvroSerdeTestSpecification
import sollecitom.libs.swissknife.core.domain.text.Name
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.cryptography.domain.factory.CryptographicOperations
import sollecitom.libs.swissknife.cryptography.domain.key.generator.CryptographicKeyGenerator
import sollecitom.libs.swissknife.cryptography.domain.key.generator.newAesKey
import sollecitom.libs.swissknife.cryptography.domain.symmetric.encryption.aes.AES
import sollecitom.libs.swissknife.cryptography.implementation.bouncycastle.bouncyCastle
import sollecitom.libs.swissknife.protected_value.domain.ProtectedValueFactory
import sollecitom.libs.swissknife.protected_value.implementation.bouncy_castle.aes256WithCTR
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasProcessed
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasRequested
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS


@TestInstance(PER_CLASS)
private class CardTransactionAvroRecordSerdeTest : AcmeAvroSerdeTestSpecification<CardTransaction>, CoreDataGenerator by CoreDataGenerator.testProvider, CryptographicKeyGenerator {

    override val cryptographicOperations = CryptographicOperations.bouncyCastle(random = secureRandom)
    override val avroSerde = CardTransaction.avroSerde
    private val cardNumber = "4321 1234 5678 98123"
    private val key = newAesKey(variant = AES.Variant.AES_256)
    private val factory = ProtectedValueFactory.aes256WithCTR { key }

    override fun parameterizedArguments() = listOf(
        "requested" to runBlocking { CardAuthWasRequested(factory.protectValue(cardNumber, "card number".let(::Name), newId.external(), String::toByteArray), newId.external(), 23.8, "food") },
        "processed" to runBlocking { CardAuthWasProcessed(factory.protectValue(cardNumber, "card number".let(::Name), newId.external(), String::toByteArray), newId.external(), 11.8, "entertainment") },
    )
}