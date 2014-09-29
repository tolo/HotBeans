import hotbeans.test.TestBeanInterface;

/**
 * 
 */
public class MyLittleTestBean implements TestBeanInterface {

   public int getTestBeanId() {
      return System.identityHashCode(this);
   }
}
