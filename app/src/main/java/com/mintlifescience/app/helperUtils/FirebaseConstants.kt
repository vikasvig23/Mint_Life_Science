package com.mintlifescience.app.helperUtils

object FirebaseConstants {
    const val USERS = "Users"
    const val SIGNUP_REQUESTS = "signupRequests"
    const val CLIENT = "Mint_Life_Science_Client"
    const val DOCTORS = "Doctors"
    const val MEDICINES = "medicines"
    const val FEEDBACK = "feedback"
    const val SCHEDULE_MEET = "scheduleMeet"
    const val HAVE_PRESENTATION = "havePresentation"
    const val ADMIN = "Mint_Life_Science_Admin"

    fun userPath(userId: String) = "$USERS/$userId/$CLIENT"
    fun doctorsPath(userId: String) = "${userPath(userId)}/$DOCTORS"
    fun doctorPath(userId: String, doctorName: String) = "${doctorsPath(userId)}/$doctorName"
}
