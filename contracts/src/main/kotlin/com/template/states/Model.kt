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
enum class RoundName { BLIND, DEAL, FLOP, RIVER, REVEAL }

@CordaSerializable
enum class ActionType { FOLD, MATCH, RAISE, CALL }

data class Round(val name : RoundName,
                 val players : List<Party>,
                 val actions : List<Action>)

data class Action(
        val player : Party,
        val action : ActionType,
        val amount : List<StateRef>)


@BelongsToContract(GameContract::class)
data class Game(val cards : List<Card>,
                val players : List<Party>,
                val dealer : Party,
                val round : RoundName,
                val tableCards : List<Card>,
                override val owner: AbstractParty,
                override val participants: List<AbstractParty> =  players + dealer,
                override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState, OwnableState {

    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        return CommandAndState( GameContract.Commands.NextTurn(), this.copy(owner = newOwner))
    }

    fun getNextRound() : RoundName {
        if( round == RoundName.REVEAL )
            throw IllegalStateException("Game cannot progress beyond REVEAL")
        return RoundName.values()[round.ordinal + 1]
    }

}