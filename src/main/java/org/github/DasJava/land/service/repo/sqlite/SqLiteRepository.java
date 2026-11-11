package org.github.DasJava.land.service.repo.sqlite;

import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.gson.GsonProvider;
import org.github.DasJava.land.service.repo.LandRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;

public class SqLiteRepository implements LandRepository {

    private final SqLiteDatabase database;
    private final ExecutorService executor;

    public SqLiteRepository(final SqLiteDatabase database, ExecutorService executor) {
        this.database = database;
        this.executor = executor;
    }

    public SqLiteRepository(final SqLiteDatabase database) {
        this(database, ForkJoinPool.commonPool());
    }

    @Override
    public CompletableFuture<Land> findById(LandID id) {
        return CompletableFuture.supplyAsync(() -> {

            String sql = """
            SELECT json
            FROM lands
            WHERE world = ?
              AND idX = ?
              AND idZ = ?
            """;

            try (Connection connection = database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, id.worldName());
                preparedStatement.setInt(2, id.x());
                preparedStatement.setInt(3, id.z());

                ResultSet result = preparedStatement.executeQuery();

                if (!result.next()) {
                    return Land.unclaimed(id);
                }

                String json = result.getString("json");

                return GsonProvider.instance.fromJson(json, Land.class);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }, executor);
    }

    @Override
    public CompletableFuture<List<Land>> findByOwner(UUID owner) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                    SELECT json FROM lands
                    WHERE owner = ?
                    """;

            try(Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)){

                statement.setString(1, owner.toString());

                List<Land> lands = new ArrayList<>();
                ResultSet result = statement.executeQuery();
                while(result.next()) {
                    String json = result.getString("json");
                    Land obj =  GsonProvider.instance.fromJson(json, Land.class);
                    lands.add(obj);
                }

                return lands;
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<LandID>> findLandIdsByOwner(UUID owner) {
        return findByOwner(owner)
                .thenApply(landList -> landList.stream()
                        .map(Land::id)
                        .toList());
    }

    private final ReentrantLock writeLock = new ReentrantLock();

    public CompletableFuture<Void> save(Land land) {
        return CompletableFuture.runAsync(() -> {
            writeLock.lock();
            try {
                String sql = """
                INSERT INTO lands (world, idX, idZ, owner, json)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (world, idX, idZ) DO UPDATE SET
                    owner = excluded.owner,
                    json = excluded.json
                """;

                try (Connection connection = database.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql)) {

                    var id = land.id();

                    statement.setString(1, id.worldName());
                    statement.setInt(2, id.x());
                    statement.setInt(3, id.z());
                    statement.setString(4, land.owner().toString());
                    statement.setString(5, GsonProvider.instance.toJson(land));

                    statement.executeUpdate();

                    /*
                        IO.println("Connection: " + connection.getMetaData().getURL());
                        IO.println("Land: " + id);
                        IO.println("Owner: " + land.owner());

                        int affected = statement.executeUpdate();

                        IO.println("Rows affected: " + affected);
                        IO.println("AutoCommit: " + connection.getAutoCommit());
                    */
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                writeLock.unlock();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> delete(LandID id) {
        return CompletableFuture.runAsync(()->{
            String sql = """
                DELETE FROM lands
                WHERE world = ?
                AND idX = ?
                AND idZ = ?
                """;

            try(Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)){

                statement.setString(1, id.worldName());
                statement.setInt(2, id.x());
                statement.setInt(3, id.z());

                statement.executeUpdate();

            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

}
