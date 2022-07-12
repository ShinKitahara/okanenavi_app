package okanenavi.co.jp.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


data class Record(
    @Exclude @JvmField
    var id: String? = null,
    val userId: String? = null,
    val created: Long? = null,
    val date: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val place: String? = null,
    val debit: String? = null,
    val debitDetail: String? = null,
    val credit: String? = null,
    val creditDetail: String? = null,
    val price: Int? = null,
    val memo: String? = null,
    val photoLink: String? = null,
) {
    companion object {
        val collectionRef = Firebase.firestore.collection("records")
        val storageRef = Firebase.storage.reference.child("records")

        fun DocumentSnapshot.toRecord(): Record? {
            val recordId = id
            return try {
                toObject(Record::class.java)?.apply {
                    id = recordId
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @Exclude
    fun isExpenditure() = debit == "費用" || credit == "費用"

    @Exclude
    fun isIncome() = debit == "収益" || credit == "収益"
}
