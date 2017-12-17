package esme.projet_video

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by darrieu on 15/12/17.
 */



class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, bluetooth::class.java)
        startActivity(intent)
        finish()
    }
}