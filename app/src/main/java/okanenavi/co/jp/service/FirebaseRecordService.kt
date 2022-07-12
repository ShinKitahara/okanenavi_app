package okanenavi.co.jp.service

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okanenavi.co.jp.model.Record
import okanenavi.co.jp.model.Record.Companion.toRecord

object FirebaseRecordService {
    fun getRecords(userId: String): Flow<List<Record>> = callbackFlow {
        val listenerRegistration = Record.collectionRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot == null) return@addSnapshotListener

                val map = querySnapshot.documents.mapNotNull { it.toRecord() }
                trySend(map).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}