package org.github.DasJava.land.service.repo.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public record SqLiteDatabase(String url) {

    public SqLiteDatabase(String url) {
        this.url = url;
        createTable();
    }

    private void createTable() {
        String tableStatement = """
                CREATE TABLE IF NOT EXISTS lands (
                    world TEXT NOT NULL,
                    idX INTEGER NOT NULL,
                    idZ INTEGER NOT NULL,
                    owner TEXT,
                    json TEXT,
                    PRIMARY KEY (world, idX, idZ)
                );
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA journal_mode = WAL;"); // activate write ahead

            statement.execute(tableStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

}