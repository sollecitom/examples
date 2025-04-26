package sollecitom.examples.pulsar.messaging.transaction.avro.trade

import sollecitom.libs.pillar.avro.serialization.test.utils.AcmeAvroSerdeTestSpecification
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS


@TestInstance(PER_CLASS)
private class TradeTransactionAvroSerdeTest : AcmeAvroSerdeTestSpecification<TradeTransaction>, CoreDataGenerator by CoreDataGenerator.testProvider {

    override val avroSerde = TradeTransaction.avroSerde

    override fun parameterizedArguments() = listOf(
        "trade order was requested" to TradeOrderWasRequested(newId.external(), 200, "some-asset", 4.99, "investment")
    )
}