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
package hotbeans.support;

import hotbeans.HotBeanContext;

import java.beans.Introspector;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * HotBeanContext implementation that uses a Spring application context for definition of beans.
 * 
 * @author Tobias Löfstrand
 */
public class SpringHotBeanContext extends ClassPathXmlApplicationContext implements HotBeanContext {

   private ClassLoader classLoader;

   private volatile boolean initialized = false;

   /**
    * Creates a new SpringHotBeanContext.
    */
   public SpringHotBeanContext(ClassLoader classLoader) throws BeansException {
      super(new String[] { "hotBeanContext.xml" }, false);
      this.classLoader = classLoader;
   }

   /**
    * Overriden to set the class loader on the XmlBeanDefinitionReader.
    */
   protected void initBeanDefinitionReader(final XmlBeanDefinitionReader beanDefinitionReader) {
      super.initBeanDefinitionReader(beanDefinitionReader);

      // Set the bean class loader of beanDefinitionReader to the classloader used by the module
      beanDefinitionReader.setBeanClassLoader(this.classLoader);
   }

   /**
    * Gets the classloader used by this SpringHotBeanContext.
    */
   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   /**
    * Sets the classloader used by this SpringHotBeanContext.
    */
   public void setClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   /**
    * Initializes this context.
    */
   public void init() throws Exception {
      if (!this.initialized) {
         this.initialized = true;
         super.refresh();
      }
   }

   /**
    * Destroys this context.
    */
   public void destroy() {
      if (this.initialized) {
         super.destroy();

         LogFactory.release(this.classLoader);

         Introspector.flushCaches();

         this.initialized = false;
      }

      this.classLoader = null;
   }

   /**
    * Checks if this context contains the bean with the specified name.
    * 
    * @since 1.0.1 (20070302)
    */
   public boolean hasHotBean(final String beanName) {
      return super.containsBean(beanName);
   }

   /**
    * Gets the bean with the specified name.
    */
   public Object getHotBean(final String beanName) {
      return super.getBean(beanName);
   }

   /**
    * Gets the class name of the bean with the specified name from this context.
    * 
    * @since 1.0.1 (20070212)
    */
   public String getHotBeanClassName(final String beanName) {
      return ((AbstractBeanDefinition) super.getBeanFactory().getBeanDefinition(beanName)).getBeanClassName();
   }

   /**
    * Gets the class of the bean with the specified name from this context.
    * 
    * @since 1.0.1 (20070212)
    */
   public Class getHotBeanClass(final String beanName) {
      return super.getType(beanName);
   }
}
