package com.template.states

class CardDeckFactory {

    fun cardDeck() : List<Card> {
        val cardDeck = ArrayList<Card>()
        CardSuit.values().map { cardSuit ->
            CardValue.values().map { cardValue ->
                cardDeck.add(Card(cardSuit, cardValue))
            }
        }
        return cardDeck
    }

}