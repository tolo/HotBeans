import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * @author Tobias Löfstrand
 */
public class TestClient {

   /**
    */
   public static void main(String[] args) {
      try {
         new FileSystemXmlApplicationContext("spring.xml");
         while(true) Thread.sleep(100000000000L);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
