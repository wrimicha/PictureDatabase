package sheridan.wrimicha.moviedatabase

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
//import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadViewModel() : ViewModel() {

//    private val _upload = MutableLiveData<Upload>()
//    val upload: LiveData<Upload> = _upload

    val mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
    val mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _url = MutableLiveData<String>()
    val url: LiveData<String> = _url

    fun updateLiveData(name: String, url: String){
        _name.value = name
        _url.value = url
    }

    fun getUploadByKey(key: String) : Upload{
        lateinit var uri : String
        lateinit var title : String
        mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("snapshot", snapshot.toString())
                //uri = snapshot.child("imageUri").toString()
                uri = snapshot.child(key).child("imageUri").getValue(Upload::class.java).toString()
                title = snapshot.child(key).child("name").toString()
            }

            override fun onCancelled(snapshotError: DatabaseError) {
            }
        })
        Log.d("uri", uri)
        Log.d("title", title)
        return Upload(title, uri)
    }
}
