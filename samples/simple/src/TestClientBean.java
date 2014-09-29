import hotbeans.test.TestBeanInterface;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.InitializingBean;

/**
 * 
 */
public class TestClientBean implements InitializingBean {

   /**
    */
   class TestBeanTask extends TimerTask
   {
      public void run()
      {
         System.out.println("Test bean id is: " + testInterface.getTestBeanId());
      }
   }

   private Timer timer;
      
   TestBeanInterface testInterface;

      
   public TestBeanInterface getTestInterface()
   {
      return testInterface;
   }
   
   public void setTestInterface(TestBeanInterface testInterface)
   {
      this.testInterface = testInterface;
   }
   
   public void afterPropertiesSet() throws Exception
   {
      this.timer = new Timer(true);
      this.timer.scheduleAtFixedRate(new TestBeanTask(), 5000, 5000);
   }
}
