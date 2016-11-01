package objective.taskboard.config;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import com.google.common.cache.CacheBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String JIRA_FIELD_METADATA = "jira-field-metadata";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Arrays.asList(
                new GuavaCache("configuration", CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).concurrencyLevel(1).build()),
                new GuavaCache("issues", CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).concurrencyLevel(1).build()),
                new GuavaCache("metadata", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("projects", CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).concurrencyLevel(1).build()),
                new GuavaCache("filters", CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).concurrencyLevel(1).build()),
                new GuavaCache("issueTypeMetadata", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("prioritiesMetadata", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("statusesMetadata", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("metadata", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("projectMetadata", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("visibleTeams", CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache("issueTypeConfiguration", CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).concurrencyLevel(1).build()),
                new GuavaCache("userTeam", CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).concurrencyLevel(1).build()),
                new GuavaCache("projectUsers", CacheBuilder.newBuilder().expireAfterAccess(6, TimeUnit.HOURS).concurrencyLevel(1).build()),
                new GuavaCache(JIRA_FIELD_METADATA, CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).concurrencyLevel(1).build())
        ));
        return simpleCacheManager;
    }

}
