package com.template.flows

import com.template.states.Game
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class TurnFlow(val game : Game) : FlowLogic<SignedTransaction>() {



    override fun call(): SignedTransaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
