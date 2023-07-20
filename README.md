# Dungeon Crawl

## Description

Dungeon Crawl is a tile-based roguelike game, in which the player needs to fight or avoid monsters in order to reach the last map and retrieve a treasure.

## Technologies used

- **IDE:** IntelliJ IDEA
- **Project Management Build Tool:** Maven
- **Programming Language:** Java
- **Special Libraries:** JavaFX
- **Database:** PostgreSQL
- **Version Control:** Git & GitHub

## Installation

As this is a project developed in IntelliJ, the running instructions work best if you use the same IDE. In other cases, please feel free to research the way to run Maven JavaFX projects with your chosen IDE.

1. Download the project onto your local machine.
2. Open the project in IntelliJ IDEA. This is a Maven project, so you need to open the file `pom.xml`.
3. The project uses JavaFX, so use the JavaFX Maven plugin to build and run the program. Build using `mvn javafx:compile`, and run using `mvn javafx:run`.

## Controls and Gameplay

1. Use the arrows on your keyboard to move on the map.
2. Whenever you find items, move over them and left click on the button "Pick up" on the right side of the screen using the mouse.
3. Keys open doors.
4. Stairs change the map you are currently on. Make sure to double press the movement key when moving up and down the stairs.
5. Find the sword to be able to battle monsters. Each sword you pick up increases your Strength.
6. Orks cannot be caught, they will always be at least one tile away from you. They also do not do damage.
7. Skeletons are easy to beat.
8. In order to beat the Nazgul, make sure you pick up both swords, as damage from him can kill you in the beginning.
9. `CTRL+S` saves the state in the database.

## Implementation

![Starting Game](https://drive.google.com/file/d/1p8lxakvUceXUpR6i-Oq1lQg-IzXBZ-uD/view?usp=drive_link)
![Gameplay]((https://drive.google.com/file/d/1X8BIHPSkpT-zSFC1mrjOKLS8bf6J4VTt/view?usp=drive_link)
![Saving Window](https://drive.google.com/file/d/1EQHsbbovK84MmRxzZVHMTBDy0IpPqK3q/view?usp=drive_link)

## Ideas For Further Development

- Add more characters
- Add more item types (e.g., lives, gold, treasure, etc.)
- Implement functionality of doors
- Create more maps
- End the game
- Possibility to save the game locally in JSON format
- Improve saving functionality in the database
- Loading the game state
