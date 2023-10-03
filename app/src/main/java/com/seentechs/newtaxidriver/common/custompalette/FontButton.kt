package com.seentechs.newtaxidriver.common.custompalette

/**
 * @package com.seentechs.newtaxidriver.common.custompalette
 * @subpackage custompalette
 * @category FontButton
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

/* ************************************************************
             Its used for FontButton
*************************************************************** */

class FontButton : Button {



    /*constructor(context: Context) : super(context) {

        CustomFontUtils.applyCustomFont(this, context)
    }*/

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        CustomFontUtils.applyCustomFont(this, context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

        CustomFontUtils.applyCustomFont(this, context, attrs)
    }
}
