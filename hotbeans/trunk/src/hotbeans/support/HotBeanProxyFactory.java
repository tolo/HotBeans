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

/*
 TODO: Introduce the concept of module/bean leases, that expire after certain time of inactivity  
 */
package hotbeans.support;

import hotbeans.BeanNotFoundException;
import hotbeans.HotBeanModule;
import hotbeans.ModuleNotFoundException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

/**
 * HotBeanProxyFactory is a proxy factory class used for creating proxies for hot beans. HotBeanProxyFactory is also the
 * method interceptor, which intercepts all method calls made through the created proxies. For each method call
 * interception, the reference to the target bean is validated. If the hot bean module of the target bean has been
 * inactivated, a reference to the lates hot bean module revision is obtained, and from that a new target bean.
 * 
 * @author Tobias Löfstrand
 */
public class HotBeanProxyFactory extends ProxyFactory implements MethodInterceptor {

   static final long serialVersionUID = 1806741813083763566L; // Simply to avoid warning...

   private final ProxyAccessHotBeanModuleRepository repository;

   private final String moduleName;

   private final String beanName;

   private HotBeanModule currentModule;

   private Object currentBean;

   private final Log logger = LogFactory.getLog(this.getClass());

   /**
    * Creates a new HotBeanProxyFactory.
    */
   public HotBeanProxyFactory(ProxyAccessHotBeanModuleRepository repository, String moduleName, String beanName,
            Class[] interfaces) {
      this(repository, moduleName, beanName, null, null, interfaces);
   }

   /**
    * Creates a new HotBeanProxyFactory.
    */
   public HotBeanProxyFactory(ProxyAccessHotBeanModuleRepository repository, String moduleName, String beanName,
            HotBeanModule currentModule, Object initalBean, Class[] interfaces) {
      this.repository = repository;
      this.moduleName = moduleName;
      this.beanName = beanName;
      this.currentModule = currentModule;

      super.addAdvice(this);
      super.setInterfaces(interfaces);

      this.currentBean = initalBean;

      super.setFrozen(true);
   }

   /**
    * Gets the name of the hot bean module from which the target bean should be fetched.
    */
   public String getModuleName() {
      return moduleName;
   }

   /**
    * Gets the target hot bean.
    */
   public String getBeanName() {
      return beanName;
   }

   /**
    * Gets the current hot bean module that this proxy factory is associated with.
    */
   public HotBeanModule getCurrentModule() {
      return currentModule;
   }

   /**
    * Call-back method invoked by the HotBeanModuleRepository implementation to update the current module/target bean
    * when {@link AbstractHotBeanModuleRepository#validateHotBeanProxyFactory(HotBeanProxyFactory)} is invoked.
    */
   public void updateHotBeanModuleAndBean(final HotBeanModule hotBeanModule, final Object hotBean) {
      this.currentModule = hotBeanModule;
      this.currentBean = hotBean;

      if (logger.isDebugEnabled())
         logger.debug("Updated module and bean reference in proxy - bean name: '" + this.beanName + "', module: "
                  + this.currentModule + ".");
   }

   /**
    * Called to invoke a method on the target bean.
    */
   public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
      if (logger.isDebugEnabled())
         logger.debug("Preparing to invoke method " + methodInvocation.getMethod().getName() + " on bean '"
                  + this.beanName + "' in module " + this.currentModule + ". Current bean: " + this.currentBean + ".");

      // Validate reference to module/bean
      this.repository.validateHotBeanProxyFactory(this);

      if (logger.isDebugEnabled())
         logger.debug("Invoking method " + methodInvocation.getMethod().getName() + " on bean '" + this.beanName
                  + "' in module " + this.currentModule + ". Current bean: " + this.currentBean + ".");

      if (this.currentBean != null) {
         try {
            this.currentModule.incrementUsageCount();

            return AopUtils.invokeJoinpointUsingReflection(this.currentBean, methodInvocation.getMethod(),
                     methodInvocation.getArguments());
         } finally {
            this.currentModule.decrementUsageCount();
         }
      } else {
         if (this.currentModule == null) throw new ModuleNotFoundException(this.moduleName, "Unable to find module '"
                  + this.moduleName + "'!");
         else throw new BeanNotFoundException(this.moduleName, this.beanName, "Unable to find bean '" + this.beanName
                  + "' in module '" + this.moduleName + "'!");
      }
   }

   /**
    * Gets a string representation of this HotBeanProxyFactory.
    */
   public String toString() {
      return "HotBeanProxyFactory@" + this.hashCode();
   }
}
