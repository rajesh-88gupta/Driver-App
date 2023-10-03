package com.seentechs.newtaxidriver.home.firebaseChat

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
class FirebaseChatModelClass {
    var type: String =""
    var message: String = ""


    internal constructor(message: String, type: String) {
        this.type = type
        this.message = message
    }

    constructor() {}
}
