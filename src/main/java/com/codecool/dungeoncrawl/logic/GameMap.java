package com.codecool.dungeoncrawl.logic;

import com.codecool.dungeoncrawl.logic.actors.*;
import com.codecool.dungeoncrawl.logic.items.Key;
import com.codecool.dungeoncrawl.logic.items.Sword;

import java.util.ArrayList;

public class GameMap {
    private int width;
    private int height;
    private Cell[][] cells;
    Cell centerCell;

    private Player player;
    private ArrayList<Actor> monsters = new ArrayList<>();
    private String currentMapName;

    public Cell[][] getCells() {
        return cells;
    }

    public GameMap(int width, int height, CellType defaultCellType) {
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(this, x, y, defaultCellType);
            }
        }
    }

    public void addMonsterToMap(Actor monster) {
        monsters.add(monster);
    }
    public void removeDeadMonsters(){
        for (Actor monster: monsters) {
            if (monster.getHealth()<=0) {
                monsters.remove(monster);
            }
        }
    }

    public ArrayList<Actor> getMonsters() {
        return monsters;
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCenterCell() {
        return centerCell;
    }

    public String getCurrentMapName() {
        return currentMapName;
    }

    public void repositionCenter(){
        int centerX;
        int centerY;

        if (player.getCell().getX() <= 10) {
            centerX = 10;
        } else {
            centerX = Math.min(player.getCell().getX(), width - 10);
        }

        if (player.getCell().getY() <= 10) {
            centerY = 10;
        } else {
            centerY = Math.min(player.getCell().getY(), height - 10);
        }

        centerCell = cells[centerX][centerY];
    }

    public String convertToString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Cell[] cells: this.cells){
            for (Cell cell: cells){
                if (cell.getType() == CellType.EMPTY){
                    stringBuilder.append(" ");
                } else if (cell.getActor() != null ){
                    if (cell.getActor() instanceof Skeleton) {
                        stringBuilder.append("s");
                    } else if (cell.getActor() instanceof Ork) {
                        stringBuilder.append("o");
                    } else if (cell.getActor() instanceof Nazgul) {
                        stringBuilder.append("n");
                    } else if (cell.getActor() instanceof Player) {
                        stringBuilder.append("@");
                    } else {
                        stringBuilder.append("+");
                    }
                } else if(cell.getItem() != null) {
                    if(cell.getItem() instanceof Key) {
                        stringBuilder.append("k");
                    } else if(cell.getItem() instanceof Sword) {
                        stringBuilder.append("w");
                    } else {
                        stringBuilder.append("+");
                    }
                } else if(cell.getType() == CellType.CLOSED_DOOR) {
                    stringBuilder.append("d");
                } else if(cell.getType() == CellType.STAIRS) {
                    stringBuilder.append("r");
                } else if(cell.getType() == CellType.FLOOR) {
                    stringBuilder.append(".");
                } else {
                    stringBuilder.append("#");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
