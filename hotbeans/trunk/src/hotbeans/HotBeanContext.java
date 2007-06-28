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
 * This class represents the context in which a hot bean module exists.
 * 
 * @author Tobias Löfstrand
 */
public interface HotBeanContext {

   /**
    * Initializes this context.
    */
   public void init() throws Exception;

   /**
    * Called to destroy this context.
    */
   public void destroy();

   /**
    * Checks if this context contains the bean with the specified name.
    * 
    * @since 1.0.1 (20070302)
    */
   public boolean hasHotBean(final String beanName);

   /**
    * Gets the bean with the specified name from this context.
    */
   public Object getHotBean(String beanName);

   /**
    * Gets the class name of the bean with the specified name from this context.
    * 
    * @since 1.0.1 (20070212
    */
   public String getHotBeanClassName(String beanName);

   /**
    * Gets the class of the bean with the specified name from this context.
    * 
    * @since 1.0.1 (20070212)
    */
   public Class getHotBeanClass(String beanName);
}
