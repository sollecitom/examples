package sollecitom.examples.pulsar.messaging.transaction.avro.card

import sollecitom.libs.pillar.protected_value.messaging.serialization.avro.stringAvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.buildRecord
import sollecitom.libs.swissknife.avro.serialization.utils.getDouble
import sollecitom.libs.swissknife.avro.serialization.utils.getString
import sollecitom.libs.swissknife.avro.serialization.utils.getValue
import sollecitom.libs.swissknife.avro.serialization.utils.setValue
import sollecitom.libs.swissknife.core.domain.identity.Id
import sollecitom.libs.pillar.avro.serialization.core.identity.avroSerde
import sollecitom.libs.swissknife.protected_value.domain.ProtectedValue
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasRequested
import sollecitom.examples.pulsar.messaging.transaction.avro.card.CardTransactionAvroSchemas.cardAuthWasRequested
import org.apache.avro.generic.GenericRecord

val CardAuthWasRequested.Companion.avroSchema get() = cardAuthWasRequested
val CardAuthWasRequested.Companion.avroSerde: AvroSerde<CardAuthWasRequested> get() = CardAuthWasRequestedAvroSerde

private object CardAuthWasRequestedAvroSerde : AvroSerde<CardAuthWasRequested> {

    override val schema get() = CardAuthWasRequested.avroSchema

    override fun serialize(value: CardAuthWasRequested): GenericRecord = buildRecord {

        setValue(Fields.id, value.id, Id.avroSerde)
        setValue(Fields.cardNumber, value.cardNumber, ProtectedValue.stringAvroSerde)
        set(Fields.amount, value.amount)
        set(Fields.tag, value.tag)
    }

    override fun deserialize(value: GenericRecord): CardAuthWasRequested = with(value) {

        val id = getValue(CardAuthWasRequestedAvroSerde.Fields.id, Id.avroSerde)
        val cardNumber = getValue(CardAuthWasRequestedAvroSerde.Fields.cardNumber, ProtectedValue.stringAvroSerde)
        val amount = getDouble(CardAuthWasRequestedAvroSerde.Fields.amount)
        val tag = getString(CardAuthWasRequestedAvroSerde.Fields.tag)
        CardAuthWasRequested(cardNumber = cardNumber, id = id, amount = amount, tag = tag)
    }

    private object Fields {
        const val cardNumber = "card_number"
        const val amount = "amount"
        const val tag = "tag"
        const val id = "id"
    }
}