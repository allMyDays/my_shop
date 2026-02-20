package com.example.cart_wishlist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        assertNotNull(dataSource);
        System.out.println("databaseUrl: "+dataSource.getConnection().getMetaData().getURL());

    }


}
