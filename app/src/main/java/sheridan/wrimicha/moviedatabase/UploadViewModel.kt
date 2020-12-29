//package sheridan.wrimicha.moviedatabase
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.liveData
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class DonutEntryViewModel(private val : ) : ViewModel() {
//
////    private var donutLiveData: LiveData<DonutEntity>? = null
////
////    fun get(id: Long): LiveData<DonutEntity> {
////        return donutLiveData ?: liveData {
////            emit(donutDao.get(id))
////        }.also {
////            donutLiveData = it
////        }
////    }
//
//    fun addData(
//        id: Long,
//        name: String,
//        description: String,
//        rating: Int,
//        setupNotification: (Long) -> Unit
//    ) {
//        val donut = DonutEntity(id, name, description, rating)
//
//        CoroutineScope(Dispatchers.Main.immediate).launch {
//
//            if (id > 0) {
//                update(donut)
//            } else {
//                insert(donut)
//            }
//        }
//    }
//
//    private suspend fun insert(donut: DonutEntity): Long {
//        return donutDao.insert(donut)
//    }
//
//    private fun update(donut: DonutEntity) = viewModelScope.launch(Dispatchers.IO) {
//        donutDao.update(donut)
//    }
//}
