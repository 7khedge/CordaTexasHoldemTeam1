package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.GameContract
import com.template.states.Bet
import com.template.states.Game
import com.template.states.Player
import com.template.states.RoundName
import com.template.states.RoundName.*
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import java.lang.IllegalStateException

// *********
// * Flows *
// *********

@InitiatingFlow
@StartableByRPC
class PlaceBetInitiator(private val action : Bet) : FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object GENERATING_TRANSACTION : Step("Generating transaction based on new IOU.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call() : SignedTransaction {
        // Obtain a reference to the notary we want to use.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val currentGame = serviceHub.vaultService.queryBy(Game::class.java).states.lastOrNull() ?: throw IllegalArgumentException("Could find game in ledger")


        val (newGame, txCommand) = applyBet(currentGame, action)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(newGame, GameContract.ID)
                .addCommand(txCommand)

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        txBuilder.verify(serviceHub)

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 4.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to other players, and receive it back with their signature.
        val sessions = currentGame.state.data.allExceptOwner().map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in all players and dealer vaults.
        return subFlow(FinalityFlow(fullySignedTx, sessions, FINALISING_TRANSACTION.childProgressTracker()))
    }

    private fun applyBet(currentGameStateRef: StateAndRef<Game>, bet: Bet) : Pair<Game, Command<GameContract.Commands.PlaceBet>> {
        val currentGame = currentGameStateRef.state.data
        val txCommand = Command(GameContract.Commands.PlaceBet(), currentGame.players.map { it.party.owningKey } + currentGame.dealer.party.owningKey)
        return Pair(updateGame(bet, currentGame), txCommand)
    }

    private fun updateGame(bet : Bet, currentGame : Game ) : Game {
        val roundBets = addRoundBet(currentGame, bet)
        val roundName = getRound(roundBets, currentGame)
        return currentGame.copy(
                round = roundName,
                owner = currentGame.getNextOwner(),
                players = updatePlayerAccount(currentGame, bet),
                dealer = currentGame.dealer.copy(tableAccount = currentGame.dealer.tableAccount + bet.amount),
                roundBets = roundBets)
    }

    private fun getRound(roundBets: Map<RoundName, Map<Player, Bet>>, currentGame: Game): RoundName {
        return when(currentGame.round){
            BLIND -> leaveBlind(roundBets[currentGame.round])
            else -> leaveRound(roundBets[currentGame.round], currentGame.players.size, currentGame.round)
        }
    }

    private fun leaveRound(roundBets: Map<Player, Bet>?, expectedBets : Int, round : RoundName): RoundName {
        if(roundBets != null){
            if( roundBets.size == expectedBets)
                return getNextRound(round)
        }
        return round
    }

    private fun leaveBlind(roundBets : Map<Player, Bet>? ): RoundName {
        if (roundBets != null) {
            if ( roundBets.size == 2 )
                return DEAL
        }
        return BLIND
    }

    private fun getNextRound(round : RoundName) : RoundName {
        if( round == REVEAL )
            throw IllegalStateException("Game cannot progress beyond END")
        return values()[round.ordinal + 1]
    }

    private fun updatePlayerAccount(currentGame: Game, bet: Bet): List<Player> {
        val toMutableList = currentGame.players.toMutableList()
        val player = currentGame.findPlayer(currentGame.owner)
        val newPlayer = player.copy(account = player.account - bet.amount)
        val indexOf = toMutableList.indexOf(player)
        toMutableList[indexOf] = newPlayer
        return toMutableList.toList()
    }

    private fun addRoundBet(currentGame : Game, bet : Bet) : Map<RoundName,Map<Player,Bet>> {
        val playerBet : MutableMap<Player, Bet> = (currentGame.roundBets[currentGame.round] ?: error("Cannot find bets for ${currentGame.round}")).toMutableMap()
        playerBet[currentGame.findPlayer(party = currentGame.owner)] = bet
        return currentGame.roundBets.plus(Pair(currentGame.round, playerBet))
    }

}

@InitiatedBy(PlaceBetInitiator::class)
class PlaceBetResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //TODO: Implement PlaceBetResponder Flow
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}


