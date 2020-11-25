package data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@NoArgsConstructor
public class DataSource {

    private static final String DB_USERNAME = System.getProperty("DB_USER");
    private static final String DB_PASSWORD = System.getProperty("DB_PASS");
    private static final String DB_URL = System.getProperty("DB_URL");
    private static final String DB_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int POOL_SIZE = 50;
    private static final int MIN_IDLE_TIME = 5;


    private static HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource hikariDataSource;

    static {
        hikariConfig.setJdbcUrl("jdbc:mysql://ski-db.cns5d9orpwwz.us-west-2.rds.amazonaws.com:3306/ski");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("databasepassword");
//        hikariConfig.setJdbcUrl(DB_URL);
//        hikariConfig.setUsername(DB_USERNAME);
//        hikariConfig.setPassword(DB_PASSWORD);

        hikariConfig.setDriverClassName(DB_DRIVER_CLASS_NAME);

        hikariConfig.addDataSourceProperty("serverTimezone", "UTC");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "256");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariConfig.setMaximumPoolSize(POOL_SIZE);
//        hikariConfig.setMinimumIdle(MIN_IDLE_TIME);

        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}
