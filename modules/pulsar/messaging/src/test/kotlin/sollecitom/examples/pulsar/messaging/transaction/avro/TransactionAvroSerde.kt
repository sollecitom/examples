package sollecitom.examples.pulsar.messaging.transaction.avro

import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.buildRecord
import sollecitom.libs.swissknife.avro.serialization.utils.deserializeWith
import sollecitom.libs.swissknife.avro.serialization.utils.getRecordFromUnion
import sollecitom.examples.pulsar.messaging.transaction.Transaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction
import sollecitom.examples.pulsar.messaging.transaction.avro.card.avroSerde
import sollecitom.examples.pulsar.messaging.transaction.avro.trade.avroSerde
import org.apache.avro.generic.GenericRecord

val Transaction.Companion.avroSchema get() = TransactionAvroSchemas.transaction
val Transaction.Companion.avroSerde: AvroSerde<Transaction> get() = TransactionAvroSerde

private object TransactionAvroSerde : AvroSerde<Transaction> {

    override val schema get() = Transaction.avroSchema

    override fun serialize(value: Transaction): GenericRecord = buildRecord {
        val record = when (value) {
            is CardTransaction -> CardTransaction.avroSerde.serialize(value)
            is TradeTransaction -> TradeTransaction.avroSerde.serialize(value)
        }
        setRecordInUnion(value.type(), record)
    }

    override fun deserialize(value: GenericRecord) = value.getRecordFromUnion { unionTypeName, unionRecord ->
        when (unionTypeName) {
            Types.cardTransaction -> unionRecord.deserializeWith(CardTransaction.avroSerde)
            Types.tradeTransaction -> unionRecord.deserializeWith(TradeTransaction.avroSerde)
            else -> error("Unknown Transaction type $unionTypeName")
        }
    }

    private fun Transaction.type(): String = when (this) {
        is CardTransaction -> Types.cardTransaction
        is TradeTransaction -> Types.tradeTransaction
    }

    private object Types {
        const val cardTransaction = "CardTransaction"
        const val tradeTransaction = "TradeTransaction"
    }
}
