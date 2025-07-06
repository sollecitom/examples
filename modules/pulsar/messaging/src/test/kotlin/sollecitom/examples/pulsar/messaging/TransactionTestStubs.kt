package sollecitom.examples.pulsar.messaging

import sollecitom.libs.pillar.messaging.conventions.AcmeMessagePropertyNames
import sollecitom.libs.pillar.protected_value.domain.ProtectedString
import sollecitom.libs.swissknife.core.domain.identity.Id
import sollecitom.libs.swissknife.core.domain.text.Name
import sollecitom.libs.swissknife.core.test.utils.text.random
import sollecitom.libs.swissknife.core.utils.RandomGenerator
import sollecitom.libs.swissknife.core.utils.TimeGenerator
import sollecitom.libs.swissknife.core.utils.UniqueIdGenerator
import sollecitom.libs.swissknife.correlation.core.domain.context.InvocationContext
import sollecitom.libs.swissknife.ddd.domain.Event
import sollecitom.libs.swissknife.messaging.domain.event.converter.EventMessageConverterTemplate
import sollecitom.libs.swissknife.messaging.domain.message.converter.MessageConverter
import sollecitom.examples.pulsar.messaging.transaction.Transaction
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasProcessed
import sollecitom.examples.pulsar.messaging.transaction.Transaction.CardTransaction.CardAuthWasRequested
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import sollecitom.examples.pulsar.messaging.transaction.TransactionEvent
import kotlin.time.Instant
import sollecitom.libs.swissknife.core.utils.nextInt
import kotlin.random.nextInt

context(ids: UniqueIdGenerator, time: TimeGenerator)
fun Transaction.asEvent(invocationContext: InvocationContext<*>, parentEvent: Event.Reference? = null, originatingEvent: Event.Reference? = null, id: Id = ids.newId.external(), timestamp: Instant = time.now()) = TransactionEvent(transaction = this, id = id, timestamp = timestamp, context = Event.Context(invocation = invocationContext, parent = parentEvent, originating = originatingEvent))

context(random: RandomGenerator, ids: UniqueIdGenerator)
fun cardAuthWasRequested(cardNumber: ProtectedString, tag: String = Name.random().value, amount: Double = random.random.nextDouble(0.1, 1000.0), id: Id = ids.newId.external()) = CardAuthWasRequested(cardNumber, id, amount, tag)

context(random: RandomGenerator, ids: UniqueIdGenerator)
fun cardAuthWasProcessed(cardNumber: ProtectedString, tag: String = Name.random().value, amount: Double = random.random.nextDouble(0.1, 1000.0), id: Id = ids.newId.external()) = CardAuthWasProcessed(cardNumber, id, amount, tag)

context(random: RandomGenerator, ids: UniqueIdGenerator)
fun tradeOrderWasRequested(assetId: String = Name.random().value, tag: String = Name.random().value, quantity: Int = random.nextInt(1..300), pricePerShare: Double = random.random.nextDouble(0.1, 100.0), id: Id = ids.newId.external()) = TradeOrderWasRequested(id, quantity, assetId, pricePerShare, tag)

val TransactionEvent.Companion.messageConverter: MessageConverter<TransactionEvent> get() = TransactionEventMessageConverter

private object TransactionEventMessageConverter : EventMessageConverterTemplate<TransactionEvent>(propertyNames = AcmeMessagePropertyNames) {

    override fun key(event: TransactionEvent) = event.transaction.tag
}