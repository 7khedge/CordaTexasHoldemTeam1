package com.template.contracts

import com.template.states.CardDeckFactory
import com.template.states.Game
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import org.junit.Test

class ContractTests {
    private val cardDeckFactory  = CardDeckFactory()
    private val ledgerServices = MockServices()
    private val player1 = TestIdentity(CordaX500Name("player1", "", "GB"))
    private val player2 = TestIdentity(CordaX500Name("player2", "", "GB"))
    private val player3 = TestIdentity(CordaX500Name("player3", "", "GB"))
    private val player4 = TestIdentity(CordaX500Name("player4", "", "GB"))
    private val dealer = TestIdentity(CordaX500Name("dealer", "", "GB"))
    private val unStartedGame  = Game(cardDeckFactory.cardDeck(), listOf<Party>(player1.party,player2.party,player3.party,player4.party), dealer.party)


    @Test
    fun `GameContract should Start with at least four participants`() {



    }
}