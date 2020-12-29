package sheridan.wrimicha.moviedatabase

import android.widget.TextView
import com.google.firebase.database.Exclude

class Upload(name: String? = null, imageUri: String? = null) {
    private lateinit var mName: String
    private lateinit var mImageUri: String
    private lateinit var mKey : String

    init {
        name?.let {
            mName = if (it.trim() == "") "No Name" else it
        }
        imageUri?.let {
            mImageUri = it
        }
    }

    fun getName(): String? {
        return mName
    }

    fun setName(name: String) {
        mName = name
    }

    fun getImageUri(): String? {
        return mImageUri
    }

    fun setImageUri(imageUri: String) {
        mImageUri = imageUri
    }

    @Exclude  //Avoid redundancy as key is already present in the database
    fun getKey(): String? {
        return mKey
    }

    @Exclude
    fun setKey(key: String) {
        mKey = key
    }
}