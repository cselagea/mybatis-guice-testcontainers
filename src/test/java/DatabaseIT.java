import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.builtin.PooledDataSourceProvider;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Testcontainers
class DatabaseIT {

    static {
        LogManager.getLogManager().reset(); // disable warning messages produced by JDBC driver trying to connect before server is ready
    }

    @Container
    private static final JdbcDatabaseContainer sqlServerContainer = new MSSQLServerContainer().withInitScript("init.sql");

    private static BookMapper bookMapper;

    @BeforeAll
    static void setUp() {
        final Properties myBatisProperties = new Properties();
        myBatisProperties.setProperty("mybatis.environment.id", "test");
        myBatisProperties.setProperty("JDBC.driver", sqlServerContainer.getDriverClassName());
        myBatisProperties.setProperty("JDBC.url", sqlServerContainer.getJdbcUrl());
        myBatisProperties.setProperty("JDBC.username", sqlServerContainer.getUsername());
        myBatisProperties.setProperty("JDBC.password", sqlServerContainer.getPassword());
        myBatisProperties.setProperty("JDBC.autoCommit", "false");

        Injector injector = Guice.createInjector(
                new MyBatisModule() {
                    protected void initialize() {
                        bindDataSourceProviderType(PooledDataSourceProvider.class);
                        bindTransactionFactoryType(JdbcTransactionFactory.class);
                        addMapperClass(BookMapper.class);
                    }
                },
                binder -> Names.bindProperties(binder, myBatisProperties));

        bookMapper = injector.getInstance(BookMapper.class);
    }

    @Test
    void shouldFindTwoBooks() {
        List<Book> books = bookMapper.selectBooks();

        assertThat(books)
                .extracting("title", "pages")
                .containsExactlyInAnyOrder(
                        tuple("A Short History of Nearly Everything", 544),
                        tuple("1984", 328)
                );
    }

}
