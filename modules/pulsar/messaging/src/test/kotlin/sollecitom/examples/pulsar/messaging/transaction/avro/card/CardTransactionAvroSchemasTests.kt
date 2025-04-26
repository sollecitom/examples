package sollecitom.examples.pulsar.messaging.transaction.avro.card

import sollecitom.libs.swissknife.avro.schema.catalogue.test.utils.SchemaContainerTestSpecification
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class CardTransactionAvroSchemasTests : SchemaContainerTestSpecification {

    override val candidate = CardTransactionAvroSchemas
}