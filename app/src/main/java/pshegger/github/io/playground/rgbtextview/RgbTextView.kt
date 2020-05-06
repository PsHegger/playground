package pshegger.github.io.playground.rgbtextview

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import pshegger.github.io.playground.R
import pshegger.github.io.playground.utils.removeDiacritics
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

class RgbTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatTextView(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    var colorMode: ColorMode? = ColorMode.RGB
        set(value) {
            field = value
            text = text
        }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RgbTextView,
            0, 0
        )

        val modeIndex = a.getInt(R.styleable.RgbTextView_colorMode, 0)
        colorMode = ColorMode.values()[modeIndex]

        a.recycle()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(colorizeText(text), type)
    }

    private fun colorizeText(text: CharSequence?): CharSequence? = text?.let { text ->
        val builder = SpannableStringBuilder()
        text.split(" ").forEach { word ->
            builder.append(
                word,
                ForegroundColorSpan(calculateTextColor(word)),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.append(" ")
        }
        builder.trimEnd()
    }

    private fun calculateTextColor(word: String): Int {
        val hexes = word.removeDiacritics()
            .map { it.toInt() }
            .filter { it in hexCodes }
            .map { hexCodes.indexOf(it) }
        val startOffset = hexes.size / 3
        val digitOverlap = hexes.size % 3
        val maxDigits = startOffset + digitOverlap

        val colorHexes = (0..2).map { i ->
            hexes.subList(startOffset * i, startOffset * i + maxDigits)
        }

        return when (colorMode) {
            ColorMode.RGB -> calculateRgbColor(colorHexes)
            ColorMode.HSV -> calculateHsvColor(colorHexes)
            ColorMode.SEED -> calculateSeedColor(colorHexes)
            else -> textColors.defaultColor
        }
    }

    private fun calculateColorComponent(digits: List<Int>): Float {
        if (digits.isEmpty()) return 0f

        val value = digits.foldRight(Pair(0, 0f)) { d, (index, acc) ->
            Pair(index + 1, acc + d * 16f.pow(index))
        }.second
        val maxValue = (16f.pow(digits.size) - 1)
        return (value / maxValue)
    }

    private fun calculateRgbColor(colorHexes: List<List<Int>>): Int = Color.rgb(
        (255 * calculateColorComponent(colorHexes[0])).roundToInt(),
        (255 * calculateColorComponent(colorHexes[1])).roundToInt(),
        (255 * calculateColorComponent(colorHexes[2])).roundToInt()
    )

    private fun calculateHsvColor(colorHexes: List<List<Int>>): Int = Color.HSVToColor(
        floatArrayOf(
            360 * calculateColorComponent(colorHexes[0]),
            calculateColorComponent(colorHexes[1]),
            calculateColorComponent(colorHexes[2])
        )
    )

    private fun calculateSeedColor(colorHexes: List<List<Int>>): Int {
        val seed = (calculateColorComponent(colorHexes.flatten()) * Long.MAX_VALUE).roundToLong()
        val rng = Random(seed)

        return Color.rgb(
            rng.nextInt(256),
            rng.nextInt(256),
            rng.nextInt(256)
        )
    }

    enum class ColorMode {
        RGB, HSV, SEED
    }

    companion object {
        private val hexCodes: List<Int> = ('0'.toInt()..'9'.toInt()) + ('a'.toInt()..'f'.toInt())
    }
}
