package sollecitom.examples.pulsar.messaging.transaction.avro.trade

import sollecitom.libs.swissknife.avro.schema.catalogue.domain.AvroSchemaCatalogueTemplate
import sollecitom.libs.swissknife.avro.schema.catalogue.domain.AvroSchemaContainer
import sollecitom.libs.pillar.avro.serialization.core.identity.IdentityAvroSchemas
import org.apache.avro.Schema

object TradeTransactionAvroSchemas : AvroSchemaCatalogueTemplate("acme.transaction.trade") {

    val tradeOrderWasRequested: Schema by lazy { getSchema(name = "TradeOrderWasRequested", dependencies = setOf(IdentityAvroSchemas.id)) }
    val trade: Schema by lazy { getSchema(name = "TradeTransaction", dependencies = setOfNotNull(tradeOrderWasRequested)) }

    override val nestedContainers: Set<AvroSchemaContainer> = emptySet()

    override val all: Sequence<Schema> = sequenceOf(tradeOrderWasRequested, trade)
}