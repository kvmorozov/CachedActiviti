/**
 * Copyright 2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.kmorozov.activiti.demo.ignite;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteIllegalStateException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.jdbc.TcpDiscoveryJdbcIpFinder;
import ru.kmorozov.activiti.demo.config.LocalH2Config;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Cache adapter for Ignite. Cache is initialized from IGNITE_HOME/config/default-config.xml settings, otherwise default
 * one is started.
 *
 * @author Roman Shtykh
 */
public final class IgniteCacheAdapter implements Cache {
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(IgniteCacheAdapter.class);

    /**
     * Cache id.
     */
    private final String id;

    /**
     * {@code ReadWriteLock}.
     */
    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    /**
     * Grid instance.
     */
    private static final Ignite ignite;

    /**
     * Cache.
     */
    private final IgniteCache cache;

    static {
        boolean started = false;
        try {
            Ignition.ignite();
            started = true;
        } catch (IgniteIllegalStateException e) {
            log.debug("Using the Ignite instance that has been already started.");
        }
        if (started)
            ignite = Ignition.ignite();
        else {
            IgniteConfiguration igniteCfg = new IgniteConfiguration();
            igniteCfg.setGridName("testGrid");
            igniteCfg.setClientMode(false);
            igniteCfg.setIgniteHome("E:\\Portable\\Apache\\apache-ignite-fabric-1.7.0-bin");

            CacheConfiguration config = new CacheConfiguration();
            config.setName("myBatisCache");
            config.setCacheMode(CacheMode.LOCAL);
            config.setStatisticsEnabled(true);
            config.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
            igniteCfg.setCacheConfiguration(config);

            TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
            TcpDiscoveryJdbcIpFinder jdbcIpFinder = new TcpDiscoveryJdbcIpFinder();
            jdbcIpFinder.setDataSource((new LocalH2Config()).dataSource());
            tcpDiscoverySpi.setIpFinder(jdbcIpFinder);
            tcpDiscoverySpi.setLocalAddress("localhost");
            igniteCfg.setDiscoverySpi(tcpDiscoverySpi);

            TcpCommunicationSpi tcpCommunicationSpi = new TcpCommunicationSpi();
            tcpCommunicationSpi.setLocalAddress("localhost");
            igniteCfg.setCommunicationSpi(tcpCommunicationSpi);

            ignite = Ignition.start(igniteCfg);
        }
    }

    /**
     * Constructor.
     *
     * @param id Cache id.
     */
    @SuppressWarnings("unchecked")
    public IgniteCacheAdapter(String id) {
        if (id == null)
            throw new IllegalArgumentException("Cache instances require an ID");

        CacheConfiguration cacheCfg = new CacheConfiguration();
        cacheCfg.setName(id);
        cacheCfg.setCacheMode(CacheMode.LOCAL);
        cacheCfg.setStatisticsEnabled(true);
        cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

        cacheCfg.setEvictionPolicy(null);
        cacheCfg.setCacheLoaderFactory(null);
        cacheCfg.setCacheWriterFactory(null);

        // overrides template cache name with the specified id.
        cacheCfg.setName(id);

        cache = ignite.getOrCreateCache(cacheCfg);

        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getObject(Object key) {
        return cache.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return cache.size(CachePeekMode.PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
}
