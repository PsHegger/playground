package pshegger.github.io.playground.rgbtextview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_rgb_text_view.*
import pshegger.github.io.playground.R

class RgbTextViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rgb_text_view)

        colorModeSelector.setOnCheckedChangeListener { _, checkedId ->
            rgbTextView.colorMode = when (checkedId) {
                R.id.colorModeRgb -> RgbTextView.ColorMode.RGB
                R.id.colorModeHsv -> RgbTextView.ColorMode.HSV
                R.id.colorModeSeed -> RgbTextView.ColorMode.SEED
                else -> null
            }
        }

        textModeSelector.setOnCheckedChangeListener { _, checkedId ->
            customText.isEnabled = checkedId == R.id.textModeCustom
            rgbTextView.text = when (checkedId) {
                R.id.textModeLoremIpsum -> getString(R.string.rgbtv_lorem_ipsum_text)
                R.id.textModeHarryPotter -> getString(R.string.rgbtv_harry_potter_text)
                else -> {
                    customText.requestFocus()
                    customText.text
                }
            }
        }

        customText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        customText.addTextChangedListener { text ->
            if (text != null && textModeSelector.checkedRadioButtonId == R.id.textModeCustom) {
                rgbTextView.text = text
            }
        }
    }
}
