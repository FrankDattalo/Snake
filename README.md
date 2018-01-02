# Snake Game
This repository contains code for a 4-player snake game.  The game supports two game modes and a score system.
## Install
This game was tested on Windows 10, feel free to try it on other operating systems. In order to install the game, the following software is required `git`, `jdk 8`, and `gradle`.
1. clone this repository locally `git clone http://www.github.com/FrankDattalo/Snake.git`
2. build the game by `cd`’ing into the downloaded directory and typing `gradle build`
## Starting the Game
There are two modes to the game:
1. Snake mode - Food spawns gradually which, when eaten, will increase the snake’s size
2. Tron mode - Players leave a trail behind them that will never disappear
In order to start the game you must be within the downloaded directory and type the following command: `java -jar build\libs\snake.jar <--skip-names or –names> <--tron or –snake>`
Using `–skip-names` will not prompt the user to enter any names or number of players, the maximum amount of detected controllers will be used and players will be given generic names.  Using `--tron` or `--snake` will set the desired game mode.
## Controls
In order to play this game, a controller is required.  Move using the d-pad, reset the game using the B button, and quit using the Y button.
