package sollecitom.examples.pulsar.messaging.transaction.avro.card

import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.buildRecord
import sollecitom.libs.swissknife.avro.serialization.utils.deserializeWith
import sollecitom.libs.swissknife.avro.serialization.utils.getRecordFromUnion
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasProcessed
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasRequested
import org.apache.avro.generic.GenericRecord

val CardTransaction.Companion.avroSchema get() = CardTransactionAvroSchemas.card
val CardTransaction.Companion.avroSerde: AvroSerde<CardTransaction> get() = CardTransactionAvroSerde

private object CardTransactionAvroSerde : AvroSerde<CardTransaction> {

    override val schema get() = CardTransaction.avroSchema

    override fun serialize(value: CardTransaction): GenericRecord = buildRecord {
        val record = when (value) {
            is CardAuthWasRequested -> CardAuthWasRequested.avroSerde.serialize(value)
            is CardAuthWasProcessed -> CardAuthWasProcessed.avroSerde.serialize(value)
        }
        setRecordInUnion(value.type(), record)
    }

    override fun deserialize(value: GenericRecord): CardTransaction = value.getRecordFromUnion { unionTypeName, unionRecord ->
        when (unionTypeName) {
            Types.cardAuthWasRequested -> unionRecord.deserializeWith(CardAuthWasRequested.avroSerde)
            Types.cardAuthWasProcessed -> unionRecord.deserializeWith(CardAuthWasProcessed.avroSerde)
            else -> error("Unknown Transaction type $unionTypeName")
        }
    }

    private fun CardTransaction.type(): String = when (this) {
        is CardAuthWasRequested -> Types.cardAuthWasRequested
        is CardAuthWasProcessed -> Types.cardAuthWasProcessed
    }

    private object Types {
        const val cardAuthWasRequested = "CardAuthWasRequested"
        const val cardAuthWasProcessed = "CardAuthWasProcessed"
    }
}
