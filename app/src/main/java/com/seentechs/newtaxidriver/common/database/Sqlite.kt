package com.seentechs.newtaxidriver.common.database
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.seentechs.newtaxidriver.home.datamodel.UserLocationModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Sqlite(context: Context) :SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION){

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_LAT + " NUMERIC," + KEY_LNG + " NUMERIC,"
                + KEY_TIME + " DATETIME DEFAULT CURRENT_TIME" + ")")

        val CREATE_LOCAL_STORAGE_TABLE = ("CREATE TABLE " + TABLE_TRIPS + "(" + KEY_DOCUMENT_ID + " TEXT PRIMARY KEY,"
                + KEY_DOCUMENT + " BLOB" + ")")


        db?.execSQL(CREATE_CONTACTS_TABLE)

        db?.execSQL(CREATE_LOCAL_STORAGE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION)
        onCreate(db)
    }

    private fun getDateTime(): String {
        val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()



        return dateFormat.format(date)
    }

    //method to insert data
    fun AddUserLocation(userLocation: UserLocationModel):Long{
        val db =this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_LAT,userLocation.lat)
        contentValues.put(KEY_LNG,userLocation.lng)
        contentValues.put(KEY_TIME,getDateTime())
        // Inserting Row
        val success = db.insert(TABLE_LOCATION, null, contentValues)
        db.close()
        return success
    }



    fun checkUser():Int{
        val db = this.readableDatabase
        try {
            val c = db.rawQuery("SELECT * FROM $TABLE_LOCATION", null)
            val count= c.count
            c.close()
            return count
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    //method to read data
    fun ViewUserLocation():List<UserLocationModel>{
        val userLocationModellist=ArrayList<UserLocationModel>()
        val selectQuery="SELECT * FROM $TABLE_LOCATION"
        val db=this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var userid: String
        var userlat: Double
        var userlng: Double
        var userTime:String
        if (cursor.moveToFirst()) {
            do {
                userid = cursor.getString(cursor.getColumnIndex("id"))
                userlat = cursor.getDouble(cursor.getColumnIndex("lat"))
                userlng = cursor.getDouble(cursor.getColumnIndex("lng"))
                userTime = cursor.getString(cursor.getColumnIndex("current_time"))
               val userLocationModel=UserLocationModel(userlat,userlng,userTime)
                userLocationModellist.add(userLocationModel)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userLocationModellist
    }


   /* //method to update data
    fun UpdateUserLocation(user: UserLocationModel):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_LAT, user.lat)
        contentValues.put(KEY_LNG,user.lng)
        contentValues.put(KEY_TIME,getDateTime())
        // Updating Row
        val success = db.update(TABLE_LOCATION, contentValues,"id="+user.id,null)
        db.close() // Closing database connection
        return success
    }
*/
    //method to delete Table
    fun DeleteUser():Int{
        val db = this.writableDatabase
        // Deleting Row
        val success = db.delete(TABLE_LOCATION,null,null)
        db.close() // Closing database connection
        return success
    }
    //method to delete data
    fun DeleteUser(userid: String):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, userid)
        // Deleting Row
        val success = db.delete(TABLE_LOCATION,"id="+userid,null)
        db.close() // Closing database connection
        return success
    }


    //method to delete data
    fun deleteUsingLatLng(lat: Double, lng: Double):Int{
        val db = this.writableDatabase

        // Deleting Row
        val success = db.delete(TABLE_LOCATION,"lat="+lat+" AND lng="+lng,null)
        db.close() // Closing database connection
        return success
    }

    fun getDocument(documentID: String): Cursor {
        val db = writableDatabase
        return db.rawQuery("SELECT " + KEY_DOCUMENT + " FROM " + TABLE_TRIPS + " WHERE " + KEY_DOCUMENT_ID + " = ?", arrayOf(documentID))
    }

    fun insertWithUpdate(documentID: String?, document: String?) {
        val db = writableDatabase
        val content = ContentValues()
        content.put(KEY_DOCUMENT_ID, documentID)
        content.put(KEY_DOCUMENT, document)
        writableDatabase.insertWithOnConflict(TABLE_TRIPS, null, content, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun dropAllTables(){
        val db = writableDatabase
        val c: Cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type IS 'table'" +
                        " AND name NOT IN ('sqlite_master', 'sqlite_sequence')",
                null
        )
        if (c.moveToFirst()) {
            do {
                db.execSQL("DROP TABLE " + c.getString(c.getColumnIndex("name")))
            } while (c.moveToNext())
        }
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "LocationDB"
        private val TABLE_LOCATION = "UserLocation"
        private val KEY_ID = "id"
        private val KEY_LAT = "lat"
        private val KEY_LNG = "lng"
        private val KEY_TIME = "current_time"

        private const val TABLE_TRIPS = "TripsInfo"
        private const val KEY_DOCUMENT_ID = "trip_doc_id"
        private const val KEY_DOCUMENT = "trip_doc"
    }
}



