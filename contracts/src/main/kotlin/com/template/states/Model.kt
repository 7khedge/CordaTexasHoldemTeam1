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
enum class RoundName { BLIND, DEAL, FLOP, RIVER, REVEAL, END }

@CordaSerializable
enum class ActionType { BIG_BLIND, LITTLE_BLIND, FOLD, MATCH, RAISE, CALL }

@CordaSerializable
data class Action(
        val player : Party,
        val actionType : ActionType,
        val amount : Int)

@BelongsToContract(GameContract::class)
data class Game(val cards : List<Card>,
                val players : List<Party>,
                val dealer : Party,
                val round : RoundName,
                val roundActions: Map<RoundName,List<Action>>,
                val tableCards : List<Card>,
                override val owner: Party,
                val tableAccount : Int = 0,
                override val participants: List<Party> =  players + dealer,
                override val linearId: UniqueIdentifier = UniqueIdentifier(),
                val bigBlindAmount : Int = 10,
                val littleBlindAmount : Int = 5)  : LinearState, OwnableState {

    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        return CommandAndState( GameContract.Commands.NextTurn(), this.copy( owner = newOwner as Party))
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
        if ( owner == dealer)
            return players[0]

        val indexOfNextPlayer = players.indexOf(owner) + 1

        if ( indexOfNextPlayer == players.size )
            return dealer

        return players[indexOfNextPlayer]
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

