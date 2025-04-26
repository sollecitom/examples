package sollecitom.examples.pulsar.messaging.transaction.avro

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
import sollecitom.examples.pulsar.messaging.transaction.Transaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasProcessed
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasRequested
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class TransactionAvroSerdeTests : AcmeAvroSerdeTestSpecification<Transaction>, CoreDataGenerator by CoreDataGenerator.testProvider, CryptographicKeyGenerator {

    override val cryptographicOperations = CryptographicOperations.bouncyCastle(random = secureRandom)
    override val avroSerde = Transaction.avroSerde
    private val cardNumber = "4321 1234 5678 98123"
    private val key = newAesKey(variant = AES.Variant.AES_256)
    private val factory = ProtectedValueFactory.aes256WithCTR { key }

    override fun parameterizedArguments() = listOf(
        "card auth was requested" to runBlocking { CardAuthWasRequested(factory.protectValue(cardNumber, "card number".let(::Name), newId.external(), String::toByteArray), newId.external(), 23.8, "food") },
        "card auth was processed" to runBlocking { CardAuthWasProcessed(factory.protectValue(cardNumber, "card number".let(::Name), newId.external(), String::toByteArray), newId.external(), 19.3, "entertainment") },
        "trade order was requested" to TradeOrderWasRequested(newId.external(), 200, "some-asset", 4.99, "investment")
    )
}