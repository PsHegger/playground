package pshegger.github.io.playground.gamedev

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import pshegger.github.io.playground.R
import pshegger.github.io.playground.gamedev.scenes.menu.MainMenuScene

class GameDevActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: Activity
    }

    lateinit var gameSurface: GameSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_dev)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val contentView = findViewById<View>(android.R.id.content)

        contentView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        instance = this

        gameSurface = findViewById(R.id.gameSurface)
        gameSurface.scene = MainMenuScene(gameSurface)
    }

    override fun onBackPressed() {
        gameSurface.scene?.onBackPressed() ?: super.onBackPressed()
    }
}
