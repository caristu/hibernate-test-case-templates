/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import static org.junit.Assert.assertTrue;

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.BatchFetchStyle;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

public class BatchFetchStyleTestCase extends BaseCoreFunctionalTestCase {

	// If you use *.hbm.xml mappings, instead of annotations, add the mappings here.
	@Override
	protected String[] getMappings() {
		return new String[] { "myMapping.hbm" };
	}

	// If those mappings reside somewhere other than resources/org/hibernate/test,
	// change this.
	@Override
	protected String getBaseForMappings() {
		return "org/hibernate/test/";
	}

	@Override
	protected void buildSessionFactory() {
		// Do nothing, let's control it ourselves
	}

	@Test
	public void batchFetchStyleHeapSizeSize10Test() throws Exception {
		testDifferentStyles(10);
	}

	@Test
	public void batchFetchStyleHeapSizeSize50Test() throws Exception {
		testDifferentStyles(50);
	}

	private void testDifferentStyles(int fetchSize) {
		long heapLegacy = buildSessionFactoryAndGetHeap(BatchFetchStyle.LEGACY, fetchSize);
		long heapDynamic = buildSessionFactoryAndGetHeap(BatchFetchStyle.DYNAMIC, fetchSize);
		assertTrue(
				"fetch size:" + fetchSize + " - legacy style heap (" + heapLegacy
						+ " MB) shouldn't be more than twice bigger than dyamic size (" + heapDynamic + " MB)",
				heapLegacy / 2 <= heapDynamic);
	}

	private long buildSessionFactoryAndGetHeap(BatchFetchStyle fetchStyle, int fetchSize) {
		System.out.println("Building session factory with fetch style " + fetchStyle + " size: " + fetchSize);
		long t = System.currentTimeMillis();
		Configuration config = constructConfiguration();
		config.setProperty(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, Integer.toString(fetchSize));
		config.setProperty(AvailableSettings.BATCH_FETCH_STYLE, fetchStyle.toString());
		BootstrapServiceRegistry bootRegistry = buildBootstrapServiceRegistry();
		StandardServiceRegistryImpl serviceRegistry = buildServiceRegistry(bootRegistry, config);
		addMappings(config);
		applyCacheSettings(config);
		afterConfigurationBuilt(config);
		SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) config
				.buildSessionFactory(serviceRegistry);

		System.gc();
		long usedHeap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

		System.out.println(fetchStyle + ": " + usedHeap + " MB - " + (System.currentTimeMillis() - t) + " ms");

		sessionFactory.close();
		serviceRegistry.destroy();
		return usedHeap;
	}
}
