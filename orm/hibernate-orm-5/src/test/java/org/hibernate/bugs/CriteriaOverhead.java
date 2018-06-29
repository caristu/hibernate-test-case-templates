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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.BatchFetchStyle;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;
import org.openbravo.model.ad.datamodel.Table;

/**
 * Test case to measure the overhead added by the MetaModelImpl.getImplementors() to the criteria
 * executions. It is intended to run with and without the fix for
 * https://hibernate.atlassian.net/browse/HHH-11495 in order to compare the execution time of the
 * test.
 */
public class CriteriaOverhead extends BaseCoreFunctionalTestCase {

  private static final int LOOPS = 50_000;

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
  public void criteriaTest() throws Exception {
    Configuration config = constructConfiguration();
    config.setProperty(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, Integer.toString(50));
    config.setProperty(AvailableSettings.BATCH_FETCH_STYLE, BatchFetchStyle.LEGACY.toString());
    BootstrapServiceRegistry bootRegistry = buildBootstrapServiceRegistry();
    StandardServiceRegistryImpl serviceRegistry = buildServiceRegistry(bootRegistry, config);
    addMappings(config);
    applyCacheSettings(config);
    afterConfigurationBuilt(config);
    SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) config
        .buildSessionFactory(serviceRegistry);

    multiThreadCriteria(sessionFactory.openSession());

    sessionFactory.close();
    serviceRegistry.destroy();
  }

  private void multiThreadCriteria(Session theSession) throws InterruptedException {
    for (int threads = 1; threads <= 8; threads++) {
      ExecutorService executor = Executors.newFixedThreadPool(threads);
      List<CallableCriteria> criterias = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        criterias.add(new CallableCriteria(LOOPS / threads, theSession));
      }
      System.out.println("*** Starting Criteria execution with " + threads + " threads ***");
      long t = System.currentTimeMillis();
      executor.invokeAll(criterias, 1, TimeUnit.HOURS);
      System.out.println("*** Criteria with " + threads + " threads done in "
          + (System.currentTimeMillis() - t) + " ms ***");
    }
  }

  private class CallableCriteria implements Callable<Void> {
    private int loops;
    private Session theSession;

    public CallableCriteria(int loops, Session theSession) {
      this.loops = loops;
      this.theSession = theSession;
    }

    @Override
    public Void call() throws Exception {
      System.out.println("Starting {} OBCriteria executions..." + loops);
      long t = System.currentTimeMillis();
      for (int i = 0; i < loops; i++) {
        OBCriteria crit = new OBCriteria(Table.class.getName(),
            (SharedSessionContractImplementor) theSession);
        crit.list();
      }
      System.out.println("OBCriteria: " + loops + " executions, {} ms"
          + (System.currentTimeMillis() - t));
      return null;
    }
  }

  private class OBCriteria extends CriteriaImpl {

    private static final long serialVersionUID = 1L;

    public OBCriteria(String entityOrClassName, SharedSessionContractImplementor session) {
      super(entityOrClassName, session);
    }
  }
}

