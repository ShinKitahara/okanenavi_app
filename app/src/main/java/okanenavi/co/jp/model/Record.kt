package okanenavi.co.jp.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


data class Record(
    val userId: String,
    val created: Long,
    val date: Long,
    val hour: Int,
    val minute: Int,
    val place: String,
    val debit: String,
    val debitDetail: String,
    val credit: String,
    val creditDetail: String,
    val price: Int,
    val memo: String,
    val photoLink: String,
) {
    companion object {
        val collectionRef = Firebase.firestore.collection("records")
        val storageRef = Firebase.storage.reference.child("records")

        fun DocumentSnapshot.toRecord(): Record? {
            val place = getString("date")
            return null
        }
    }
}
