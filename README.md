# Poker game in Corda


Texas Holdem Poker Model

Models

Resources
* DeckOfCards
* Chips
* PlayerHand
* TableCards
* BettingPool

Actors

Deck
*  List<Card>

Account
*  Money

Game
*  <Dealer>	Dealer	 
*  List<Player>	GamePlayers
*  Account 	BettingPool
*  List<Card>	TableCards
*  List<Round>	BettingRounds
	
*  EnterGame
*  PlayRound	
*  StopGame

Round
*  List<Card>	TableCards
*  Account	BettingPool
*  List<Player>	RoundPlayers
	
Dealer
*  <Deck> 	DeckOfCards

*  DealCardsToTable
*  DealCardsToPlayers
	
Player
*  List<Card>	PlayerHand
*  Account	Balance
	
*  PlaceBet
*  EnterGame
*  LeaveGame

Flows
*  EnterGame<Player,Game>
*  StartGame<Game>
*  PlayingRound
	*  DealCardToTable<Dealer,Game>				
	*  DealCardToPlayers<Player,Game>
	*  PlaceBets<Player,Game>
*  StopGame<Game>