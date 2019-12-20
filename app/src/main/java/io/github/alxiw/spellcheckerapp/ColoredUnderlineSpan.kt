package io.github.alxiw.spellcheckerapp

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.UpdateAppearance
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast


internal class ColoredUnderlineSpan(
    private val mColor: Int,
    private val mSuggestions: List<String>,
    private val offset: Int,
    private val length: Int,
    private var word: String,
    private val listener: WordReplacerListener
) : ClickableSpan(), UpdateAppearance {
    override fun onClick(widget: View) {
        if (mSuggestions.isNotEmpty()) {
            val contextThemeWrapper = ContextThemeWrapper(
                widget.context,
                R.style.PopupMenuOverlapAnchor
            )
            val popup = PopupMenu(contextThemeWrapper, widget)

            for (i in mSuggestions.indices) {
                popup.menu.add(mSuggestions[i])
            }
            popup.setOnMenuItemClickListener { item ->
                val text = item.title.toString()
                listener.replaceWord(text, offset, length)
                Toast.makeText(
                    widget.context,
                    String.format(
                        widget.context.getString(R.string.toast_replaced_pattern),
                        word,
                        text
                    ),
                    Toast.LENGTH_SHORT
                ).show()
                word = text
                true
            }
            popup.show()
        }
    }

    override fun updateDrawState(tp: TextPaint) {
        try {
            val method = TextPaint::class.java.getMethod(
                "setUnderlineText",
                Integer.TYPE,
                java.lang.Float.TYPE
            )
            method.invoke(tp, mColor, 2.0f)
        } catch (e: Exception) {
            tp.isUnderlineText = true
        }
    }
}
