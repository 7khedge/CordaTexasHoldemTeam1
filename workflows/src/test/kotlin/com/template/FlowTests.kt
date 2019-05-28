package com.template

import com.template.flows.GameInitiator
import com.template.flows.GameResponder
import com.template.flows.Responder
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class FlowTests {
     //CardDeckFactory()
     //Game(cardDeckFactory.cardDeck(), listOf<Party>(player1.party,player2.party,player3.party,player4.party), dealer.party)
    
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
        TestCordapp.findCordapp("com.template.contracts"),
        TestCordapp.findCordapp("com.template.flows")
    )))
    private val player1 = network.createNode(CordaX500Name("player1", "", "GB"))
    private val player2 = network.createNode(CordaX500Name("player2", "", "GB"))
    private val dealer = network.createNode(CordaX500Name("dealer", "", "GB"))

    init {
        listOf(player1, player2, dealer).forEach {
            it.registerInitiatedFlow(GameResponder::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `dummy test`() {
        var flow = GameInitiator(listOf(player1.info.legalIdentities.first(),
                player2.info.legalIdentities.first()), dealer.info.legalIdentities.first())
        var future = dealer.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }
}