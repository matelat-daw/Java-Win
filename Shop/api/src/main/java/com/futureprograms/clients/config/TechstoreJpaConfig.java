package com.futureprograms.clients.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.miapp.repository",
        entityManagerFactoryRef = "techstoreEntityManagerFactory",
        transactionManagerRef = "techstoreTransactionManager"
)
public class TechstoreJpaConfig {

    private final Environment environment;

    public TechstoreJpaConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource techstoreDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(environment.getProperty("spring.datasource.techstore.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.techstore.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.techstore.password"));
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.techstore.driver-class-name", "org.mariadb.jdbc.Driver"));
        return dataSource;
    }

    @Bean(name = "techstoreEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean techstoreEntityManagerFactory(
            @Qualifier("techstoreDataSource") DataSource dataSource
    ) {
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", environment.getProperty("app.techstore.jpa.ddl-auto", "update"));
        jpaProps.put("hibernate.dialect", environment.getProperty("app.techstore.jpa.dialect", "org.hibernate.dialect.MariaDBDialect"));
        jpaProps.put("hibernate.jdbc.time_zone", "UTC");
        jpaProps.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        jpaProps.put("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.miapp.model");
        emf.setPersistenceUnitName("techstore");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(jpaProps);
        return emf;
    }

    @Bean(name = "techstoreTransactionManager")
    public JpaTransactionManager techstoreTransactionManager(
            @Qualifier("techstoreEntityManagerFactory") LocalContainerEntityManagerFactoryBean techstoreEntityManagerFactory
    ) {
        return new JpaTransactionManager(techstoreEntityManagerFactory.getObject());
    }
}
