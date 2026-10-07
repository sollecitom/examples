package sollecitom.examples.pulsar.messaging.transaction

import assertk.assertThat
import assertk.assertions.isEqualTo
import sollecitom.examples.pulsar.messaging.transaction.Transaction.TradeTransaction.TradeOrderWasRequested
import sollecitom.libs.swissknife.core.domain.identity.StringId
import sollecitom.libs.swissknife.test.utils.assertions.failedThrowing
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.math.BigDecimal

@TestInstance(PER_CLASS)
class TransactionTests {

    @Test
    fun `money amounts are normalised to two decimal places`() {

        val order = TradeOrderWasRequested(id = StringId("order-1"), quantity = 2, assetId = "ACME", pricePerShare = BigDecimal("9.5"), tag = "trade")

        assertThat(order.pricePerShare).isEqualTo(BigDecimal("9.50"))
        assertThat(order.amount).isEqualTo(BigDecimal("19.00"))
    }

    @Test
    fun `money amounts that would lose precision are rejected`() {

        val result = runCatching { TradeOrderWasRequested(id = StringId("order-1"), quantity = 1, assetId = "ACME", pricePerShare = BigDecimal("1.234"), tag = "trade") }

        assertThat(result).failedThrowing<ArithmeticException>()
    }
}
