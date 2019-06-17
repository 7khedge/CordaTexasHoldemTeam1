@InitiatingFlow
@StartableByRPC
class FuzzyGameFlow(val command : FuzzyCommands) : FlowLogic<Unit>() {
    override fun call() {
        val currentGame = serviceHub.vaultService.queryBy(FuzzyGame::class.java).states.firstOrNull()
        when (currentGame) {
            null -> startGame(command)
            else -> continueGame(command, currentGame)
        }
    }

    private fun continueGame(command: FuzzyCommands, currentGame: StateAndRef<FuzzyGame>) {
        val nextGameState = currentGame.state.data.generateNextGameState(command)
        when (nextGameState) {
            null -> {
                // game has finished
                // TODO: create a transaction that consumes the game, and emit no new game state
                // and informs all participants so they can rock n roll their UI
            }
            else -> {
                // TODO: new game state transaction, with all parit
            }
        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun startGame(command: FuzzyCommands) {
        assert(command is FuzzyCommands.StartGame) { "no game is in progress but command is ${command.javaClass.simpleName}" }
        // TODO: create transaction
    }
}

sealed class FuzzyCommands {
    data class StartGame(val players: List<AbstractParty>) : FuzzyCommands()
    data class PlaceBet(val amount: Amount<Unit>)

}

data class FuzzyGame(override val owner: AbstractParty, override val participants: List<AbstractParty>) : OwnableState {
    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun generateNextGameState(command: FuzzyCommands) : FuzzyGame? {
        // core game logic
    }

}


//TODO: implement receiver flow - all transactions are passed to all players
//Suspendble

/*fun Game.foo() : Game {
    return this.copy()
}

fun <T : Number> Collection<T>.wibble() : Collection<String> {
    return this.map { it.toString() }
}


fun test() {
    val n = listOf(1, 2, 3, 4)
    val s = n.map { it.toString() }
    n.wibble()
   // s.wibble()

}*/
