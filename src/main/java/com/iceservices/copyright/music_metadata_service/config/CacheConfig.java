package com.iceservices.copyright.music_metadata_service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${spring.cache.redis.time-to-live:30m}") Duration defaultTimeToLive,
            @Value("${spring.cache.artist-of-the-day.time-to-live:24h}") Duration artistOfTheDayTimeToLive) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTimeToLive);

        RedisCacheConfiguration artistOfTheDayConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(artistOfTheDayTimeToLive);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("artist-of-the-day", artistOfTheDayConfig)
                .build();
    }
}