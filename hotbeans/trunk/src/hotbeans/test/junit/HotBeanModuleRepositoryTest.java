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

import hotbeans.HotBeanModuleInfo;
import hotbeans.HotBeanModuleRepository;
import hotbeans.support.AbstractHotBeanModuleRepository;
import hotbeans.test.TestBeanInterface;
import hotbeans.util.FileDeletor;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanModuleRepositoryTest extends AbstractDependencyInjectionSpringContextTests {

   private static final String MODULE_NAME = "TestModule";

   private static final String BEAN_NAME = "MyLittleTestBean";

   protected HotBeanModuleRepository HotBeanModuleRepository1;

   protected HotBeanModuleRepository HotBeanModuleRepository2;

   private final Log logger = LogFactory.getLog(this.getClass());

   /**
    */
   public HotBeanModuleRepositoryTest() {
      super.setPopulateProtectedVariables(true);
   }

   /**
    */
   protected String[] getConfigLocations() {
      return new String[] { "hotbeans/test/junit/applicationContext-HotBeanModuleRepositoryTest.xml" };
   }

   /**
    */
   protected void onSetUp() throws Exception {
      super.onSetUp();

      FileDeletor.deleteTreeImpl("test/junit/hotModules");
      new File("test/junit/hotModules").mkdirs();
   }

   /**
    */
   protected void onTearDown() throws Exception {
      if (this.HotBeanModuleRepository1 instanceof AbstractHotBeanModuleRepository) ((AbstractHotBeanModuleRepository) this.HotBeanModuleRepository1)
               .destroy();
      else if (this.HotBeanModuleRepository1 instanceof DisposableBean)
         ((DisposableBean) this.HotBeanModuleRepository1).destroy();

      if (this.HotBeanModuleRepository2 instanceof AbstractHotBeanModuleRepository) ((AbstractHotBeanModuleRepository) this.HotBeanModuleRepository2)
               .destroy();
      else if (this.HotBeanModuleRepository2 instanceof DisposableBean)
         ((DisposableBean) this.HotBeanModuleRepository2).destroy();

      logger.info("Cleaning up files.");

      // Clean up
      // FileDeletor.deleteTreeImpl("test/junit/hotModules");

      logger.info("Clean up complete.");
   }

   /* ### TESTS ### */

   public void testAddRemoveUpdateRevertSynch() throws Exception {
      if (logger.isDebugEnabled()) logger.debug("*** Begin testAddRemoveUpdateRevertSynch ***");

      int exprectedRevision = -1;
      int idVersion1 = -1;
      int idVersion2 = -1;

      // ###
      // ADD BROKEN MODULE
      // ###
      if (logger.isDebugEnabled()) logger.debug("Adding broken module '" + MODULE_NAME + "'.");
      HotBeanModuleRepository1.addHotBeanModule(new FileInputStream(new File("samples/simple/testmodule/testModuleBroken.jar")
               .getAbsoluteFile()));

      TestBeanInterface testInterface = (TestBeanInterface) HotBeanModuleRepository1.getHotBean(MODULE_NAME, BEAN_NAME,
               TestBeanInterface.class);

      boolean success = false;
      try {
         System.out.println("ID: " + testInterface.getTestBeanId());
      } catch (Exception e) {
         success = true;
      }

      if (!success) super.fail("Method getId should throw exception for broken module!");
      else if (logger.isDebugEnabled()) logger.debug("Broken module added successfully.");

      // Increment exprected revision
      exprectedRevision++;

      // ###
      // UPDATE TO WORKING MODULE
      // ###
      if (logger.isDebugEnabled()) logger.debug("Updating to working module '" + MODULE_NAME + "'.");
      HotBeanModuleRepository1.updateHotBeanModule(MODULE_NAME, new FileInputStream("samples/simple/testmodule/testModule.jar"));

      TestBeanInterface testInterface1 = (TestBeanInterface) HotBeanModuleRepository1.getHotBean(MODULE_NAME,
               BEAN_NAME, TestBeanInterface.class);

      success = false;

      for (int i = 0; i < 12; i++) {
         try {
            idVersion1 = testInterface1.getTestBeanId();
            success = true;
            break;
         } catch (Exception e) {
            logger.error("Error invoking TestBeanInterface.getTestBeanId - " + e);
         }

         try {
            Thread.sleep(2500);
         } catch (Exception e) {
         }
      }

      if (!success) super.fail("Module not added!");
      else if (logger.isDebugEnabled()) logger.debug("Working module added successfully.");

      // Increment exprected revision
      exprectedRevision++;

      // Check that getHotBeanClass and getHotBeanClassName doesn't return null
      Class handlerClass = HotBeanModuleRepository1.getHotBeanClass(MODULE_NAME, BEAN_NAME);
      assertNotNull(handlerClass);

      String handlerClassName = HotBeanModuleRepository1.getHotBeanClassName(MODULE_NAME, BEAN_NAME);
      assertNotNull(handlerClassName);

      // ###
      // UPDATE AGAIN
      // ###
      if (logger.isDebugEnabled()) logger.debug("Updating module '" + MODULE_NAME + "' again.");
      HotBeanModuleRepository2.updateHotBeanModule(MODULE_NAME, new FileInputStream("samples/simple/testmodule/testModule.jar"));

      success = false;

      for (int i = 0; i < 12; i++) {
         try {
            idVersion2 = testInterface1.getTestBeanId();
            if (idVersion2 != idVersion1) {
               success = true;
               break;
            }
         } catch (Exception e) {
            logger.error("Error invoking methods on TestBeanInterface - " + e);
         }

         try {
            Thread.sleep(2500);
         } catch (Exception e) {
         }
      }

      if (!success) super.fail("Module not updated!");
      if (logger.isDebugEnabled()) logger.debug("Module updated successfully.");

      // Increment exprected revision
      exprectedRevision++;

      // ###
      // REVERT
      // ###
      if (logger.isDebugEnabled()) logger.debug("Reverting module 'test' to revision 1.");
      HotBeanModuleRepository1.revertHotBeanModule(MODULE_NAME, 1);
      if (logger.isDebugEnabled()) logger.debug("Done reverting module 'test' to revision 1.");

      for (int i = 0; i < 12; i++) {
         try {
            if (testInterface1.getTestBeanId() != idVersion2) break;
         } catch (Exception e) {
            logger.error("Error invoking methods on TestBeanInterface - " + e);
         }

         try {
            Thread.sleep(2500);
         } catch (Exception e) {
         }
      }

      if (logger.isDebugEnabled()) logger.debug("Done checking version of reverted module.");

      // Increment exprected revision
      exprectedRevision++;

      if (HotBeanModuleRepository1.getCurrentHotBeanModuleInfo(MODULE_NAME).getRevision() != exprectedRevision)
         super.fail("Module not reverted!");
      if ((testInterface1.getTestBeanId() == idVersion1) || ((testInterface1.getTestBeanId() == idVersion2)))
         super.fail("Module not properly reverted!");
      if (logger.isDebugEnabled()) logger.debug("Module reverted successfully.");

      // ###
      // CHECK HotBeanModuleRepository2
      // ###
      if (logger.isDebugEnabled())
         logger.debug("Checking that HotBeanModuleRepository2 is in synch with HotBeanModuleRepository1");
      success = false;

      for (int i = 0; i < 12; i++) {
         try {
            if (HotBeanModuleRepository2.getCurrentHotBeanModuleInfo(MODULE_NAME).getRevision() == exprectedRevision) {
               success = true;
               break;
            }
         } catch (Exception e) {
         }

         try {
            Thread.sleep(2500);
         } catch (Exception e) {
         }
      }

      if (!success
               || (HotBeanModuleRepository2.getCurrentHotBeanModuleInfo(MODULE_NAME).getRevision() != exprectedRevision)) super
               .fail("HotBeanModuleRepository2 not in synch with HotBeanModuleRepository1!");
      else if (logger.isDebugEnabled())
         logger.debug("HotBeanModuleRepository2 is in synch with HotBeanModuleRepository1");

      // ###
      // REMOVE
      // ###
      testInterface1 = null; // Release reference
      if (logger.isDebugEnabled()) logger.debug("Removing module.");
      HotBeanModuleRepository1.removeHotBeanModule(MODULE_NAME);
      if (logger.isDebugEnabled()) logger.debug("Waiting.....");
      Thread.sleep(1500);

      if (logger.isDebugEnabled()) logger.debug("Checking if module exists in repository 1.");
      HotBeanModuleInfo[] info = HotBeanModuleRepository1.getHotBeanModuleInfo(MODULE_NAME);
      if ((info != null) && (info.length > 0))
         super.fail("Module '" + MODULE_NAME + "' isn't removed from repository 1 properly!");

      if (logger.isDebugEnabled()) logger.debug("Checking if module exists in repository 2.");
      info = HotBeanModuleRepository2.getHotBeanModuleInfo(MODULE_NAME);
      if ((info != null) && (info.length > 0))
         super.fail("Module '" + MODULE_NAME + "' isn't removed from repository 2 properly!");

      if (logger.isDebugEnabled()) logger.debug("*** End testAddRemoveUpdateRevertSynch ***");
   }
}
