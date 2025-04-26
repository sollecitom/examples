package sollecitom.examples.pulsar.messaging.transaction

import sollecitom.libs.pillar.protected_value.domain.ProtectedString
import sollecitom.libs.swissknife.core.domain.identity.Id

sealed class Transaction(val id: Id, val amount: Double, val tag: String) {

    sealed class CardTransaction(val cardNumber: ProtectedString, id: Id, amount: Double, tag: String) : Transaction(id, amount, tag) {

        class CardAuthWasRequested(cardNumber: ProtectedString, id: Id, amount: Double, tag: String) : CardTransaction(cardNumber, id, amount, tag) {

            override fun toString() = "CardAuthWasRequested(cardNumber='$cardNumber', id=$id)"

            companion object
        }

        class CardAuthWasProcessed(cardNumber: ProtectedString, id: Id, amount: Double, tag: String) : CardTransaction(cardNumber, id, amount, tag) {

            constructor(request: CardAuthWasRequested) : this(request.cardNumber, request.id, request.amount, request.tag)

            override fun toString() = "CardAuthWasProcessed(cardNumber='$cardNumber', id=$id)"

            companion object
        }

        companion object
    }

    sealed class TradeTransaction(val quantity: Int, val assetId: String, val pricePerShare: Double, id: Id, tag: String) : Transaction(id, pricePerShare * quantity, tag) {

        class TradeOrderWasRequested(id: Id, quantity: Int, assetId: String, pricePerShare: Double, tag: String) : TradeTransaction(quantity, assetId, pricePerShare, id, tag) {

            override fun toString() = "TradeOrderWasRequested(quantity=$quantity, assetId='$assetId', pricePerShare='$pricePerShare' id=$id)"

            companion object
        }

        companion object
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Transaction
        return id == other.id
    }

    final override fun hashCode() = id.hashCode()

    override fun toString() = "Transaction(amount=$amount, tag=$tag, id=$id)"

    companion object
}

fun Transaction.CardTransaction.CardAuthWasRequested.andThenProcessed(): Transaction.CardTransaction.CardAuthWasProcessed = Transaction.CardTransaction.CardAuthWasProcessed(cardNumber = cardNumber, id = id, amount = amount, tag = tag)
