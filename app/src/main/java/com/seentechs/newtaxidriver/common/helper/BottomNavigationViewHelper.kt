package com.seentechs.newtaxidriver.common.helper

/**
 * @package com.seentechs.newtaxidriver.common.helper
 * @subpackage helper
 * @category BottomNavigationViewHelper
 * @author Seen Technologies
 *
 */

import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.seentechs.newtaxidriver.common.util.CommonMethods

/* ************************************************************
                      BottomNavigationViewHelper
Its used Home page BottomNavigation function
*************************************************************** */
object BottomNavigationViewHelper {

    fun removeShiftMode(view: BottomNavigationView) {
        val menuView = view.getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                //item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            CommonMethods.DebuggableLogE("ERROR NO SUCH FIELD", "Unable to get shift mode field")
        } catch (e: IllegalAccessException) {
            CommonMethods.DebuggableLogE("ERROR ILLEGAL ALG", "Unable to change value of shift mode")
        }

    }
}
