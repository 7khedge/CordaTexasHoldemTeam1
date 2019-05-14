package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class CardSuit { HEART, CLUB, SPADE, DIAMOND}

@CordaSerializable
enum class CardValue { ACE,  TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

@CordaSerializable
data class Card(val suit : CardSuit, val value: CardValue)

@BelongsToContract(TemplateContract::class)
data class GameAccount(var balance : Double,
                       val owner : Party,
                       override val participants: List<AbstractParty> = listOf(owner)) : ContractState

@BelongsToContract(TemplateContract::class)
data class GamePlayer(var hand : List<Card>,
                      val owner : Party,
                      override val participants: List<AbstractParty> = listOf(owner)) : ContractState

@BelongsToContract(TemplateContract::class)
data class Game(val players : List<Card>,
                val dealer : Party,
                override val participants: List<AbstractParty> = listOf(dealer)) : ContractState
