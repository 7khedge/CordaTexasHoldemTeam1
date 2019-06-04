package com.template.contracts

import com.template.states.Game
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class GameContract : Contract {

    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.GameContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class StartGame : Commands
        class NextTurn : Commands
        class EndGame : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val jobInputs = tx.inputsOfType<Game>()
        val jobOutputs = tx.outputsOfType<Game>()
        val jobCommand = tx.commandsOfType<Commands>().single()

        when(jobCommand.value){
            is Commands.NextTurn -> requireThat {






            }

        }
    }

}