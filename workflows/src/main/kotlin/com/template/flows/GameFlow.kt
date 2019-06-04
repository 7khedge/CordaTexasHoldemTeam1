package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.GameContract
import com.template.states.CardDeckFactory
import com.template.states.Game
import com.template.states.RoundName
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class GameInitiator(val players : List<Party> , val dealer : Party) : FlowLogic<SignedTransaction>() {
    val cardDeckFactory  = CardDeckFactory()

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
        val currentGame = serviceHub.vaultService.queryBy(Game::class.java).states.firstOrNull()

        val gameAndCommand = if (null == currentGame) { startGame() }
                             else { continueGame(currentGame) }

        var game = gameAndCommand.first
        val txCommand = gameAndCommand.second
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(game, GameContract.ID)
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
        val sessions = game.players.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in all players and dealer vaults.
        return subFlow(FinalityFlow(fullySignedTx, sessions, FINALISING_TRANSACTION.childProgressTracker()))
    }

    private fun continueGame(currentGameStateRef: StateAndRef<Game>) : Pair<Game, Command<GameContract.Commands.NextTurn>> {
        val currentGame = currentGameStateRef.state.data
        // Generate an unsigned transaction.
        var game = Game(cardDeckFactory.cardDeck(), players, dealer, currentGame.getNextRound(), emptyList(), dealer)
        //need to add dealer signer
        val txCommand = Command(GameContract.Commands.NextTurn(), game.players.map { it.owningKey } + dealer.owningKey)
        return Pair(game, txCommand)
    }

    private fun startGame(): Pair<Game, Command<GameContract.Commands.StartGame>> {
        // Generate an unsigned transaction.
        var game = Game(cardDeckFactory.cardDeck(), players, dealer, RoundName.BLIND, emptyList(), dealer)
        //need to add dealer signer
        val txCommand = Command(GameContract.Commands.StartGame(), game.players.map { it.owningKey } + dealer.owningKey)
        return Pair(game, txCommand)
    }

}

@InitiatedBy(GameInitiator::class)
class GameResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction." using (output is Game)
                val iou = output as Game
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

