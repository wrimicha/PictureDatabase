package sheridan.wrimicha.moviedatabase

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import sheridan.wrimicha.moviedatabase.databinding.UploadFragmentBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var binding: UploadFragmentBinding
    private var mImageUri: Uri? = null
    private lateinit var mImageView: ImageView
    private val picasso = Picasso.get()
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mEditTextFileName: EditText
    private lateinit var mTextViewShowUploads: TextView
    private lateinit var viewModel: UploadViewModel
    private lateinit var mDBListener : ValueEventListener


    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference

    private var mUploadTask : StorageTask<UploadTask.TaskSnapshot>? = null

    companion object{
        private const val PICK_IMAGE_REQUEST = 1
    }

    private enum class EditingState {
        NEW_UPLOAD,
        EDIT_UPLOAD
    }

    @SuppressLint("SetTextI18n") //Used for supporting multiple languages
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {

        binding = UploadFragmentBinding.inflate(inflater, container, false)

        val mButtonChooseImage = binding.buttonChooseImage
        val mButtonUpload = binding.buttonUpload
        mTextViewShowUploads = binding.textViewShowUploads
        mEditTextFileName = binding.editTextFileName
        mProgressBar = binding.progressBar

        mImageView = binding.imageView

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")

        val args: FirstFragmentArgs by navArgs()
        val editingState =
                if (args.key != "0") EditingState.EDIT_UPLOAD
            else EditingState.NEW_UPLOAD

        if (editingState == EditingState.EDIT_UPLOAD) {
            mDBListener = mDatabaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("snapshot", snapshot.toString())
                    val uri = snapshot.child(args.key).child("imageUri").getValue().toString()
                    val title = snapshot.child(args.key).child("name").getValue().toString()
                    picasso.load(uri.toUri()).into(mImageView)
                    binding.editTextFileName.setText(title)
                }
                override fun onCancelled(snapshotError: DatabaseError) {
                }
            })
            binding.buttonUpload.text = "UPDATE"
        }

        mButtonChooseImage.setOnClickListener { openFileChooser() }

        mButtonUpload.setOnClickListener {
            if (mUploadTask != null && mUploadTask!!.isInProgress){
                Toast.makeText(context, "Upload In Progress", Toast.LENGTH_SHORT).show()
            } else {
                uploadImage()
            }
        }

        mTextViewShowUploads.setOnClickListener{
            findNavController().navigate(FirstFragmentDirections.actionUploadFragmentToImagesFragment())
        }
        return binding.root
    }

    /* Method which gets file extension for the image */
    private fun getFileExtension(uri: Uri): String? {
        val cR = context?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR?.getType(uri))
    }

    private fun uploadImage() {
//        entryViewModel.addData(
//            donut?.id ?: 0,
//            binding.editTextFileName.text.toString(),
//            picasso.
//            binding.description.text.toString(),
//            binding.ratingBar.rating.toInt()
//        )

        if (mImageUri != null) {
            val fileReference = mStorageRef.child(
                System.currentTimeMillis().toString() + "." + getFileExtension(mImageUri!!)  //create file name for image
            )
            mUploadTask = fileReference.putFile(mImageUri!!)  //add image with file name under "uploads"
                .addOnSuccessListener { it ->
                    it.storage.downloadUrl.addOnSuccessListener { //file successfully uploaded to storage
                    Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mProgressBar.progress = 0
                    }, 1000) //reset the progress bar after one second

                    val upload = Upload(  //create an Upload object
                        mEditTextFileName.text.toString().trim(), //title
                        it.toString() //image url
                    )
                    val uploadId = mDatabaseRef.push().key //create a key for the Upload object
                    mDatabaseRef.child(uploadId!!).setValue(upload) //set the child of database reference to the value of the Upload object
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                    print(e.message)
                }
                .addOnProgressListener { e ->
                    val progress: Double = (100.0 * e.bytesTransferred / e.totalByteCount)
                    binding.progressBar.progress = progress.toInt()
                }

        } else {
            Toast.makeText(context,"No File Selected",Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null
        ) {
            mImageUri = data.data!!
            picasso.load(mImageUri).into(mImageView)
        }
    }
}