# SudokuADC

This project has been developed as final course work for Distributed Systems course at the University of Salerno.

Candidate:  
Piccolo Gianluca - 0522500596

## Homework assignment

Date of birth: 03/03/1995  
md5(gianlucapiccolo-03) = d2c8ef93317526ed291cc1590c0377b0    
First character is "d" so homework assigned is number 4 (Sudoku)


## Project description and overview

This project is a Sudoku challenge game on a P2P network. It has been developed in Java, using the following technologies:
- [TompP2P](https://tomp2p.net/): P2P-based key-value pair storage library
- [Apache Maven](https://maven.apache.org/): Software project management
- [JUnit](https://junit.org/junit5/): Unit testing framework for the Java programming language

The game requires a nickname based login to create a new Sudoku game or to join one. Nicknames and game names must be unique.  

Features before game starts:
- Create game: generate a new game and then join it
- Join game: entering an existing game

Features after game starts:
- View Sudoku: get local board of the player
- Place number: place a number on local board and get the score of the move
- Exit: leave the game

## Game description

When participating to a Sudoku game, players don't share the global board but can only see the fixed numbers and and those they placed in their local board.  

Each player has a score and earns 1 pt every time he places a number that no other player in the challenge has found. If the number is correct but already found by another player, no point is given. 

There is a penalization of 1 pt if the player attempts to place a wrong number in an empty cell or try to place a number in a fixed cell.  

The challenge ends when one player has completed his local board. Direct messages are sent to the other players in the same room after each move, specifying the score of the move itself.


# Architecture

When a new Sudoku game is generated, a MatchData object is stored into the Dht at "gameName" path.
It contains:
- startGrid: initial board generated
- sharedGrid: global board filled by all correct numbers placed by the players
- solvedGrid: solution board to calcuate the score of the player move
- players: list of players with nicknames, peers and scores.

When the player joins a game, the local grid (initially corresponds to initial grid) is stored into the Dht at "nickname" path.  
The player "subribes" to the match by adding himself into the player list of MatchData object, with the aim to send/receive direct messages with the other players of the match.  After he places a number, the local grid is updated.


# Test

Testing

# Build and run with Docker

Per lanciare il programma, dal master digitare:

    mpirun -np <numProcs> --hostfile machinefile ./par <numRighe> <numColonne> <numGenerazioni>
