package sollecitom.examples.pulsar.messaging.transaction.avro

import sollecitom.examples.pulsar.messaging.transaction.avro.card.CardTransactionAvroSchemas
import sollecitom.examples.pulsar.messaging.transaction.avro.trade.TradeTransactionAvroSchemas
import sollecitom.libs.pillar.avro.serialization.core.identity.avroSchema
import sollecitom.libs.pillar.avro.serialization.core.time.avroSchema
import sollecitom.libs.pillar.avro.serialization.ddd.event.avroSchema
import sollecitom.libs.swissknife.avro.schema.catalogue.domain.AvroSchemaCatalogueTemplate
import sollecitom.libs.swissknife.avro.schema.catalogue.domain.AvroSchemaContainer
import sollecitom.libs.swissknife.core.domain.identity.Id
import sollecitom.libs.swissknife.ddd.domain.Event
import kotlinx.datetime.Instant
import org.apache.avro.Schema

object TransactionAvroSchemas : AvroSchemaCatalogueTemplate("acme.transaction") {

    val transaction: Schema by lazy { getSchema(name = "Transaction", dependencies = setOf(CardTransactionAvroSchemas.card, TradeTransactionAvroSchemas.trade)) }
    val transactionEvent: Schema by lazy { getSchema(name = "TransactionEvent", dependencies = setOf(transaction, Id.avroSchema, Instant.avroSchema, Event.Context.avroSchema)) }

    override val nestedContainers: Set<AvroSchemaContainer> = emptySet()

    override val all: Sequence<Schema> = sequenceOf(transaction, transactionEvent)
}