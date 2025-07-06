package sollecitom.examples.pulsar.messaging.transaction

import sollecitom.libs.swissknife.core.domain.identity.Id
import sollecitom.libs.swissknife.core.domain.text.Name
import sollecitom.libs.swissknife.core.domain.versioning.IntVersion
import sollecitom.libs.swissknife.ddd.domain.Event
import sollecitom.libs.swissknife.ddd.domain.Happening
import kotlin.time.Instant

data class TransactionEvent(val transaction: Transaction, override val id: Id, override val timestamp: Instant, override val context: Event.Context) : Event {

    override val type get() = Companion.type

    companion object {
        val type = Happening.Type("transaction-happened".let(::Name), 1.let(::IntVersion))
    }
}