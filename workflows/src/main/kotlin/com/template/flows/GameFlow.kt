package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.GameContract
import com.template.states.CardDeckFactory
import com.template.states.Game
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class GameInitiator(val players : List<Party> , val dealer : Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()
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


    @Suspendable
    override fun call() {
        // Obtain a reference to the notary we want to use.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]


        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION
        // Generate an unsigned transaction.
        var game = Game(cardDeckFactory.cardDeck(), players, dealer)
        //need to add dealer signer
        val txCommand = Command(GameContract.Commands.CreateGame(), game.players.map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(game, GameContract.ID)
                .addCommand(txCommand)

    }
}

@InitiatedBy(GameInitiator::class)
class GameResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
    }
}