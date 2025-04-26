package sollecitom.examples.pulsar.messaging.transaction.avro

import sollecitom.libs.pillar.avro.serialization.test.utils.AcmeAvroSerdeTestSpecification
import sollecitom.libs.swissknife.core.domain.text.Name
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.correlation.core.domain.context.InvocationContext
import sollecitom.libs.swissknife.correlation.core.test.utils.context.create
import sollecitom.libs.swissknife.cryptography.domain.factory.CryptographicOperations
import sollecitom.libs.swissknife.cryptography.domain.key.generator.CryptographicKeyGenerator
import sollecitom.libs.swissknife.cryptography.domain.key.generator.newAesKey
import sollecitom.libs.swissknife.cryptography.domain.symmetric.encryption.aes.AES
import sollecitom.libs.swissknife.cryptography.implementation.bouncycastle.bouncyCastle
import sollecitom.libs.swissknife.ddd.domain.Event
import sollecitom.libs.swissknife.protected_value.domain.ProtectedValueFactory
import sollecitom.libs.swissknife.protected_value.implementation.bouncy_castle.aes256WithCTR
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasProcessed
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasRequested
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import sollecitom.examples.pulsar.messaging.transaction.TransactionEvent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
private class TransactionEventAvroSerdeTests : AcmeAvroSerdeTestSpecification<TransactionEvent>, CoreDataGenerator by CoreDataGenerator.testProvider, CryptographicKeyGenerator {

    override val cryptographicOperations = CryptographicOperations.bouncyCastle(random = secureRandom)
    override val avroSerde = TransactionEvent.avroSerde
    private val cardNumber = "4321 1234 5678 98123"
    private val key = newAesKey(variant = AES.Variant.AES_256)
    private val factory = ProtectedValueFactory.aes256WithCTR { key }

    override fun parameterizedArguments() = listOf(
        "card auth was requested" to runBlocking { TransactionEvent(transaction = CardAuthWasRequested(factory.protectValue(cardNumber, "card number".let(::Name), newId.external(), String::toByteArray), id = newId.external(), amount = 9.95, tag = "bills"), id = newId.external(), timestamp = clock.now(), context = Event.Context(invocation = InvocationContext.create(), parent = null, originating = null)) },
        "card auth was processed" to runBlocking { TransactionEvent(transaction = CardAuthWasProcessed(factory.protectValue(cardNumber, "card number".let(::Name), newId.external(), String::toByteArray), newId.external(), 19.3, "entertainment"), id = newId.external(), timestamp = clock.now(), context = Event.Context(invocation = InvocationContext.create(), parent = null, originating = null)) },
        "trade order was requested" to TransactionEvent(transaction = TradeOrderWasRequested(newId.external(), 200, "some-asset", 4.99, "investment"), id = newId.external(), timestamp = clock.now(), context = Event.Context(invocation = InvocationContext.create(), parent = null, originating = null))
    )
}