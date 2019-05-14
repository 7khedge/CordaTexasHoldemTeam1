package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

//Use CashPaymentFlow


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


@BelongsToContract(TemplateContract::class)
data class Game(val cards : List<Card>,
                val rounds : List<Round>,
                val players : List<Party>,
                val dealer : Party,
                override val participants: List<AbstractParty> =  players + dealer) : ContractState


data class Round(val name : RoundName,
                 val players : List<Party>,
                 val actions : List<Action>)

data class Action(
        val player : Party,
        val action : ActionType,
        val amount : List<StateRef>)
