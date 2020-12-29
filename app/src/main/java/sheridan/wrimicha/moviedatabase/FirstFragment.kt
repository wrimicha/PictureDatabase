package sheridan.wrimicha.moviedatabase

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import sheridan.wrimicha.moviedatabase.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private var mImageUri: Uri? = null
    private lateinit var mImageView: ImageView
    private val picasso = Picasso.get()
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mEditTextFileName: EditText
    private lateinit var mTextViewShowUploads: TextView

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

    @SuppressLint("SetTextI18n") //Used fro supporting multiple languages
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {

        binding = FragmentFirstBinding.inflate(inflater, container, false)

        val mButtonChooseImage = binding.buttonChooseImage
        val mButtonUpload = binding.buttonUpload
        mTextViewShowUploads = binding.textViewShowUploads
        mEditTextFileName = binding.editTextFileName
        mProgressBar = binding.progressBar

        mImageView = binding.imageView

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")

//        val args: FirstFragmentArgs by navArgs()
//        val editingState =
//            if (args.uploadId > 0) EditingState.EDIT_UPLOAD
//            else EditingState.NEW_UPLOAD

//        if (editingState == EditingState.EDIT_UPLOAD) {
//            // Request to edit an existing item, whose id was passed in as an argument.
//            // Retrieve that item and populate the UI with its details
//            entryViewModel.get(args.itemId).observe(viewLifecycleOwner) { donutItem ->
//                binding.name.setText(donutItem.name)
//                binding.description.setText(donutItem.description)
//                binding.ratingBar.rating = donutItem.rating.toFloat()
//                donut = donutItem
//            }
//        }

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
        if (mImageUri != null) {
            val fileReference = mStorageRef.child(
                System.currentTimeMillis().toString() + "." + getFileExtension(mImageUri!!)
            )
            mUploadTask = fileReference.putFile(mImageUri!!)
                .addOnSuccessListener { it ->
                    it.storage.downloadUrl.addOnSuccessListener {
                    Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mProgressBar.progress = 0
                    }, 1000)
                    val upload = Upload(
                        mEditTextFileName.text.toString().trim(),
                        it.toString()
                    )
                    val uploadId = mDatabaseRef.push().key
                    mDatabaseRef.child(uploadId!!).setValue(upload)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                    print(e.message)
                }
                .addOnProgressListener { e ->
                    var progress: Double = (100.0 * e.bytesTransferred / e.totalByteCount)
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