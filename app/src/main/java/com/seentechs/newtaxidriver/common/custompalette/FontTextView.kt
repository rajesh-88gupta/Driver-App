package com.seentechs.newtaxidriver.common.custompalette

/**
 * @package com.seentechs.newtaxidriver.common.custompalette
 * @subpackage custompalette
 * @category FontTextView
 * @author Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/* ************************************************************
                   Its used for FontTextView
*************************************************************** */
@SuppressLint("AppCompatCustomView")
class FontTextView : TextView {


   /* constructor(context: Context) : super(context) {

        CustomFontUtils.applyCustomFont(this, context, null!!)
    }
*/
    /*constructor(context: Context) : super(context) {

        CustomFontUtils.applyCustomFont(this, context, null)
    }*/

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        CustomFontUtils.applyCustomFont(this, context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

        CustomFontUtils.applyCustomFont(this, context, attrs)
    }
}
