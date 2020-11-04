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


## Architecture

When a new Sudoku game is generated, a MatchData object is stored into the Dht at "gameName" path. 

It contains:
- startGrid: initial board generated
- sharedGrid: global board filled with all correct numbers placed by the players
- solvedGrid: solution board to calcuate the score of the player move
- players: list of players with nicknames, peer addresses and scores.
  
\
When the player joins a game, the local grid (initially corresponds to initial grid) is stored into the Dht at "nickname" path.  

The player "subscribes" to the match by adding himself into the player list of MatchData object, with the aim to send/receive direct messages with the other players of the match.  

After he places a number, the local grid is updated.


# Test
The tests provide an example game with 4 peers which communicate and play together joining the same game. Functionalities tested:
- creating a new game
- joining an existing game
- attempt to login with an existing username
- attempt to create a new game with an existing game name
- attempt to join a game with a non valid game name
- calculating the correct score of the move
- leaving the game


# Build and run with Docker

Steps:

1) Clone the project from github at: <a>https://github.com/gipic/SudokuADC</a>

2) Move to the project folder and build the docker container with the following command: ``docker build --no-cache -t p2p-sudoku-adc .``

3) Start the master peer with the following command: ``docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 p2p-sudoku-adc``

    the MASTERIP envirnoment variable is the master peer ip address and the ID environment variable is the unique id of your peer. Remeber you have to run the master peer using the ID=0.

4) Start a generic peer, to do that you first have to check the ip address of your container:
    * Check the docker: ``docker ps``
    * Check the IP address: ``docker inspect <container ID>``

Now you can start peers by executing the following command passing the IP address previously found as MASTERIP:
``docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 p2p-sudoku-adc`` 
