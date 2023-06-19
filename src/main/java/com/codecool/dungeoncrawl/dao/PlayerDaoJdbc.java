package com.codecool.dungeoncrawl.dao;

import com.codecool.dungeoncrawl.model.PlayerModel;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDaoJdbc implements PlayerDao {
    private DataSource dataSource;

    public PlayerDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private PlayerModel retrievePlayerModelFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        int hp = rs.getInt(3);
        int x = rs.getInt(4);
        int y = rs.getInt(5);
        PlayerModel playerModel = new PlayerModel(name, x, y);
        playerModel.setHp(hp);
        playerModel.setId(id);
        return playerModel;
    }

    @Override
    public void add(PlayerModel player) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO player (player_name, hp, x, y) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, player.getPlayerName());
            statement.setInt(2, player.getHp());
            statement.setInt(3, player.getX());
            statement.setInt(4, player.getY());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            player.setId(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(PlayerModel player) {
        try (Connection conn = dataSource.getConnection()){
            String sql = "UPDATE player  SET player_name = ?, hp = ?, x = ?, y= ? WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, player.getPlayerName());
            statement.setInt(2, player.getHp());
            statement.setInt(3, player.getX());
            statement.setInt(4, player.getY());
            statement.setInt(5, player.getId());
            statement.executeUpdate();
        }
        catch (SQLException e){
            throw new RuntimeException();
        }
    }

    @Override
    public PlayerModel get(int id) {
        try(Connection conn = dataSource.getConnection()){
            String sql = "SELECT * FROM player WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            } else {
                return retrievePlayerModelFromResultSet(rs);
            }
        }
        catch (SQLException e){
            throw new RuntimeException();
        }
    }

    @Override
    public List<PlayerModel> getAll() {
        try(Connection conn = dataSource.getConnection()){
            String sql = "SELECT * FROM player";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            List<PlayerModel> playerModels = new ArrayList<>();
            while (rs.next()) {
                playerModels.add(retrievePlayerModelFromResultSet(rs));
            }
            return playerModels;
        }
        catch (SQLException e){
            throw new RuntimeException();
        }
    }

    public boolean checkNameExists(String name) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM player WHERE player_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
