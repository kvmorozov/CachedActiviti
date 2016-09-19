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
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.locks.ReadWriteLock;

public final class IgniteCacheAdapter implements Cache {

    private static final String DEFAULT_CACHE_NAME = "myBatisCache";
    /**
     * {@code ReadWriteLock}.
     */
    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    @Autowired
    private IgniteProvider igniteProvider;

    /**
     * Cache.
     */
    private final IgniteCache cache;

    public static final IgniteCacheAdapter INSTANCE = new IgniteCacheAdapter();

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    private IgniteCacheAdapter() {
        cache = igniteProvider.getIgnite().getOrCreateCache(DEFAULT_CACHE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return DEFAULT_CACHE_NAME;
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
