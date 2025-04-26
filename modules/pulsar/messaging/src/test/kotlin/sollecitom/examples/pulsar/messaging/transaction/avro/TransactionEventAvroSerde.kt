package sollecitom.examples.pulsar.messaging.transaction.avro

import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.buildRecord
import sollecitom.libs.swissknife.avro.serialization.utils.getValue
import sollecitom.libs.swissknife.avro.serialization.utils.setValue
import sollecitom.libs.pillar.avro.serialization.ddd.utils.WithEventAvroSerdeSupport
import sollecitom.examples.pulsar.messaging.transaction.Transaction
import sollecitom.examples.pulsar.messaging.transaction.TransactionEvent
import org.apache.avro.generic.GenericRecord

val TransactionEvent.Companion.avroSchema get() = TransactionAvroSchemas.transactionEvent
val TransactionEvent.Companion.avroSerde: AvroSerde<TransactionEvent> get() = TransactionEventAvroSerde

private object TransactionEventAvroSerde : AvroSerde<TransactionEvent>, WithEventAvroSerdeSupport {

    override val schema get() = TransactionEvent.avroSchema

    override fun serialize(value: TransactionEvent): GenericRecord = buildRecord {

        setEventFields(value)
        setValue(Fields.transaction, value.transaction, Transaction.avroSerde)
    }

    override fun deserialize(value: GenericRecord) = with(value) {

        val (id, timestamp, context) = getEventFields()
        val transaction = getValue(sollecitom.examples.pulsar.messaging.transaction.avro.TransactionEventAvroSerde.Fields.transaction, Transaction.avroSerde)
        TransactionEvent(id = id, timestamp = timestamp, transaction = transaction, context = context)
    }

    private object Fields {
        const val transaction = "transaction"
    }
}