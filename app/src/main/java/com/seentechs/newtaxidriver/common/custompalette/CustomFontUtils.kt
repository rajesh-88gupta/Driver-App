package com.seentechs.newtaxidriver.common.custompalette

/**
 * @package com.seentechs.newtaxidriver.common.custompalette
 * @subpackage custompalette
 * @category CustomFontUtils
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.seentechs.newtaxidriver.R


object CustomFontUtils {

    val ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android"

    fun applyCustomFont(customFontTextView: TextView, context: Context, attrs: AttributeSet) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView)

        val fontName = attributeArray.getString(R.styleable.FontTextView_fontname)


        // check if a special textStyle was used (e.g. extra bold)
        var textStyle = attributeArray.getInt(R.styleable.FontTextView_textStyle, 0)

        // if nothing extra was used, fall back to regular android:textStyle parameter
        if (textStyle == 0) {
            textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL)
        }

        val customFont = selectTypeface(context, fontName)
        customFontTextView.typeface = customFont

        attributeArray.recycle()
    }

    fun applyCustomFont(customFontTextView: EditText, context: Context, attrs: AttributeSet) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView)

        val fontName = attributeArray.getString(R.styleable.FontTextView_fontname)


        // check if a special textStyle was used (e.g. extra bold)
        var textStyle = attributeArray.getInt(R.styleable.FontTextView_textStyle, 0)

        // if nothing extra was used, fall back to regular android:textStyle parameter
        if (textStyle == 0) {
            textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL)
        }

        val customFont = selectTypeface(context, fontName)
        customFontTextView.typeface = customFont

        attributeArray.recycle()
    }

    fun applyCustomFont(customFontTextView: Button, context: Context, attrs: AttributeSet) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView)

        val fontName = attributeArray.getString(R.styleable.FontTextView_fontname)


        // check if a special textStyle was used (e.g. extra bold)
        var textStyle = attributeArray.getInt(R.styleable.FontTextView_textStyle, 0)

        // if nothing extra was used, fall back to regular android:textStyle parameter
        if (textStyle == 0) {
            textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL)
        }

        val customFont = selectTypeface(context, fontName)
        customFontTextView.typeface = customFont

        attributeArray.recycle()
    }

    private fun selectTypeface(context: Context, fontName: String?): Typeface? {
        if (TextUtils.isEmpty(fontName)) {
            return FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
        }
        return if (fontName!!.contentEquals(context.resources.getString(R.string.font_PermanentMarker))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_PermanentMarker), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_Book))) {

            FontCache.getTypeface(context.resources.getString(R.string.fonts_Book), context)

        } else if (fontName.contentEquals(context.resources.getString(R.string.font_Medium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_Medium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_NarrBook))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_NarrBook), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_NarrMedium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_NarrMedium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_NarrNews))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_NarrNews), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_News))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_News), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_WideMedium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_WideMedium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_WideNews))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_WideNews), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UBERBook))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERBook), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UBERMedium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UBERNews))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERNews), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UberClone))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UberClone), context)
        } else {
            // no matching font found
            // return null so Android just uses the standard font (Roboto)
            //return null;
            // temporaryly returning this uber medium font
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
        }
    }
}