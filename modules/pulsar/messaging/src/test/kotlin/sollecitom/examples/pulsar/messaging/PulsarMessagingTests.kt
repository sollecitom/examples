package sollecitom.examples.pulsar.messaging

import assertk.assertThat
import sollecitom.examples.pulsar.messaging.transaction.Transaction
import sollecitom.examples.pulsar.messaging.transaction.TransactionEvent
import sollecitom.examples.pulsar.messaging.transaction.andThenProcessed
import sollecitom.examples.pulsar.messaging.transaction.avro.avroSerde
import sollecitom.libs.swissknife.avro.serialization.utils.AvroSerde
import sollecitom.libs.swissknife.core.domain.text.Name
import sollecitom.libs.swissknife.core.test.utils.testProvider
import sollecitom.libs.swissknife.core.utils.CoreDataGenerator
import sollecitom.libs.swissknife.correlation.core.domain.context.InvocationContext
import sollecitom.libs.swissknife.correlation.core.test.utils.context.authenticated
import sollecitom.libs.swissknife.cryptography.domain.factory.CryptographicOperations
import sollecitom.libs.swissknife.cryptography.domain.key.generator.CryptographicKeyGenerator
import sollecitom.libs.swissknife.cryptography.domain.key.generator.newAesKey
import sollecitom.libs.swissknife.cryptography.domain.symmetric.encryption.aes.AES
import sollecitom.libs.swissknife.cryptography.implementation.bouncycastle.bouncyCastle
import sollecitom.libs.swissknife.ddd.test.utils.descendsFrom
import sollecitom.libs.swissknife.ddd.test.utils.isOriginating
import sollecitom.libs.swissknife.ddd.test.utils.originatesFrom
import sollecitom.libs.swissknife.logger.core.LoggingLevel
import sollecitom.libs.swissknife.logging.standard.configuration.configureLogging
import sollecitom.libs.swissknife.messaging.domain.message.consumer.MessageConsumer
import sollecitom.libs.swissknife.messaging.domain.message.consumer.messages
import sollecitom.libs.swissknife.messaging.domain.message.producer.MessageProducer
import sollecitom.libs.swissknife.messaging.domain.topic.Topic
import sollecitom.libs.swissknife.messaging.test.utils.message.MessagingTestSpecification
import sollecitom.libs.swissknife.messaging.test.utils.message.matches
import sollecitom.libs.swissknife.messaging.test.utils.message.outboundMessage
import sollecitom.libs.swissknife.messaging.test.utils.topic.create
import sollecitom.libs.swissknife.protected_value.domain.ProtectedValueFactory
import sollecitom.libs.swissknife.protected_value.implementation.bouncy_castle.aes256WithCTR
import sollecitom.libs.swissknife.pulsar.avro.serialization.asPulsarSchema
import sollecitom.libs.swissknife.pulsar.messaging.adapter.ensureTopicExists
import sollecitom.libs.swissknife.pulsar.messaging.adapter.newMessageConsumer
import sollecitom.libs.swissknife.pulsar.messaging.adapter.newMessageProducer
import sollecitom.libs.swissknife.pulsar.messaging.adapter.topics
import sollecitom.libs.swissknife.pulsar.test.utils.admin
import sollecitom.libs.swissknife.pulsar.test.utils.client
import sollecitom.libs.swissknife.pulsar.test.utils.newPulsarContainer
import sollecitom.libs.swissknife.test.utils.execution.utils.test
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.client.api.SubscriptionInitialPosition
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import sollecitom.libs.swissknife.core.domain.identity.factory.invoke
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@TestInstance(PER_CLASS)
class PulsarMessagingTests : MessagingTestSpecification, CoreDataGenerator by CoreDataGenerator.testProvider, CryptographicKeyGenerator {

    init {
        configureLogging(defaultMinimumLoggingLevel = LoggingLevel.INFO)
    }

    override val cryptographicOperations = CryptographicOperations.bouncyCastle(random = secureRandom)
    private val pulsar = newPulsarContainer()
    private val pulsarClient by lazy { pulsar.client() }
    private val pulsarAdmin by lazy { pulsar.admin() }
    override val timeout: Duration get() = 30.seconds
    private val key = newAesKey(variant = AES.Variant.AES_256)
    private val factory = ProtectedValueFactory.aes256WithCTR { key }

    @BeforeAll
    fun beforeAll() = pulsar.start()

    @AfterAll
    fun afterAll() = pulsar.stop()

