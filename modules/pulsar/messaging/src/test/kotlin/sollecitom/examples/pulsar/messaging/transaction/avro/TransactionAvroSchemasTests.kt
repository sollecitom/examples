package sollecitom.examples.pulsar.messaging.transaction.avro

import sollecitom.libs.swissknife.avro.schema.catalogue.test.utils.SchemaContainerTestSpecification
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class TransactionAvroSchemasTests : SchemaContainerTestSpecification {

    override val candidate = TransactionAvroSchemas
}