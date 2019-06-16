package com.template.states

import com.template.contracts.GameContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.lang.IllegalStateException

//Use CashPaymentFlow
//Use Units for now
@CordaSerializable
enum class CardSuit { HEART, CLUB, SPADE, DIAMOND}

@CordaSerializable
enum class CardValue { ACE,  TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

@CordaSerializable
data class Card(val suit : CardSuit, val value: CardValue)

@CordaSerializable
enum class RoundName { BLIND, DEAL, FLOP, TURN, RIVER, REVEAL, END }

@CordaSerializable
enum class ActionType { BIG_BLIND, LITTLE_BLIND, FOLD, MATCH, RAISE, CALL }

@CordaSerializable
data class Bet(val amount : Int,
               val player: Party,
               val actionType: ActionType)

@CordaSerializable
data class Dealer(
            val party : Party,
            val tableAccount : Int = 0,
            val tableCards : List<Card> = emptyList(),
            val deck : List<Card> = CardDeckFactory().cardDeck())

@CordaSerializable
data class Player(
        val party : Party,
        val cards : List<Card> = emptyList(),
        val account : Int = 100)

@BelongsToContract(GameContract::class)
data class Game(val dealer : Dealer,
                val players : List<Player>,
                val round : RoundName,
                override val owner: Party,
                override val participants: List<Party> =  players.map { it.party } + dealer.party,
                override val linearId: UniqueIdentifier = UniqueIdentifier(),
                val bigBlindAmount : Int = 10,
                val littleBlindAmount : Int = 5)  : LinearState, OwnableState {

    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        return CommandAndState( GameContract.Commands.PlaceBet(), this.copy( owner = newOwner as Party))
    }

    fun allExceptOwner()  = participants.filter { party -> party != owner }

    fun hasFinished() :  Boolean {
        return round == RoundName.END
    }

    fun getNextRound() : RoundName {
        if( round == RoundName.END )
            throw IllegalStateException("Game cannot progress beyond END")
        return RoundName.values()[round.ordinal + 1]
    }

    fun getNextOwner() : Party {
        if ( owner == dealer.party)
            return players[0].party
        val partyList = players.map { it.party }
        val indexOfNextPlayer = partyList.indexOf(owner) + 1
        if ( indexOfNextPlayer == partyList.size )
            return dealer.party
        return partyList[indexOfNextPlayer]
    }

    /**
     * Deal two cards to each player
     */
    fun deal(): Game {
        val dealingDeck = dealer.deck.toMutableList()
        val newPlayers = players.map { it.copy(cards = listOf(dealingDeck.removeAt(0), dealingDeck.removeAt(0))) }
        return copy(dealer = dealer.copy(deck = dealingDeck),
                    players = newPlayers,
                    round = RoundName.FLOP)
    }

    /**
     * Deal three to the table
     */
    fun flop(): Game{
        val dealingDeck = dealer.deck.toMutableList()
        val tableCards = listOf(dealingDeck.removeAt(0),
                dealingDeck.removeAt(0),
                dealingDeck.removeAt(0))
        return copy(dealer = dealer.copy(deck = dealingDeck, tableCards = tableCards))
    }

    /**
     * Add Card to table
     */
    fun turn(): Game{
        return addCardToTable()
    }

    /**
     * Add Card to table
     */
    fun river(): Game{
        return addCardToTable()
    }

    private fun addCardToTable(): Game {
        val dealingDeck = dealer.deck.toMutableList()
        val tableCards = dealer.tableCards + dealingDeck.removeAt(0)
        return copy(dealer = dealer.copy(deck = dealingDeck, tableCards = tableCards))
    }
}

/*fun Game.foo() : Game {
    return this.copy()
}

fun <T : Number> Collection<T>.wibble() : Collection<String> {
    return this.map { it.toString() }
}


fun test() {
    val n = listOf(1, 2, 3, 4)
    val s = n.map { it.toString() }
    n.wibble()
   // s.wibble()

}*/

