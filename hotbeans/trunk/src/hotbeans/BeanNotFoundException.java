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
package hotbeans;

/**
 * Exception class thrown when a bean isn't found.
 * 
 * @author Tobias Löfstrand
 */
public class BeanNotFoundException extends HotBeansException {

   private static final long serialVersionUID = 2244208705807897895L;

   private String moduleName;

   private String beanName;

   /**
    * Creates a new BeanNotFoundException.
    */
   public BeanNotFoundException(String moduleName, String beanName, String message) {
      super(message);

      this.moduleName = moduleName;
      this.beanName = beanName;
   }

   /**
    * Creates a new BeanNotFoundException.
    */
   public BeanNotFoundException(String moduleName, String beanName, String message, Throwable cause) {
      super(message, cause);

      this.moduleName = moduleName;
      this.beanName = beanName;
   }

   /**
    * Gets the associated module name.
    */
   public String getModuleName() {
      return moduleName;
   }

   /**
    * Gets the associated bean name.
    */
   public String getBeanName() {
      return beanName;
   }
}
