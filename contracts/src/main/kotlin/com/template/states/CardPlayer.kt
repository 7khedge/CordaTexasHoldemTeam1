package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
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


@BelongsToContract(TemplateContract::class)
data class Game(val cards : List<Card>,
                val players : List<Party>,
                val rounds : List<Round>,
                val dealer : Party,
                override val participants: List<AbstractParty> = listOf(dealer)) : ContractState

@CordaSerializable
enum class RoundName { BLIND, DEAL, FLOP, RIVER, REVEAL }

@BelongsToContract(TemplateContract::class)
data class Round(override val participants: List<AbstractParty> = listOf()) : ContractState

@BelongsToContract(TemplateContract::class)
data class Action(override val participants: List<AbstractParty> = listOf()) : ContractState
