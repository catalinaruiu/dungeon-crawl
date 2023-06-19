package com.codecool.dungeoncrawl;

import com.codecool.dungeoncrawl.logic.Cell;
import com.codecool.dungeoncrawl.logic.GameMap;
import com.codecool.dungeoncrawl.logic.MapLoader;
import com.codecool.dungeoncrawl.logic.actors.Actor;
import com.codecool.dungeoncrawl.logic.actors.Nazgul;
import com.codecool.dungeoncrawl.logic.actors.Ork;
import com.codecool.dungeoncrawl.dao.GameDatabaseManager;
import com.codecool.dungeoncrawl.logic.actors.Player;
import com.codecool.dungeoncrawl.model.GameState;
import com.codecool.dungeoncrawl.model.PlayerModel;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Main extends Application implements EventHandler<ActionEvent> {
    private GameDatabaseManager dbManager;
    int CANVAS_SIZE = 20;
    GameMap map = MapLoader.loadMap(1);
    GameMap savedMap1 = map;
    GameMap savedMap2 = MapLoader.loadMap(2);
    Canvas canvas = new Canvas(
            CANVAS_SIZE * Tiles.TILE_WIDTH,
            CANVAS_SIZE * Tiles.TILE_WIDTH);
    GraphicsContext context = canvas.getGraphicsContext2D();
    Label healthLabel = new Label();
    Label strengthLabel = new Label();
    Label playerInventory = new Label("Inventory-> ");
    Button pickUpItems = new Button("Pick up");
    Stage secondaryStage = new Stage();
    private Button saveButton;
    private Button loadButton;
    private Button cancelButton;
    Stage loadsStage = new Stage();

    public static void main(String[] args) {
        launch(args);
    }

    public Main(){
        dbManager = new GameDatabaseManager();
        try {
            dbManager.setup();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupDbManager();
        GridPane ui = new GridPane();
        ui.setPrefWidth(200);
        ui.setPadding(new Insets(10));

        ui.add(new Label("Health: "), 0, 0);
        ui.add(new Label("Strength: "), 0,1);
        ui.add(healthLabel, 1, 0);
        ui.add(strengthLabel,1,1);
        ui.add(pickUpItems,0,2);
        pickUpItems.setOnAction(click -> {
            map.getPlayer().pickUpItem();
            refresh();
        });
        pickUpItems.setFocusTraversable(false);
        ui.add(new Label("Inventory-> "),0,3);
        ui.add(playerInventory,0,4);

        BorderPane borderPane = new BorderPane();

        borderPane.setCenter(canvas);
        borderPane.setRight(ui);

        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        refresh();
        scene.setOnKeyPressed(this::onKeyPressed);
        scene.setOnKeyReleased(this::onKeyReleased);

        primaryStage.setTitle("Dungeon Crawl");
        primaryStage.show();
    }

    private void onKeyReleased(KeyEvent keyEvent) {
        KeyCombination exitCombinationMac = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
        KeyCombination exitCombinationWin = new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN);
        if (exitCombinationMac.match(keyEvent)
                || exitCombinationWin.match(keyEvent)
                || keyEvent.getCode() == KeyCode.ESCAPE) {
            exit();
        }
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
//            Player player = map.getPlayer();
//            dbManager.savePlayer(player);
//            break;
            saveWindow();
        } else {
            switch (keyEvent.getCode()) {
                case UP:
                    map.getPlayer().move(0, -1);
                    refresh();
                    break;
                case DOWN:
                    map.getPlayer().move(0, 1);
                    refresh();
                    break;
                case LEFT:
                    map.getPlayer().move(-1, 0);
                    refresh();
                    break;
                case RIGHT:
                    map.getPlayer().move(1,0);
                    refresh();
                    break;
            }
            map.repositionCenter();
            monstersMovement(map);
            if (map.getPlayer().isDead()){
                System.exit(0);
            }
            changeMap();
        }
    }

    private void monstersMovement(GameMap map) {
        try {
            map.removeDeadMonsters();
        } catch (ConcurrentModificationException e) {
            System.out.println("No monsters on map.");
        }
        for (Actor monster: map.getMonsters()){
            if (monster instanceof Ork){
                ((Ork)monster).move();
            }else if (monster instanceof Nazgul){
                ((Nazgul)monster).move();
            }
        }
    }

    private void refresh(){
        map.repositionCenter();
        int minX = map.getCenterCell().getX() - CANVAS_SIZE/2;
        int minY = map.getCenterCell().getY() - CANVAS_SIZE/2;
        int maxX = map.getCenterCell().getX() + CANVAS_SIZE/2;
        int maxY = map.getCenterCell().getY() + CANVAS_SIZE/2;
        context.setFill(Color.BLACK);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                Cell cell = map.getCell(x, y);
                if (cell.getActor() != null) {
                    Tiles.drawTile(context, cell.getActor(), x-minX, y-minY);
                } else if (cell.getItem() != null) {
                    Tiles.drawTile(context, cell.getItem(), x-minX, y-minY);
                } else {
                    Tiles.drawTile(context, cell, x-minX, y-minY);
                }
            }
            healthLabel.setText("" + map.getPlayer().getHealth());
            strengthLabel.setText("" + map.getPlayer().getStr());
            if (map.getPlayer().isDead()){
                healthLabel.setText("DEAD");
            }
            playerInventory.setText(""+map.getPlayer().showInventory());
        }

    }
    public void changeMap() {
        int previousHealth = map.getPlayer().getHealth();
        int previousAttackStrength = map.getPlayer().getStr();
        ArrayList previousInventory = map.getPlayer().getInventory();
        if (map.getPlayer().getChangeMap() && map.getPlayer().getOnMap() == 1) {
            savedMap1 = map;
            map = savedMap2;
            map.getPlayer().setOnMap(2);
        } else if (map.getPlayer().getChangeMap() && map.getPlayer().getOnMap() == 2) {
            savedMap2 = map;
            map = savedMap1;
            map.getPlayer().setOnMap(1);
        }
        map.getPlayer().setChangeMap(false);
        map.getPlayer().setHealth(previousHealth);
        map.getPlayer().setStr(previousAttackStrength);
        map.getPlayer().setInventory(previousInventory);
    }

    private void setupDbManager() {
        dbManager = new GameDatabaseManager();
        try {
            dbManager.setup();
        } catch (SQLException ex) {
            System.out.println("Cannot connect to database.");
        }
    }

    public void saveWindow() {
        secondaryStage.setTitle("Save Game State");

        saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setOnAction(event -> saveStates());

        loadButton = new Button();
        loadButton.setText("Load");
        loadButton.setOnAction(event -> loadStates());

        cancelButton = new Button();
        cancelButton.setText("Cancel");
        cancelButton.setOnAction(event -> closeWindow());

        HBox buttons = new HBox(saveButton, loadButton, cancelButton);
        buttons.setSpacing(50);
        buttons.setAlignment(Pos.CENTER);

        Label labelInput = new Label("Name");
        TextField textField = new TextField();
        HBox hbox = new HBox(labelInput, textField);
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        VBox vbox = new VBox(hbox, buttons);
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox, 600, 300);
        secondaryStage.setScene(scene);
        secondaryStage.show();
    }

    public void saveStates() {
        VBox vbox = (VBox) secondaryStage.getScene().getRoot();
        HBox hbox = (HBox) vbox.getChildren().get(0);
        TextField textField = (TextField) hbox.getChildren().get(1);
        String name = textField.getText();

        if (name != null && !name.isEmpty()) {
            // Check if the given name already exists in the database
            boolean nameExists = dbManager.getPlayerDaoJdbc().checkNameExists(name);

            if (nameExists) {
                // Show a confirmation dialog box
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(getClass().getResource("/com/codecool/dungeoncrawl/style.css").toExternalForm());
                dialogPane.getStyleClass().add("confirmation-dialog");
                alert.setTitle("Confirm Overwrite");
                alert.setHeaderText("Would you like to overwrite the already existing state?");

                // Add Yes, No, and Cancel buttons to the dialog
                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                ButtonType cancelButton = new ButtonType("Cancel");
                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                // Handle the user's choice
                alert.showAndWait().ifPresent(response -> {
                    if (response == yesButton) {
                        // Update the existing state
                        Date savedAt = new Date(System.currentTimeMillis());
                        Player player = map.getPlayer();
                        PlayerModel playerModel = dbManager.savePlayer(player);
                        GameState gameState = new GameState(map.getCurrentMapName(), savedAt, playerModel);
                        dbManager.saveGameStateOnPreviousSave(gameState);
                        closeWindow();
                    } else if (response == noButton) {
                        // Clear the input field and select it again
                        textField.clear();
                        textField.requestFocus();
                    }
                    // For the Cancel option, no further action is taken
                });
            } else {
                // Save the new state
                Date savedAt = new Date(System.currentTimeMillis());
                Player player = map.getPlayer();
                PlayerModel playerModel = dbManager.savePlayer(player);
                GameState gameState = new GameState(map.getCurrentMapName(), savedAt, playerModel);
                dbManager.saveGameState(gameState);
                closeWindow();
            }
        }
    }

    public void loadStates(){
        System.out.println(dbManager.getAllGameStates());
        VBox layout = new VBox();

        Label label1 = new Label();
        label1.setText("Saved States");

        Label label2 = new Label();
        label2.setText("Saved States");

        layout.getChildren().addAll(label1, label2);

        Scene scene = new Scene(layout, 400, 400);
        loadsStage.setScene(scene);
        loadsStage.show();
    }

    public void closeWindow() {
        secondaryStage.close();
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        GameDatabaseManager gameDatabaseManager = new GameDatabaseManager();
        try {
            gameDatabaseManager.setup();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(actionEvent.getSource() == saveButton) {
            if (map.getPlayer().getChangeMap()) {
                gameDatabaseManager.createNewSave(savedMap2);
            }
            else {
                gameDatabaseManager.createNewSave(map);
            }
        } if (actionEvent.getSource() == loadButton) {
            loadStates();
        }
    }

    private void exit() {
        try {
            stop();
        } catch (Exception e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
