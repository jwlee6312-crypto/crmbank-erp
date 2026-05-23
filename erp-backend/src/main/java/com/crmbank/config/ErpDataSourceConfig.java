package com.crmbank.config;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.crmbank.global.handler.MapKeyUpperWrapperFactory;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
/**
 * 💡 ERP (SQL Server) 데이터 소스 설정
 */
@MapperScan(
    // 💡 스캔 범위를 .mapper 하위로 좁혀서 빈 주입 충돌 및 기동 에러를 방지합니다.
    basePackages = { "com.crmbank.erp.**.mapper" }, 
    sqlSessionTemplateRef = "erpSqlSessionTemplate"
)
public class ErpDataSourceConfig {

    @Autowired
    private Environment env;

    @Bean(name = "erpDataSource")
    @Primary
    public DataSource erpDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        String host = env.getProperty("ERP_DB_HOST", "127.0.0.1");
        String port = env.getProperty("ERP_DB_PORT", "1433");
        String dbName = env.getProperty("ERP_DB_NAME", "STANDARD");
        String username = env.getProperty("ERP_DB_USERNAME", "sa");
        String password = env.getProperty("ERP_DB_PASSWORD", "crmbank");
        
        String jdbcUrl = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=false;loginTimeout=30",
                host, port, dbName);
        
        log.info("🔌 [ERP DB 연동] 접속 시도: {}", jdbcUrl);
        
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(20000);
        
        return dataSource;
    }

    @Bean(name = "erpJdbcTemplate")
    public JdbcTemplate erpJdbcTemplate(@Qualifier("erpDataSource") DataSource erpDataSource) {
        return new JdbcTemplate(erpDataSource);
    }

    @Bean(name = "erpSqlSessionFactory")
    @Primary
    public SqlSessionFactory erpSqlSessionFactory(@Qualifier("erpDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 💡 XML 매퍼 위치를 명확히 지정
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:/com/crmbank/erp/**/*.xml"));
        
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setCallSettersOnNulls(true);
        configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(false); 
        configuration.setObjectWrapperFactory(new MapKeyUpperWrapperFactory());

        sessionFactory.setConfiguration(configuration);

        return sessionFactory.getObject();
    }

    @Bean(name = "erpSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate erpSqlSessionTemplate(
            @Qualifier("erpSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "erpTransactionManager")
    @Primary
    public PlatformTransactionManager erpTransactionManager(@Qualifier("erpDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
