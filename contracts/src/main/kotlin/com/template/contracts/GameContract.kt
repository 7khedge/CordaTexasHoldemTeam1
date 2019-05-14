package com.template.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class GameContract : Contract {

    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.GameContract"
    }

    override fun verify(tx: LedgerTransaction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class StartGame : Commands
        class FoldCommand : Commands
        class MatchCommand : Commands
        class RaiseCommand : Commands
        class CallCommand : Commands
    }

}