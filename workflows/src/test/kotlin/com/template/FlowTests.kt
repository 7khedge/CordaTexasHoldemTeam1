package com.template

import com.template.flows.DealInitiator
import com.template.flows.GameInitiator
import com.template.flows.GameResponder
import com.template.flows.PlaceBetInitiator
import com.template.states.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import sun.font.CoreMetrics
import kotlin.test.assertEquals

class FlowTests {
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
    )))
    private val player1 = network.createNode(CordaX500Name("player1", "", "GB"))
    private val player2 = network.createNode(CordaX500Name("player2", "", "GB"))
    private val dealer = network.createNode(CordaX500Name("dealer", "", "GB"))
    private val playerNodeMap = HashMap<Party, StartedMockNode>()

    init {
        listOf(player1, player2, dealer).forEach {
            it.registerInitiatedFlow(GameResponder::class.java)
        }
        playerNodeMap[player1.info.legalIdentities.first()] = player1
        playerNodeMap[player2.info.legalIdentities.first()] = player2
        playerNodeMap[dealer.info.legalIdentities.first()] = dealer
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
        val game = startGame()
        //Action
        assertEquals(RoundName.BLIND, game.round)

        val action = Bet(10, game.owner, ActionType.BIG_BLIND)
        val placeBetFlow = PlaceBetInitiator(action)
        val ownerNode = playerNodeMap[game.owner]
        if (ownerNode != null) {
            val nextFuture = ownerNode.startFlow(placeBetFlow)
            network.runNetwork()
            val nextGame = nextFuture.get().coreTransaction.outputStates.first() as Game
            assertEquals(10, nextGame.dealer.tableAccount)
            assertEquals(nextGame.findPlayer(game.owner).account, 90)
        }
    }

    private fun startGame(): Game {
        val startGameFlow = GameInitiator(listOf(player1.info.legalIdentities.first(),
                player2.info.legalIdentities.first()), dealer.info.legalIdentities.first())
        val future = dealer.startFlow(startGameFlow)

        //When (only required once)
        network.runNetwork()
        return future.get().coreTransaction.outputStates.first() as Game
    }

    @Test
    fun `play game`() {
        val blindGame = startGame()
        assertEquals(blindGame.owner, player1.info.legalIdentities.first())
        assertEquals(blindGame.round, RoundName.BLIND)

        val bigBlindBet = Bet(10, blindGame.owner, ActionType.BIG_BLIND)
        val blindGame2 = placeBet(bigBlindBet)
        assertEquals(blindGame2.owner, player2.info.legalIdentities.first())
        assertEquals(blindGame2.round, RoundName.BLIND)

        val littleBlindBet = Bet(5, blindGame2.owner, ActionType.LITTLE_BLIND)
        val dealGame = placeBet(littleBlindBet)
        assertEquals(dealGame.owner, dealer.info.legalIdentities.first())
        assertEquals(dealGame.round, RoundName.DEAL)


        val dealGame2 = playRound(dealGame, RoundName.DEAL, RoundName.FLOP)
        val flopGame = playRound(dealGame2, RoundName.FLOP, RoundName.TURN)
        val turnGame = playRound(flopGame, RoundName.TURN, RoundName.RIVER)
        val riverGame = playRound(turnGame, RoundName.RIVER, RoundName.REVEAL)
        assertEquals(riverGame.owner, dealer.info.legalIdentities.first())
    }

    private fun playRound(dealGame: Game, round: RoundName, nextRound: RoundName): Game {
        val dealGame2 = deal(dealGame.owner)
        assertEquals(dealGame2.owner, player1.info.legalIdentities.first())
        assertEquals(round, dealGame2.round)

        val bet1 = Bet(10, dealGame2.owner, ActionType.RAISE)
        val game1 = placeBet(bet1)
        assertEquals(game1.owner, player2.info.legalIdentities.first())
        assertEquals(round, game1.round)

        val bet2 = Bet(10, game1.owner, ActionType.MATCH)
        val game2 = placeBet(bet2)
        assertEquals(game2.owner, dealer.info.legalIdentities.first())
        assertEquals(nextRound, game2.round)
        return game2
    }

    private fun placeBet(bet: Bet): Game {
        val placeBetFlow = PlaceBetInitiator(bet)
        val ownerNode = playerNodeMap[bet.player]
        if (ownerNode != null) {
            val future = ownerNode.startFlow(placeBetFlow)
            network.runNetwork()
            return future.get().coreTransaction.outputStates.first() as Game
        }
        throw IllegalStateException("Could find game owner")
    }

    private fun deal(player: Party): Game {
        val placeBetFlow = DealInitiator()
        val ownerNode = playerNodeMap[player]
        if (ownerNode != null) {
            val future = ownerNode.startFlow(placeBetFlow)
            network.runNetwork()
            return future.get().coreTransaction.outputStates.first() as Game
        }
        throw IllegalStateException("Could find game owner")
    }


}