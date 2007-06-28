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
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanModuleTest extends TestCase {

   private final Log logger = LogFactory.getLog(this.getClass());

   static class TestHotBeanModuleLoader implements HotBeanModuleLoader {

      boolean destroyCalled = false;

      private HotBeanModule hotBeanModule;

      public void init(HotBeanModule hotBeanModule) {
         this.hotBeanModule = hotBeanModule;
      }

      public void destroy() {
         destroyCalled = true;
         hotBeanModule.unloaded();
      }

      public ClassLoader getClassLoader() {
         return this.getClass().getClassLoader();
      }
   }

   static class TestHotBeanContext implements HotBeanContext {

      boolean destroyCalled = false;

      public void init() throws Exception {
      }

      public void destroy() {
         destroyCalled = true;
      }

      public boolean hasHotBean(String beanName) {
         return true;
      }

      public Object getHotBean(String beanName) {
         if ("test".equals(beanName)) return this;
         else return null;
      }

      public Class getHotBeanClass(String beanName) {
         return null;
      }

      public String getHotBeanClassName(String beanName) {
         return null;
      }
   }

   private TestHotBeanModuleLoader testHotBeanModuleLoader;

   private TestHotBeanContext testHotBeanContext;

   private HotBeanModule hotBeanModule;

   /**
    */
   protected void setUp() {
      this.testHotBeanModuleLoader = new TestHotBeanModuleLoader();
      this.testHotBeanContext = new TestHotBeanContext();

      this.hotBeanModule = new HotBeanModule(new HotBeanModuleInfo("test", null, 1, "1", System.currentTimeMillis()),
               this.testHotBeanModuleLoader, this.testHotBeanContext);

      this.testHotBeanModuleLoader.init(this.hotBeanModule);
   }

   /**
    */
   protected void tearDown() {
   }

   /* ### TESTS ### */

   public void testUsageCount() {
      if (logger.isDebugEnabled()) logger.debug("*** Begin testUsageCount ***");

      if (this.hotBeanModule.inUse()) super.fail("Module is in use before used!");

      this.hotBeanModule.incrementUsageCount();
      this.hotBeanModule.decrementUsageCount();
      this.hotBeanModule.incrementUsageCount();
      this.hotBeanModule.incrementUsageCount();
      this.hotBeanModule.decrementUsageCount();
      this.hotBeanModule.decrementUsageCount();

      if (this.hotBeanModule.inUse()) super.fail("Module should not be in use!");

      if (logger.isDebugEnabled()) logger.debug("*** End testUsageCount ***");
   }

   public void testUnload() {
      if (logger.isDebugEnabled()) logger.debug("*** Begin testUnload ***");

      this.hotBeanModule.unload();

      try {
         Thread.sleep(100);
      } catch (Exception e) {
      }

      if (!this.testHotBeanModuleLoader.destroyCalled) super.fail("Loader is not destroyed!");
      if (!this.testHotBeanContext.destroyCalled) super.fail("Context is not destroyed!");

      if (!this.hotBeanModule.isUnloaded()) super.fail("Module is not unloaded!");

      if (logger.isDebugEnabled()) logger.debug("*** End testUnload ***");
   }

   public void testGetBean() {
      if (logger.isDebugEnabled()) logger.debug("*** Begin testGetBean ***");

      if (this.hotBeanModule.getHotBean("test") != this.testHotBeanContext) super.fail("Wrong bean returned!");

      if (logger.isDebugEnabled()) logger.debug("*** End testGetBean ***");
   }
}
