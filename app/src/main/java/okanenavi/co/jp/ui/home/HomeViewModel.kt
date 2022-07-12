package okanenavi.co.jp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import okanenavi.co.jp.model.Record
import okanenavi.co.jp.service.FirebaseRecordService

class HomeViewModel : ViewModel() {
    private val _records1 = MutableLiveData<List<Record>>()
    val records1: LiveData<List<Record>> = _records1
    private val _records2 = MutableLiveData<List<Record>>()
    val records2: LiveData<List<Record>> = _records2
    private val _records3 = MutableLiveData<List<Record>>()
    val records3: LiveData<List<Record>> = _records3

    init {
        viewModelScope.launch {
            Firebase.auth.currentUser?.also { currentUser ->
                FirebaseRecordService.getRecords(currentUser.uid).collect { records ->
                    val sorted = records.sortedByDescending { it.date }
                    _records1.value = sorted.filter { it.isExpenditure() }
                    _records2.value = sorted.filter { it.isIncome() }
                    _records3.value = sorted.filter { !it.isExpenditure() && !it.isIncome() }
                }
            }
        }
    }
}