package sollecitom.examples.pulsar.messaging.transaction.avro.trade

import sollecitom.libs.swissknife.avro.schema.catalogue.test.utils.SchemaContainerTestSpecification
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class TradeTransactionAvroSchemasTests : SchemaContainerTestSpecification {

    override val candidate = TradeTransactionAvroSchemas
}