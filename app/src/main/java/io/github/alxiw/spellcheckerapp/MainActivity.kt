package io.github.alxiw.spellcheckerapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.textservice.*
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.alxiw.spellcheckerapp.Utils.suggestionsToText
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), SpellCheckerSessionListener, WordReplacerListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val SUGGESTIONS_LIMIT = 3
    }

    private lateinit var enter: Button
    private lateinit var status: TextView
    private lateinit var details: TextView
    private lateinit var input: EditText
    private lateinit var bubble: TextView

    private var session: SpellCheckerSession? = null

    private var cache: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.text_edit_input)
        enter = findViewById(R.id.button_enter)
        bubble = findViewById(R.id.text_view_bubble)
        status = findViewById(R.id.text_view_status)
        details = findViewById(R.id.text_view_details)

        initSampleButtons()

        enter.setOnClickListener {
            request(input.text.toString())
        }

        bubble.movementMethod = LinkMovementMethod.getInstance()

        savedInstanceState?.let {
            bubble.text = savedInstanceState.getString(getString(R.string.code_bubble))
            status.text = savedInstanceState.getString(getString(R.string.code_status))
            details.text = savedInstanceState.getString(getString(R.string.code_details))
        } ?: run {
            bubble.visibility = View.GONE
            status.visibility = View.GONE
            details.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        session = startNewSpellCheckerSession()
        Log.d(TAG, session?.let { "Started new spell checker session." } ?: "Started new spell checker session.")
    }

    override fun onPause() {
        super.onPause()
        session?.apply {
            close()
            Log.d(TAG, "Spell checker session closed.")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(getString(R.string.code_bubble), bubble.text.toString())
        outState.putString(getString(R.string.code_status), status.text.toString())
        outState.putString(getString(R.string.code_details), details.text.toString())
        super.onSaveInstanceState(outState)
    }

    /**
     * Callback for [SpellCheckerSession.getSentenceSuggestions]
     * @param results an array of [SentenceSuggestionsInfo]s.
     * These results are details for [TextInfo]s
     * queried by [SpellCheckerSession.getSentenceSuggestions].
     */
    override fun onGetSentenceSuggestions(results: Array<SentenceSuggestionsInfo>) {
        Log.d(TAG, "Callback on get sentence details was invoked.")

        val resultMap: MutableMap<Int, Word?> = HashMap()
        val senInfo = results[0]
        val numberOfWords = senInfo.suggestionsCount
        for (i in 0 until numberOfWords) {
            val info = senInfo.getSuggestionsInfoAt(i)
            var list: ArrayList<String>? = null
            if (info.suggestionsCount != -1) {
                list = ArrayList()
                val numberOfSuggestions = info.suggestionsCount
                for (j in 0 until numberOfSuggestions) {
                    list.add(info.getSuggestionAt(j))
                }
            }
            resultMap[i] = Word(senInfo.getOffsetAt(i), senInfo.getLengthAt(i), list)
        }
        Log.d(TAG, "Sentence map size is: " + resultMap.size)
        runOnUiThread {
            bubble.text = cache
            if (resultMap.isEmpty()) {
                Log.d(TAG, "Sentence map is empty")
                details.text = "OK"
            } else {
                val builder = StringBuilder()
                var incorrect = 0
                for (i in 0 until numberOfWords) {
                    if (resultMap.containsKey(i)) {
                        val word = resultMap[i] ?: continue
                        builder.append(i + 1)
                            .append(". ")
                            .append(cache!!.substring(word.offset, word.offset + word.length))
                            .append(" – ")
                        if (word.suggestions != null) {
                            val line = suggestionsToText(word.suggestions!!)
                            builder.append(line)
                            incorrect++
                            highlightStringAndSuggest(bubble.text.toString().substring(word.offset, word.offset + word.length), word.suggestions, word.offset, word.length)
                        } else {
                            builder.append("OK")
                        }
                        builder.append("\n")
                    }
                }
                status.text = String.format(Locale.ENGLISH, "Misspellings: %d words from %d", incorrect, resultMap.size)
                val text = builder.toString()
                Log.d(TAG, "All details: $text")
                details.text = text

                bubble.visibility = View.VISIBLE
                status.visibility = View.VISIBLE
                details.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Callback for [SpellCheckerSession.getSuggestions]
     * and [SpellCheckerSession.getSuggestions]
     * @param results an array of [SuggestionsInfo]s.
     * These results are details for [TextInfo]s queried by
     * [SpellCheckerSession.getSuggestions] or
     * [SpellCheckerSession.getSuggestions]
     */
    override fun onGetSuggestions(results: Array<SuggestionsInfo>?) {
        Log.d(TAG, "Deprecated callback onGetSuggestions was invoked.")
        // deprecated since API 16
    }

    private fun highlightStringAndSuggest(input: String, list: java.util.ArrayList<String>?, offset: Int, length: Int) { //Get the text from text view and create a spannable string
        val spannableString = SpannableString(bubble.text)
        /*
        //Get the previous spans and remove them
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span: backgroundSpans) {
            spannableString.removeSpan(span);
        }*/
        //Search for all occurrences of the keyword in the string
        var indexOfKeyword = spannableString.toString().indexOf(input)
        while (indexOfKeyword >= 0) { //Create a background color span on the keyword
            val currentWord = cache!!.substring(offset, offset + length)
            spannableString.setSpan(ColoredUnderlineSpan(Color.RED, list!!, offset, length, currentWord, this), indexOfKeyword, indexOfKeyword + input.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            //Get the next index of the keyword
            indexOfKeyword = spannableString.toString().indexOf(input, indexOfKeyword + input.length)
        }
        //Set the final text on TextView
        bubble.text = spannableString
    }

    override fun replaceWord(text: String?, offset: Int, length: Int) {
        val newText = bubble.text.toString().substring(0, offset) + text + bubble.text.toString().substring(offset + length, bubble.text.length)
        bubble.text = newText
        request(newText)
    }

    private fun request(text: String) {
        session?.let {
            Log.d(TAG, "Sentence spellchecking supported.")
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(applicationContext, "Input is empty", Toast.LENGTH_SHORT).show()
                return
            }
            Log.d(TAG, "Requested text: $text")
            cache = text
            it.getSentenceSuggestions(arrayOf(TextInfo(text)), SUGGESTIONS_LIMIT)
        } ?: run {
            Toast.makeText(applicationContext, "Spell checker service is unavailable", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Couldn't obtain the spell checker service.")
        }
    }

    private fun startNewSpellCheckerSession(): SpellCheckerSession? {
        val tsm = getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE) as TextServicesManager
        return tsm.newSpellCheckerSession(null, null, this, true)
    }

    private fun initSampleButtons() {
        findViewById<View>(R.id.button_sample_de)?.setOnClickListener { input.setText(getString(R.string.text_sample_de)) }
        findViewById<View>(R.id.button_sample_en)?.setOnClickListener { input.setText(getString(R.string.text_sample_en)) }
        findViewById<View>(R.id.button_sample_ru)?.setOnClickListener { input.setText(getString(R.string.text_sample_ru)) }
    }

}
