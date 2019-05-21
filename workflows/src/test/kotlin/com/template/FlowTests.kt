package com.template

import com.template.flows.GameInitiator
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
     private val player1 = TestIdentity(CordaX500Name("player1", "", "GB"))
    private val player2 = TestIdentity(CordaX500Name("player2", "", "GB"))
    private val player3 = TestIdentity(CordaX500Name("player3", "", "GB"))
    private val player4 = TestIdentity(CordaX500Name("player4", "", "GB"))
    private val dealer = TestIdentity(CordaX500Name("dealer", "", "GB"))
    private val players = listOf(player1.party,player2.party,player3.party,player4.party)

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
        TestCordapp.findCordapp("com.template.contracts"),
        TestCordapp.findCordapp("com.template.flows")
    )))
    private val a = network.createNode()
    private val b = network.createNode()

    init {
        listOf(a, b).forEach {
            it.registerInitiatedFlow(Responder::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `dummy test`() {
        var flow = GameInitiator(players, dealer.party)
        var future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }
}