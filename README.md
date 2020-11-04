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

Feature before game starts:
- Create game: generate a new game and then join it
- Join game: entering an existing game

Feature after game starts:
- View Sudoku: get local board of the player
- Place number: place a number on local board and get the score of the move
- Exit: leave the game

## Game description

When participating to a Sudoku game, players don't share the global board but can only see the fixed numbers and those they placed themselves.  

Each player has a score and earns 1 pt every time he places a number that no other player in the challenge has found. If the number is correct but already found by another player, no point is given. 

There is a penalization of 1 pt if the player attempts to place a wrong number in an empty cell or try to place a number in a fixed cell.  

The challenge ends as soon as one player has completed his own board. Direct messages are sent to the other players in the same room after each move, specifying the score of the move.


# Architecture

Dht feature:
- a
- b
- c

# Test

Testing

# Build and run with Docker

Per lanciare il programma, dal master digitare:

    mpirun -np <numProcs> --hostfile machinefile ./par <numRighe> <numColonne> <numGenerazioni>
