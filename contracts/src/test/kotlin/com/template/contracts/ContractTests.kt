package com.template.contracts

import net.corda.core.utilities.getOrThrow
import net.corda.finance.flows.CashIssueAndPaymentFlow
import net.corda.finance.flows.CashPaymentFlow
import net.corda.node.services.Permissions.Companion.invokeRpc
import net.corda.node.services.Permissions.Companion.startFlow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.MockServices
import net.corda.testing.node.User
import net.corda.testing.node.internal.FINANCE_CORDAPPS
import org.junit.Ignore
import org.junit.Test

class ContractTests {
    private val ledgerServices = MockServices()

    @Test
    @Ignore
    fun shouldTest() {
        driver(DriverParameters(startNodesInProcess = true, cordappsForAllNodes = FINANCE_CORDAPPS)) {
            val aliceUser = User("aliceUser", "testPassword1", permissions = setOf(
                    startFlow<CashIssueAndPaymentFlow>(),
                    invokeRpc("vaultTrackBy")
            ))

            val bobUser = User("bobUser", "testPassword2", permissions = setOf(
                    startFlow<CashPaymentFlow>(),
                    invokeRpc("vaultTrackBy")
            ))

            val (alice, bob) = listOf(
                    startNode(providedName = ALICE_NAME, rpcUsers = listOf(aliceUser)),
                    startNode(providedName = BOB_NAME, rpcUsers = listOf(bobUser))
            ).map { it.getOrThrow() }
            //alice.dostuff(...)
        }
    }
}