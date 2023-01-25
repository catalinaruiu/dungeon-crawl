package com.codecool.dungeoncrawl;

import com.codecool.dungeoncrawl.dao.GameDatabaseManager;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        //Main.main(args);
        GameDatabaseManager gameDatabaseManager = new GameDatabaseManager();
        try{
            gameDatabaseManager.setup();
        }
        catch (SQLException e){
            System.out.println("database not found");
        }
    }
}
