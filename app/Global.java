import controllers.Application;
import play.GlobalSettings;
import services.ClassifyNews;


/**
 * Created by hugh_sd on 4/28/15.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {
        try {
            ClassifyNews.train(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStop(Application app) {
    }

}
