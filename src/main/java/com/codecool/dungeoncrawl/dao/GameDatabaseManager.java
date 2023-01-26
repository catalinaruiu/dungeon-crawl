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
import java.time.LocalTime;
import java.util.Properties;

public class GameDatabaseManager {
    private PlayerDao playerDao;
    private GameStateDao gameDao;
    private Date date;

    public void setup() throws SQLException {
        DataSource dataSource = connect();
        playerDao = new PlayerDaoJdbc(dataSource);
        gameDao = new GameStateDaoJdbc(dataSource);
    }

    public PlayerModel savePlayer(Player player) {
        PlayerModel model = new PlayerModel(player);
        playerDao.add(model);
        return model;
    }

    public void saveGameState(GameState gameState) {
        gameDao.add(gameState);
    }

    public void saveGameStateOnPreviousSave(GameState currentGameState) {
        gameDao.update(currentGameState);
    }

    public void createNewSave(GameMap map) {
        Player player = map.getPlayer();
        PlayerModel playerModel = savePlayer(player);
        LocalTime localTime = LocalTime.now();
        Date saveTime = Date.valueOf(String.valueOf(localTime));
        GameState gameState = new GameState("map.txt", saveTime, playerModel);
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
}
