package data;

import data.model.LiftRide;
import data.model.SkierVertical;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SkierDao {

    public boolean insertLiftRide(LiftRide liftRide) {
        String insertSQLStatement =
                "INSERT INTO liftRides (resortID, liftID, skierID, dayID, runTime, vertical)"
                + "VALUES(?,?,?,?,?,?)";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQLStatement)) {

            preparedStatement.setString(1, liftRide.getResortId());
            preparedStatement.setString(2, liftRide.getLiftId());
            preparedStatement.setString(3, liftRide.getSkierId());
            preparedStatement.setString(4, liftRide.getDayId());
            preparedStatement.setString(5, liftRide.getTime());
            preparedStatement.setInt(6, liftRide.getVertical());

            return preparedStatement.executeUpdate() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public SkierVertical selectSkierTotalVerticalForDay(
            String skierId, String resortId, String dayId) {

        String selectSQLStatement =
                "SELECT resortID, SUM(vertical) as totalVertical FROM liftRides " +
                        "WHERE  resortID=? AND dayID=? AND skierID=? " +
                        "GROUP BY resortID";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQLStatement)) {

            preparedStatement.setString(1, resortId);
            preparedStatement.setString(2, dayId);
            preparedStatement.setString(3, skierId);

            ResultSet resultSet = preparedStatement.executeQuery();
            Optional<SkierVertical> skierVert = getSkierVertFromSQLResults(resultSet);

            return skierVert.orElse(null);
        } catch (SQLException ignored) {
        }
        return null;
    }

    private Optional<SkierVertical> getSkierVertFromSQLResults(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                int totalVertical = resultSet.getInt("totalVertical");
                String resortId = resultSet.getString("resortID");

                return Optional.of(new SkierVertical(resortId, totalVertical));
            }
        } catch (SQLException ignored) {
        }
        return Optional.empty();
    }

    public SkierVertical selectSkierVerticalResortTotal(String skierId, String resortId) {
        String selectSQLStatement =
                "SELECT resortId, SUM(vertical) as totalVertical FROM liftRides " +
                        "WHERE resortID=? AND skierID=? " +
                        "GROUP BY resortId";

        try (Connection connection = DataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQLStatement)) {

            preparedStatement.setString(1, resortId);
            preparedStatement.setString(2, skierId);

            ResultSet resultSet = preparedStatement.executeQuery();
            Optional<SkierVertical> skierVert = getSkierVertFromSQLResults(resultSet);

            return skierVert.orElse(null);
        } catch (SQLException ignored) {
        }
        return null;
    }
}
