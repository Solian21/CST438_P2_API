package edu.csumb.cst438.api;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

// change test in run config to dbtest or run ./gradlew dbtest
@Tag("neon")
@SpringBootTest
public class DatabaseConnectionTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void testNeonConnection() throws SQLException{
        try(Connection connection = dataSource.getConnection()){
            assertThat(connection.isValid(5)).isTrue();
        }
    }
}
