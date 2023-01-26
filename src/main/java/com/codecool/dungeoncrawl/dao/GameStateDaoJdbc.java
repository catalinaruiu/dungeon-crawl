package com.codecool.dungeoncrawl.dao;

import com.codecool.dungeoncrawl.model.GameState;
import com.codecool.dungeoncrawl.model.PlayerModel;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameStateDaoJdbc implements GameStateDao {
    private DataSource dataSource;

    public GameStateDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void add(GameState state) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO game_state (current_map, saved_at, player_id) VALUES (?,now(),?)";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, state.getCurrentMap());
            statement.setInt(2, state.getPlayer().getId());
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            state.setId(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(GameState state) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE game_state SET current_map = ? ,saved_at = now(), player_id = ? WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, state.getCurrentMap());
            statement.setInt(2, state.getPlayer().getId());
            statement.setInt(3, state.getId());
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    private GameState retrieveGameStateFromResultSet(ResultSet rs) throws SQLException {
        int stateId = rs.getInt(1);
        String currentMap = rs.getString(2);
        Date date = rs.getDate(3);
        int playerId = rs.getInt(4);
        String playerName = rs.getString(5);
        int hp = rs.getInt(6);
        int x = rs.getInt(7);
        int y = rs.getInt(8);
        PlayerModel playerModel = new PlayerModel(playerName, x, y);
        playerModel.setHp(hp);
        playerModel.setId(playerId);

        GameState gameState=  new GameState(currentMap, date, playerModel);
        gameState.setId(stateId);
        return gameState;
    }

    @Override
    public GameState get(int id) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT game_state.id, game_state.current_map, game_state.saved_at, game_state.player_id,\n" +
                    "       player.player_name, player.hp, player.x, player.y\n" +
                    "FROM game_state\n" +
                    "INNER JOIN player ON player.id = game_state.player_id\n" +
                    "WHERE game_state.id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            } else {
                return retrieveGameStateFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public List<GameState> getAll() {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT game_state.id, game_state.current_map, game_state.saved_at, game_state.player_id,\n" +
                    "       player.player_name, player.hp, player.x, player.y\n" +
                    "FROM game_state\n" +
                    "inner join player on player.id = game_state.player_id\n";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            List<GameState> gameStates = new ArrayList<>();
            while (rs.next()) {
                gameStates.add(retrieveGameStateFromResultSet(rs));
            }
            return gameStates;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
