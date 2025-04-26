package sollecitom.examples.pulsar.messaging.transaction.avro.trade

import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.buildRecord
import sollecitom.libs.swissknife.avro.serialization.utils.getDouble
import sollecitom.libs.swissknife.avro.serialization.utils.getInt
import sollecitom.libs.swissknife.avro.serialization.utils.getString
import sollecitom.libs.swissknife.avro.serialization.utils.getValue
import sollecitom.libs.swissknife.avro.serialization.utils.setValue
import sollecitom.libs.swissknife.core.domain.identity.Id
import sollecitom.libs.pillar.avro.serialization.core.identity.avroSerde
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import org.apache.avro.generic.GenericRecord

val TradeOrderWasRequested.Companion.avroSchema get() = TradeTransactionAvroSchemas.tradeOrderWasRequested
val TradeOrderWasRequested.Companion.avroSerde: AvroSerde<TradeOrderWasRequested> get() = TradeOrderWasRequestedAvroSerde

private object TradeOrderWasRequestedAvroSerde : AvroSerde<TradeOrderWasRequested> {

    override val schema get() = TradeOrderWasRequested.avroSchema

    override fun serialize(value: TradeOrderWasRequested): GenericRecord = buildRecord {

        setValue(Fields.id, value.id, Id.avroSerde)
        set(Fields.assetId, value.assetId)
        set(Fields.tag, value.tag)
        set(Fields.quantity, value.quantity)
        set(Fields.pricePerShare, value.pricePerShare)
    }

    override fun deserialize(value: GenericRecord): TradeOrderWasRequested = with(value) {

        val id = getValue(sollecitom.examples.pulsar.messaging.transaction.avro.trade.TradeOrderWasRequestedAvroSerde.Fields.id, Id.avroSerde)
        val quantity = getInt(sollecitom.examples.pulsar.messaging.transaction.avro.trade.TradeOrderWasRequestedAvroSerde.Fields.quantity)
        val assetId = getString(sollecitom.examples.pulsar.messaging.transaction.avro.trade.TradeOrderWasRequestedAvroSerde.Fields.assetId)
        val pricePerShare = getDouble(sollecitom.examples.pulsar.messaging.transaction.avro.trade.TradeOrderWasRequestedAvroSerde.Fields.pricePerShare)
        val tag = getString(sollecitom.examples.pulsar.messaging.transaction.avro.trade.TradeOrderWasRequestedAvroSerde.Fields.tag)
        TradeOrderWasRequested(id = id, quantity = quantity, assetId = assetId, pricePerShare = pricePerShare, tag = tag)
    }

    private object Fields {

        const val assetId = "asset_id"
        const val pricePerShare = "price_per_share"
        const val quantity = "quantity"
        const val tag = "tag"
        const val id = "id"
    }
}