    @Test
    fun `sending and receiving a message in Avro`() = test(timeout = timeout) {

        val topic = newTopic()
        val clearTextCreditCardNumber = "1234 1234 5678 123498"
        val userId = newId.external()
        val protectedCreditCardNumber = factory.protectValue(clearTextCreditCardNumber, "credit card number".let(::Name), userId, String::toByteArray)
        val cardAuthWasRequested = cardAuthWasRequested(protectedCreditCardNumber)
        val cardAuthWasProcessed = cardAuthWasRequested.andThenProcessed()
        val tradeOrderWasRequested = tradeOrderWasRequested()
        val consumer = newMessageConsumer(serde = Transaction.avroSerde, topic = topic)
        val producer = newMessageProducer(serde = Transaction.avroSerde, topic = topic)
        val outboundMessages = listOf(
            outboundMessage(key = "a key", value = cardAuthWasRequested),
            outboundMessage(key = "a key", value = cardAuthWasProcessed),
            outboundMessage(key = "a key", value = tradeOrderWasRequested)
        )

        outboundMessages.forEach { producer.produce(message = it) }
        val receivedMessages = consumer.messages.take(3).toList()

        receivedMessages.forEachIndexed { index, receivedMessage -> assertThat(receivedMessage).matches(outboundMessages[index]) }
    }

    @Test
    fun `sending and receiving events in Avro with a message converter`() = test(timeout = timeout) {

        val topic = newTopic()
        val clearTextCreditCardNumber = "1234 1234 5678 123498"
        val userId = newId.external()
        val protectedCreditCardNumber = factory.protectValue(clearTextCreditCardNumber, "credit card number".let(::Name), userId, String::toByteArray)
        val cardAuthWasRequested = cardAuthWasRequested(protectedCreditCardNumber)
        val cardAuthWasProcessed = cardAuthWasRequested.andThenProcessed()
        val tradeOrderWasRequested = tradeOrderWasRequested()
        val invocationContext = InvocationContext.authenticated()
        val event1 = cardAuthWasRequested.asEvent(invocationContext)
        val event2 = cardAuthWasProcessed.asEvent(invocationContext, parentEvent = event1.reference, originatingEvent = event1.reference)
        val event3 = tradeOrderWasRequested.asEvent(invocationContext)
        val consumer = newMessageConsumer(serde = TransactionEvent.avroSerde, topic = topic)
        val producer = newMessageProducer(serde = TransactionEvent.avroSerde, topic = topic)
        val outboundMessages = listOf(event1, event2, event3).map(TransactionEvent.messageConverter::toOutboundMessage)

        outboundMessages.forEach { producer.produce(message = it) }
        val receivedMessages = consumer.messages.take(3).toList()

        receivedMessages.forEachIndexed { index, receivedMessage -> assertThat(receivedMessage).matches(outboundMessages[index]) }
        assertThat(receivedMessages[1].value).descendsFrom(receivedMessages[0].value)
        assertThat(receivedMessages[1].value).originatesFrom(receivedMessages[0].value)
        assertThat(receivedMessages[2].value).isOriginating()
    }

    override fun newTopic(tenant: Name, namespaceName: Name, namespace: Topic.Namespace?, name: Name, persistent: Boolean): Topic = Topic.create(persistent, tenant, namespaceName, namespace, name).also { pulsarAdmin.ensureTopicExists(topic = it, isAllowAutoUpdateSchema = true) }

    override fun newMessageProducer(topic: Topic, name: String): MessageProducer<String> = pulsarClient.newMessageProducer(name.let(::Name), topic, pulsarClient.newProducer(Schema.STRING).enableBatching(false))

    override fun newMessageConsumer(topics: Set<Topic>, subscriptionName: String, name: String): MessageConsumer<String> = pulsarClient.newMessageConsumer(topics) { pulsarClient.newConsumer(Schema.STRING).topics(it).subscriptionName(subscriptionName).subscriptionInitialPosition(SubscriptionInitialPosition.Earliest).consumerName(name) }

    private fun <VALUE : Any> newMessageProducer(serde: AvroSerde<VALUE>, topic: Topic, name: String = newId().stringValue): MessageProducer<VALUE> = pulsarClient.newMessageProducer(name.let(::Name), topic, pulsarClient.newProducer(serde.asPulsarSchema()).enableBatching(false))

    private fun <VALUE : Any> newMessageConsumer(serde: AvroSerde<VALUE>, topics: Set<Topic>, subscriptionName: String = newId().stringValue, name: String = newId().stringValue): MessageConsumer<VALUE> = pulsarClient.newMessageConsumer(topics) { pulsarClient.newConsumer(serde.asPulsarSchema()).topics(it).subscriptionName(subscriptionName).subscriptionInitialPosition(SubscriptionInitialPosition.Earliest).consumerName(name) }

    private fun <VALUE : Any> newMessageConsumer(serde: AvroSerde<VALUE>, topic: Topic, subscriptionName: String = newId().stringValue, name: String = newId().stringValue): MessageConsumer<VALUE> = newMessageConsumer(serde, setOf(topic), subscriptionName, name)
}