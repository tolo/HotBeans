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

import hotbeans.util.FileDeletor;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 */
public class AllTests {

   /**
    */
   public static void main(String[] args) {
      junit.textui.TestRunner.run(AllTests.suite());
   }

   /**
    */
   public static Test suite() {
      // Initial clean up
      FileDeletor.deleteTreeImpl("test/junit/hotModules");
      FileDeletor.deleteTreeImpl("junit.log");

      // Set level to WARN to minimize logging output (since logging configuration is hard coded in ant - the level must
      // be set in code)
      // Logger.getLogger("hotbeans").setLevel(Level.WARN);

      TestSuite suite = new TestSuite("All tests");

      // $JUnit-BEGIN$

      suite.addTestSuite(HotBeanModuleTest.class);
      suite.addTestSuite(HotBeanProxyFactoryTest.class);
      suite.addTestSuite(HotBeanModuleRepositoryTest.class);

      // $JUnit-END$

      return suite;
   }
}
