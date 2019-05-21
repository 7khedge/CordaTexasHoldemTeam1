package com.template.contracts

import com.google.common.collect.ImmutableList
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test

class FlowTests {

    private var network: MockNetwork? = null
    private var nodeA: StartedMockNode? = null
    private var nodeB: StartedMockNode? = null

    @Before
    fun setup() {
        network = MockNetwork(ImmutableList.of("java_bootcamp"))
        nodeA = network!!.createPartyNode(null)
        nodeB = network!!.createPartyNode(null)
        network!!.runNetwork()
    }

    @After
    fun tearDown() {
        network!!.stopNodes()
    }


    @Test
    fun name() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


    }

    //    @Test
//    public void transactionConstructedByFlowUsesTheCorrectNotary() throws Exception {
//        TokenIssueFlow flow = new TokenIssueFlow(nodeB.getInfo().getLegalIdentities().get(0), 99);
//        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
//        network.runNetwork();
//        SignedTransaction signedTransaction = future.get();
//
//        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
//        TransactionState output = signedTransaction.getTx().getOutputs().get(0);
//
//        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), output.getNotary());
//    }
}