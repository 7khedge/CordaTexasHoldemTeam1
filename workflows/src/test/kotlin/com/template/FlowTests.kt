package com.template

import com.template.flows.GameInitiator
import com.template.flows.GameResponder
import com.template.flows.Responder
import com.template.states.Game
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlowTests {
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
    fun `start game`() {
        //Given
        /*Start a game with two players and a dealer, a constructed flow returned*/
        val flow = GameInitiator(listOf(player1.info.legalIdentities.first(),
                player2.info.legalIdentities.first()), dealer.info.legalIdentities.first())
        val future = dealer.startFlow(flow)
        //When
        network.runNetwork()
        //Then
        /*execute constructed flow, the call method on the acceptor flow is executed*/
        /* calls verify on Game Contract - no rules for now */
        val stx = future.getOrThrow()
        val q = dealer.services.vaultService.queryBy(Game::class.java)

        assertEquals(1, q.states.size)
        assertEquals(stx.id, q.states.first().ref.txhash)
    }

    @Test
    fun `game next turn`() {
        //Given
        /*Start a game with two players and a dealer, a constructed flow returned*/
        val flow = GameInitiator(listOf(player1.info.legalIdentities.first(),
                player2.info.legalIdentities.first()), dealer.info.legalIdentities.first())
        val future = dealer.startFlow(flow)

        //When (only required once)
        network.runNetwork()
        var game = future.get().coreTransaction.outputStates.first() as Game



       /* game.linearId

        //Game state
        val flow2 = GameInitiator(listOf(player1.info.legalIdentities.first(),
                player2.info.legalIdentities.first()), dealer.info.legalIdentities.first())
        val future2 = player1.startFlow(flow2)
            player1 start
                         - play input state 10 units
                         - input game linearId
                         - input orignal game (lookup inside)
        places bug blind*/


    }

    @Test
    fun `play game`() {
       // game = GameInitiator() + execute
        // while (game.isNotFinished()) {
            // currentPlayer = game.owner
            // currentPlayerNode = getTheNode(currentPlayer)
            // nextFlow = NextStepFlow(game, action)
        // game = startFlow(nextFlow) .. future ...
        // }
    }
}