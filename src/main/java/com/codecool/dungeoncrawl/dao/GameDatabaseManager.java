package com.codecool.dungeoncrawl.dao;

import com.codecool.dungeoncrawl.logic.GameMap;
import com.codecool.dungeoncrawl.logic.actors.Player;
import com.codecool.dungeoncrawl.model.GameState;
import com.codecool.dungeoncrawl.model.PlayerModel;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class GameDatabaseManager {
    private PlayerDao playerDao;
    private GameStateDao gameDao;
    private PlayerDaoJdbc playerDaoJdbc;
    private GameStateDaoJdbc gameStateDaoJdbc;

    public void setup() throws SQLException {
        DataSource dataSource = connect();
        playerDao = new PlayerDaoJdbc(dataSource);
        playerDaoJdbc = (PlayerDaoJdbc) playerDao;
//        gameDao = new GameStateDaoJdbc(dataSource);
        gameStateDaoJdbc = new GameStateDaoJdbc(dataSource);
        gameDao = gameStateDaoJdbc;
    }

    public List<GameState> getAllGameStates(){
        return gameDao.getAll();
    }

    public PlayerModel savePlayer(Player player, String name) {
        PlayerModel model = new PlayerModel(player);
        String playerName = name;
        if (playerName != null) {
            model.setPlayerName(playerName);
        }
        playerDao.add(model);
        return model;
    }

    public void saveGameState(GameState gameState) {
        gameDao.add(gameState);
    }

    public void saveGameStateOnPreviousSave(GameState currentGameState) {
        // Retrieve the existing GameState
        GameState existingGameState = gameDao.get(currentGameState.getId());

        // Update the necessary fields
        existingGameState.setCurrentMap(currentGameState.getCurrentMap());
        existingGameState.setSavedAt(currentGameState.getSavedAt());
        existingGameState.setPlayer(currentGameState.getPlayer());

        // Update the existing GameState in the database
        gameDao.update(existingGameState);
    }

    public void createNewSave(GameMap map, String name) {
        Player player = map.getPlayer();
        PlayerModel playerModel = savePlayer(player, name);
        Date localDate = new Date(System.currentTimeMillis());
        GameState gameState = new GameState(map.convertToString(), localDate, playerModel);
        saveGameState(gameState);
    }

    private DataSource connect() {
        PGSimpleDataSource dataSource = null;
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/connection.properties");
            Properties properties = new Properties();
            properties.load(fileInputStream);

            dataSource = new PGSimpleDataSource();
            String dbName = (String) properties.get("DB_NAME");
            String user = (String) properties.get("USERNAME");
            String password = (String) properties.get("PASSWORD");

            dataSource.setDatabaseName(dbName);
            dataSource.setUser(user);
            dataSource.setPassword(password);

            System.out.println("Trying to connect");
            dataSource.getConnection().close();
            System.out.println("Connection ok.");

        } catch (SQLException e) {
            System.out.println("Cannot connect to Database {} to data source {}");
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            System.out.println("could not find the file");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("could not load file");
            throw new RuntimeException(e);
        }

        return dataSource;
    }

    public PlayerDaoJdbc getPlayerDaoJdbc() {
        return playerDaoJdbc;
    }

    public GameStateDaoJdbc getGameStateDaoJdbc() {
        return gameStateDaoJdbc;
    }
}
