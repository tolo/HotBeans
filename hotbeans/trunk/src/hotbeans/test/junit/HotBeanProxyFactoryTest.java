/*
 * Copyright 2007 the project originators.
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
package hotbeans.test.junit;

import hotbeans.HotBeanContext;
import hotbeans.HotBeanModule;
import hotbeans.HotBeanModuleInfo;
import hotbeans.HotBeanModuleLoader;
import hotbeans.support.AbstractHotBeanModuleRepository;
import hotbeans.support.HotBeanProxyFactory;
import hotbeans.test.TestBeanInterface;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Test for HotBeanProxyFactory and AbstractHotBeanModuleRepository.
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanProxyFactoryTest extends TestCase {

   private final Log logger = LogFactory.getLog(this.getClass());

   public static class TestHotBeanModuleLoader implements HotBeanModuleLoader {

      private HotBeanModule hotBeanModule;

      public void init(HotBeanModule hotBeanModule) {
         this.hotBeanModule = hotBeanModule;
      }

      public void destroy() {
         hotBeanModule.unloaded();
      }

      public ClassLoader getClassLoader() {
         return this.getClass().getClassLoader();
      }
   }

   public static class TestHotBeanContext implements HotBeanContext {

      private Object bean;

      public TestHotBeanContext(Object bean) {
         this.bean = bean;
      }

      public void init() throws Exception {
      }

      public void destroy() {
      }

      public boolean hasHotBean(String beanName) {
         return true;
      }

      public Object getHotBean(String beanName) {
         return bean;
      }

      public Class getHotBeanClass(String beanName) {
         return bean.getClass();
      }

      public String getHotBeanClassName(String beanName) {
         return bean.getClass().getName();
      }
   }

   public static class TestHotBeanModuleRepository extends AbstractHotBeanModuleRepository {

      public HotBeanModuleInfo addHotBeanModule(InputStream moduleFile) {
         return null;
      }

      public void removeHotBeanModule(String moduleName) {
      }

      public HotBeanModuleInfo revertHotBeanModule(String moduleName, long revision) {
         return null;
      }

      public HotBeanModuleInfo updateHotBeanModule(String moduleName, InputStream moduleFile) {
         return null;
      }

      public void registerHotBeanModule(HotBeanModule module) {
         super.registerHotBeanModule(module);
      }
   }

   public static class TestBean implements TestBeanInterface {

      private int id;

      public TestBean(int id) {
         this.id = id;
      }

      public int getTestBeanId() {
         return id;
      }
   }

   private TestHotBeanModuleRepository testHotBeanModuleRepository;

   private TestBean bean1;

   private TestBean bean2;

   private HotBeanModule hotBeanModule1;

   private HotBeanModule hotBeanModule2;

   private HotBeanProxyFactory hotBeanProxyFactory;

   private TestBeanInterface proxy;

   /**
    */
   protected void setUp() {
      this.testHotBeanModuleRepository = new TestHotBeanModuleRepository();

      this.bean1 = new TestBean(1);
      this.bean2 = new TestBean(2);

      TestHotBeanModuleLoader testHotBeanModuleLoader = new TestHotBeanModuleLoader();
      TestHotBeanContext testHotBeanContext = new TestHotBeanContext(this.bean1);

      this.hotBeanModule1 = new HotBeanModule(new HotBeanModuleInfo("pfTest", null, 1, "1",
               System.currentTimeMillis() - 60000), testHotBeanModuleLoader, testHotBeanContext);
      testHotBeanModuleLoader.init(this.hotBeanModule1);

      testHotBeanModuleLoader = new TestHotBeanModuleLoader();
      testHotBeanContext = new TestHotBeanContext(this.bean2);

      this.hotBeanModule2 = new HotBeanModule(
               new HotBeanModuleInfo("pfTest", null, 2, "2", System.currentTimeMillis()), testHotBeanModuleLoader,
               testHotBeanContext);
      testHotBeanModuleLoader.init(this.hotBeanModule2);

      this.testHotBeanModuleRepository.registerHotBeanModule(this.hotBeanModule1);

      this.hotBeanProxyFactory = this.testHotBeanModuleRepository.getHotBeanProxyFactory("pfTest", "test",
               new Class[] { TestBeanInterface.class });
      this.proxy = (TestBeanInterface) this.hotBeanProxyFactory.getProxy();
   }

   /**
    */
   protected void tearDown() {
   }

   /* ### TESTS ### */

   public void testModuleUpdate() {
      if (logger.isDebugEnabled()) logger.debug("*** Begin testModuleUpdate ***");

      if (this.proxy.getTestBeanId() != 1)
         super.fail("Wrong id for bean - expected 1 but got " + this.proxy.getTestBeanId() + "!");

      this.testHotBeanModuleRepository.registerHotBeanModule(hotBeanModule2);
      hotBeanModule1.inactivate();

      if (this.proxy.getTestBeanId() != 2)
         super.fail("Wrong id for bean - expected 2 but got " + this.proxy.getTestBeanId() + "!");

      if (logger.isDebugEnabled()) logger.debug("*** End testModuleUpdate ***");
   }
}
