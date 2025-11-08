package com.playtheatria.sessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.errors.NotFoundException;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.errors.RepositoryException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public final class StreakRepository {
    private final Dao<Streak, UUID> dao;

    public StreakRepository(@NotNull TheatriaSessionsDB sessionsDB) throws SQLException {
        this.dao = sessionsDB.getDao(Streak.class);
    }

    /**
     * Load all streaks from the database
     * @return returns a Result containing the list of streaks if successful, or a PersistenceException if something failed.
     */
    public Result<List<Streak>, PersistenceException> loadStreaks() {
        try {
            return new Ok<>(dao.queryForAll());
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Failed to load streaks from the database", exception));
        }
    }

    /**
     * Load a streak by player UUID
     * @param playerUUID the UUID of the player whose streak we want to load.
     * @return returns a Result containing the streak if found, or an Exception if something failed.
     */
    public Result<Streak, RepositoryException> loadByPlayerUUID(@NotNull UUID playerUUID) {
        try {
            Streak streak = dao.queryForId(playerUUID);
            if (streak != null) {
                return new Ok<>(streak);
            } else {
                return new Err<>(
                        new NotFoundException("No streak found for player UUID: " + playerUUID));
            }
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Failed to load streak for player UUID: " + playerUUID, exception));
        }
    }

    /**
     * Creates or updates a streak
     * @param streak the streak we are going to persist in the database.
     * @return returns a Result containing the streak if creation or update was successful, or an Exception if something failed.
     */
    public Result<Streak, RepositoryException> createOrUpdate(@NotNull Streak streak) {
        try {
            dao.createOrUpdate(streak);
            return new Ok<>(streak);
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Error on createOrUpdate Streak: " + streak.getPlayerUUID(),
                            exception));
        }
    }

    /**
     * Deletes a streak
     * @param streak the streak we are going to delete from the database.
     * @return returns a Result containing true if deletion was successful, or an Exception if something failed.
     */
    // TODO! Fix return type to int return value from dao.delete
    public Result<Boolean, PersistenceException> delete(@NotNull Streak streak) {
        try {
            return new Ok<>(dao.delete(streak) > 0);
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Failed to delete streak for player: " + streak.getPlayerUUID(),
                            exception));
        }
    }
}
