package com.codecool.shop.dao.implementation;

import com.codecool.shop.config.ConnectionHandler;
import com.codecool.shop.dao.GenericQueriesDao;
import com.codecool.shop.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBC extends ConnectionHandler implements GenericQueriesDao<User> {
    private static UserDaoJDBC instance = null;
    private PreparedStatement statement;

    private UserDaoJDBC() {
    }

    public static UserDaoJDBC getInstance() {
        if (instance == null) {
            instance = new UserDaoJDBC();
        }
        return instance;
    }


    @Override
    public void add(User user) {
        try {
            statement = getConn().prepareStatement("INSERT INTO users (user_name, password) VALUES  (?, ?);");
            statement.setString(1, user.getUsername());
            statement.setString(2, passWordHasher(user.getPassword()));
            statement.executeUpdate();
            statement.close();

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public User find(int id) {
        User user = null;
        try {
            statement = getConn().prepareStatement("SELECT * FROM users WHERE id = ?;");
            statement.setInt(1, id);

            ResultSet results = statement.executeQuery();

            String userName = "";
            String password = "";

            while (results.next()) {
                userName = results.getString("user_name");
                password = results.getString("password");
            }

            user = new User(userName, password);
            user.setId(id);

            statement.close();

        } catch (SQLException e) {
            System.out.println(e);
        }
        return user;
    }


    @Override
    public void remove(int id) {
        try {
            statement = getConn().prepareStatement("DELETE FROM users WHERE id=?;");
            statement.setInt(1, id);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try {
            statement = getConn().prepareStatement("SELECT id FROM users");
            ResultSet results = statement.executeQuery();

            while (results.next()) {

                int id = results.getInt("id");
                users.add(find(id));

            }

            statement.close();
            results.close();

            return users;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return users;
    }

    private String passWordHasher(String rawPassword){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, 4321, 25);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return new String(hash, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}