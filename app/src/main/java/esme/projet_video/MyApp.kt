package esme.projet_video

/**
 * Created by darrieu on 15/12/17.
 */
import android.app.Application;
import android.os.SystemClock;

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SystemClock.sleep(5000)
    }
}