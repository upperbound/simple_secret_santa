package com.github.upperbound.secret_santa.config;

import com.github.upperbound.secret_santa.web.dto.AvailableMessageBundle;
import com.github.upperbound.secret_santa.util.StaticContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * <p> All service related configurations </p>
 * @author Vladislav Tsukanov
 */
@Slf4j
@Configuration
public class ServiceConfig {
    /**
     * <p> Message source for message bundles </p>
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(StaticContext.MESSAGE_BUNDLE_BASE_NAME);
        messageSource.setDefaultLocale(Locale.ROOT);
        return messageSource;
    }

    /**
     * <p> All available message bundles </p>
     */
    @Bean
    public List<AvailableMessageBundle> availableMessageBundles() throws IOException {
        return Arrays.stream(
                        new PathMatchingResourcePatternResolver(this.getClass().getClassLoader())
                                .getResources("classpath*:" + StaticContext.MESSAGE_BUNDLE_BASE_NAME + "*.properties")
                )
                .map(resource -> {
                    Properties props = new Properties();
                    try {
                        props.load(resource.getInputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return new AvailableMessageBundle()
                            .setLang(props.getProperty("html.lang"))
                            .setImageUrl(props.getProperty("html.locale-image-url"));
                })
                .toList();
    }

    /**
     * <p> For JDBC operations </p>
     */
    @Primary
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcProperties.Template template = properties.getTemplate();
        jdbcTemplate.setFetchSize(template.getFetchSize());
        jdbcTemplate.setMaxRows(template.getMaxRows());
        if (template.getQueryTimeout() != null) {
            jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
        }
        return jdbcTemplate;
    }

    /**
     * <p> Just in case we will need some dialect specific params </p>
     */
    @Primary
    @Bean
    public Dialect jdbcHibernateDialect(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.execute((ConnectionCallback<Dialect>) con ->
                new StandardDialectResolver().resolveDialect(
                        new DatabaseMetaDataDialectResolutionInfoAdapter(con.getMetaData())
                )
        );
    }
}
