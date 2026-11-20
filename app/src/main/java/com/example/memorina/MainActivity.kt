package com.example.memorina

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.min

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var grid: GridLayout
    private lateinit var subtitle: TextView
    private val pictures = listOf(
        R.drawable.img_1, R.drawable.img_2, R.drawable.img_3, R.drawable.img_4,
        R.drawable.img_5, R.drawable.img_6, R.drawable.img_7, R.drawable.img_8
    )
    private var faceUp = 0
    private var gameVersion = 0
    private val opened = mutableSetOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(24), dp(18), dp(16))
            setBackgroundColor(Color.rgb(255, 246, 238))
        }
        val title = TextView(this).apply {
            text = "МЕМОРИНА"
            textSize = 32f
            setTextColor(Color.rgb(106, 55, 87))
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            view.setPadding(dp(18) + bars.left, dp(24) + bars.top, dp(18) + bars.right, dp(16) + bars.bottom)
            insets
        }
        root.addView(title, LinearLayout.LayoutParams(-1, dp(50)))
        subtitle = TextView(this).apply {
            text = "Найди одинаковые сладости"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(146, 105, 125))
        }
        root.addView(subtitle, LinearLayout.LayoutParams(-1, dp(38)))
        grid = GridLayout(this).apply { columnCount = 4; rowCount = 4; alignmentMode = GridLayout.ALIGN_BOUNDS }
        val gameSpace = LinearLayout(this).apply { gravity = Gravity.CENTER; addView(grid) }
        root.addView(gameSpace, LinearLayout.LayoutParams(-1, 0, 1f))
        val reset = Button(this).apply {
            text = "ПЕРЕМЕШАТЬ КАРТЫ"
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(169, 104, 151), 22f)
            setOnClickListener { newGame() }
        }
        root.addView(reset, LinearLayout.LayoutParams(-1, dp(54)))

        setContentView(root)
        gameSpace.post { newGame() }
    }

    private fun newGame() {
        gameVersion++
        val version = gameVersion
        handler.removeCallbacksAndMessages(null)
        opened.clear()
        faceUp = 0
        subtitle.text = "Открыто карт: 0 из 2"
        grid.removeAllViews()
        val deck = (pictures + pictures).shuffled()
        val widthAvailable = (grid.parent as View).width.takeIf { it > 0 }
            ?: (resources.displayMetrics.widthPixels - dp(36))
        val heightAvailable = (grid.parent as View).height.takeIf { it > 0 }
            ?: (resources.displayMetrics.heightPixels - dp(270))
        val size = min(widthAvailable / 4, heightAvailable / 4).coerceAtLeast(dp(45))
        val margin = dp(4)
        deck.forEachIndexed { index, picture ->
            val card = ImageView(this).apply {
                tag = picture
                setImageResource(R.drawable.card_back)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = rounded(Color.WHITE, 12f)
                clipToOutline = true
                contentDescription = "Закрытая карта ${index + 1}"
                setOnClickListener { tapped ->
                    val selected = tapped as ImageView
                    if (selected in opened || faceUp >= 2) return@setOnClickListener
                    val imageId = selected.tag as Int
                    selected.setImageResource(imageId)
                    selected.contentDescription = "Открытая карта ${index + 1}"
                    opened.add(selected)
                    faceUp++
                    subtitle.text = "Открыто карт: $faceUp из 2"
                    handler.postDelayed({
                        if (version != gameVersion) return@postDelayed
                        selected.setImageResource(R.drawable.card_back)
                        selected.contentDescription = "Закрытая карта ${index + 1}"
                        if (opened.remove(selected)) faceUp--
                        subtitle.text = "Открыто карт: $faceUp из 2"
                    }, 850)
                }
            }
            val params = GridLayout.LayoutParams(GridLayout.spec(index / 4), GridLayout.spec(index % 4)).apply {
                width = size - margin * 2; height = size - margin * 2
                setMargins(margin, margin, margin, margin)
            }
            grid.addView(card, params)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius.toInt()).toFloat()
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
