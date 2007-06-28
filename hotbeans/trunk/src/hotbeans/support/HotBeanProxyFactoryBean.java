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

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory bean implementation facilitating the use of a {@link HotBeanProxyFactory} in a Spring application context.
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanProxyFactoryBean implements InitializingBean, FactoryBean {

   private ProxyAccessHotBeanModuleRepository hotBeanModuleRepository;

   private String moduleName;

   private String beanName;

   private Class[] interfaces;

   private HotBeanProxyFactory proxyFactory;

   private Object proxy;

   /**
    * Invoked by a BeanFactory after it has set all bean properties.
    */
   public final void afterPropertiesSet() throws Exception {
      this.proxyFactory = this.hotBeanModuleRepository.getHotBeanProxyFactory(this.moduleName, this.beanName,
               this.interfaces);
   }

   /**
    * Checks if the bean managed by this factory is a singleton.
    */
   public final boolean isSingleton() {
      return true;
   }

   /**
    * Gets a proxy for a bean in a hot bean module.
    */
   public synchronized final Object getObject() throws Exception {
      if (this.proxy == null) this.proxy = this.proxyFactory.getProxy();
      return this.proxy;
   }

   /**
    * Return the type of object that this FactoryBean creates.
    */
   public Class getObjectType() {
      return null;
   }

   /**
    * Gets the associated HotBeanModuleRepository.
    */
   public ProxyAccessHotBeanModuleRepository getHotBeanModuleRepository() {
      return hotBeanModuleRepository;
   }

   /**
    * Sets the associated HotBeanModuleRepository.
    */
   public void setHotBeanModuleRepository(ProxyAccessHotBeanModuleRepository hotBeanModuleRepository) {
      this.hotBeanModuleRepository = hotBeanModuleRepository;
   }

   /**
    * Gets the target bean name.
    */
   public String getBeanName() {
      return beanName;
   }

   /**
    * Sets the target bean name.
    */
   public void setBeanName(String beanName) {
      this.beanName = beanName;
   }

   /**
    * Gets the target hot bean module name.
    */
   public String getModuleName() {
      return moduleName;
   }

   /**
    * Sets the target hot bean module name.
    */
   public void setModuleName(String moduleName) {
      this.moduleName = moduleName;
   }

   /**
    * Gets the interfaces to be implemented by a proxy.
    */
   public Class[] getInterfaces() {
      return interfaces;
   }

   /**
    * Sets the interfaces to be implemented by a proxy.
    */
   public void setInterfaces(Class[] interfaces) {
      this.interfaces = interfaces;
   }

   /**
    * Gets the interfaces class names to be implemented by a proxy.
    * 
    * @since 1.0 (20060607)
    */
   public List getInterfaceClassNames() {
      if (this.interfaces != null) {
         List classNames = new ArrayList(this.interfaces.length);
         for (int i = 0; i < this.interfaces.length; i++) {
            classNames.add(this.interfaces[i].getName());
         }
         return classNames;
      } else return null;
   }

   /**
    * Sets the interfaces class names to be implemented by a proxy.
    * 
    * @since 1.0 (20060607)
    */
   public void setInterfaceClassNames(final List interfaceClassNames) throws ClassNotFoundException {
      if (interfaceClassNames != null) {
         this.interfaces = AopUtils.toInterfaceArray((String[]) interfaceClassNames
                  .toArray(new String[interfaceClassNames.size()]));
      } else this.interfaces = null;
   }
}
