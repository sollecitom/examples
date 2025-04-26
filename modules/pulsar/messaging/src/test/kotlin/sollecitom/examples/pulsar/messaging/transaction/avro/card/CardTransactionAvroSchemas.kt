package sollecitom.examples.pulsar.messaging.transaction.avro.card

import sollecitom.libs.pillar.protected_value.messaging.serialization.avro.stringAvroSchema
import sollecitom.libs.swissknife.avro.schema.catalogue.domain.AvroSchemaCatalogueTemplate
import sollecitom.libs.swissknife.avro.schema.catalogue.domain.AvroSchemaContainer
import sollecitom.libs.pillar.avro.serialization.core.identity.IdentityAvroSchemas
import sollecitom.libs.swissknife.protected_value.domain.ProtectedValue
import org.apache.avro.Schema

object CardTransactionAvroSchemas : AvroSchemaCatalogueTemplate("acme.transaction.card") {

    val cardAuthWasRequested: Schema by lazy { getSchema(name = "CardAuthWasRequested", dependencies = setOf(IdentityAvroSchemas.id, ProtectedValue.stringAvroSchema)) }
    val cardAuthWasProcessed: Schema by lazy { getSchema(name = "CardAuthWasProcessed", dependencies = setOf(IdentityAvroSchemas.id, ProtectedValue.stringAvroSchema)) }
    val card: Schema by lazy { getSchema(name = "CardTransaction", dependencies = setOf(cardAuthWasRequested, cardAuthWasProcessed)) }

    override val nestedContainers: Set<AvroSchemaContainer> = emptySet()

    override val all: Sequence<Schema> = sequenceOf(card, cardAuthWasRequested, cardAuthWasProcessed)
}
