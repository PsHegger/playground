package pshegger.github.io.playground.gamedev

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import pshegger.github.io.playground.R
import pshegger.github.io.playground.gamedev.scenes.menu.MainMenuScene

class GameDevActivity : AppCompatActivity() {

    private lateinit var gameSurface: GameSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_dev)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        gameSurface = findViewById(R.id.gameSurface)
        gameSurface.scene = MainMenuScene(gameSurface)
    }

    override fun onBackPressed() {
        val wasHandled = gameSurface.scene?.onBackPressed() ?: false

        if (!wasHandled) {
            super.onBackPressed()
        }
    }
}
