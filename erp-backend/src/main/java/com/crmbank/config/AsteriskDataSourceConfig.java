package com.crmbank.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.crmbank.global.handler.MapKeyUpperWrapperFactory;
import javax.sql.DataSource;

@Slf4j
@Configuration
/**
 * 💡 Asterisk (MySQL) 데이터 소스 설정
 */
@MapperScan(
    basePackages = "com.crmbank.asterisk.mapper",
    sqlSessionTemplateRef = "asteriskSqlSessionTemplate"
)
public class AsteriskDataSourceConfig {

    @Autowired
    private Environment env;

    @Bean(name = "asteriskDataSource")
    public DataSource asteriskDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        String defaultUrl = "jdbc:mysql://localhost:3306/asterisk?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";
        String url = env.getProperty("ASTERISK_DB_URL", defaultUrl);
        
        log.info("🔌 [Asterisk MySQL 연동] 접속 시도: {}", url);
        
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(env.getProperty("ASTERISK_DB_USERNAME", "root"));
        dataSource.setPassword(env.getProperty("ASTERISK_DB_PASSWORD", "gkdldhs12#$"));
        dataSource.setMaximumPoolSize(5);
        
        return dataSource;
    }

    @Bean(name = "asteriskSqlSessionFactory")
    public SqlSessionFactory asteriskSqlSessionFactory(@Qualifier("asteriskDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        // 💡 [최종] 계층형 구조(com/crmbank/asterisk)의 XML만 로드하도록 설정 통합
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:/com/crmbank/asterisk/**/*.xml"));
        
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setCallSettersOnNulls(true);
        configuration.setMapUnderscoreToCamelCase(false); 
        
        // 💡 모든 결과 Map의 Key를 대문자로 강제 변환
        configuration.setObjectWrapperFactory(new MapKeyUpperWrapperFactory());

        sessionFactory.setConfiguration(configuration);

        return sessionFactory.getObject();
    }

    @Bean(name = "asteriskSqlSessionTemplate")
    public SqlSessionTemplate asteriskSqlSessionTemplate(
            @Qualifier("asteriskSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "asteriskTransactionManager")
    public PlatformTransactionManager asteriskTransactionManager(@Qualifier("asteriskDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
