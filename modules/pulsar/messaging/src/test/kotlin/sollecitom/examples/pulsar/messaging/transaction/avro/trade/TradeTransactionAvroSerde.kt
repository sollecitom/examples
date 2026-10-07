package sollecitom.examples.pulsar.messaging.transaction.avro.trade

import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.buildRecord
import sollecitom.libs.swissknife.avro.serialization.utils.deserializeWith
import sollecitom.libs.swissknife.avro.serialization.utils.getEnvelope
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import org.apache.avro.generic.GenericRecord

val TradeTransaction.Companion.avroSchema get() = TradeTransactionAvroSchemas.trade
val TradeTransaction.Companion.avroSerde: AvroSerde<TradeTransaction> get() = TradeTransactionAvroSerde

private object TradeTransactionAvroSerde : AvroSerde<TradeTransaction> {

    override val schema get() = TradeTransaction.avroSchema

    override fun serialize(value: TradeTransaction): GenericRecord = buildRecord {
        val record = when (value) {
            is TradeOrderWasRequested -> TradeOrderWasRequested.avroSerde.serialize(value)
        }
        setEnvelope(record)
    }

    override fun deserialize(value: GenericRecord): TradeTransaction = value.getEnvelope { branchName, unionRecord ->
        when (branchName) {
            Types.tradeOrderWasRequested -> unionRecord.deserializeWith(TradeOrderWasRequested.avroSerde)
            else -> error("Unknown Trade Transaction type $branchName")
        }
    }

    private object Types {
        const val tradeOrderWasRequested = "TradeOrderWasRequested"
    }
}
