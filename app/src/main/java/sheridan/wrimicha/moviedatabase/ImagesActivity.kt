package sheridan.wrimicha.moviedatabase

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import sheridan.wrimicha.moviedatabase.databinding.ImagesViewBinding
import sheridan.wrimicha.moviedatabase.domain.UploadDataId


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ImagesActivity : Fragment(), ImageAdapter.OnItemClickListener {

    private lateinit var binding: ImagesViewBinding
    private lateinit var mAdapter: ImageAdapter
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mDatabaseRef : DatabaseReference
    private lateinit var mUploads: MutableList<Upload>
    private lateinit var mRecyclerView : RecyclerView
    private lateinit var mProgressCircle : ProgressBar
    private lateinit var mDBListener : ValueEventListener

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = ImagesViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRecyclerView = binding.recyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        mProgressCircle = binding.progressCircle

        mUploads = ArrayList()

        mAdapter = ImageAdapter(requireContext(), mUploads)
        mRecyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener(this@ImagesActivity)

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")
        mStorage = FirebaseStorage.getInstance()

        mDBListener = mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUploads.clear()
                for (postSnapshot : DataSnapshot in snapshot.children){
                    val upload = postSnapshot.getValue(Upload::class.java)!!
                    upload.setKey(postSnapshot.key!!)
                    mUploads.add(upload)
                }

                mAdapter.notifyDataSetChanged()

                mProgressCircle.visibility = View.GONE
            }

            override fun onCancelled(snapshotError: DatabaseError) {
                Toast.makeText(context, snapshotError.message, Toast.LENGTH_SHORT).show()
                mProgressCircle.visibility = View.GONE
            }
        })
    }


    override fun onItemClick(position: Int) {
        Toast.makeText(context, "Normal click at position $position", Toast.LENGTH_SHORT).show()
    }

    override fun onEditClick(position: Int) {
        val selectedItem = mUploads[position]
        val selectedKey = selectedItem.getKey()

//        val imgRef : String = selectedItem.getImageUri().toString()
//        Log.d("IMAGE REF", imgRef)
//
//        val titleRef = selectedItem.getName().toString()
//        Log.d("TITLE REF", titleRef)

        //put these values into a viewModel ?
        //then when you load up your edit file you can grab these values and display them on the screen

        findNavController().navigate(ImagesActivityDirections.actionImagesFragmentToFirstFragment(selectedKey!!))
    }

    override fun onDeleteClick(position: Int) {
        val selectedItem = mUploads[position]
        val selectedKey = selectedItem.getKey()

        val imgRef = mStorage.getReferenceFromUrl(selectedItem.getImageUri()!!) //get reference to item in storage
        imgRef.delete().addOnSuccessListener{ //only delete item from database if the deletion from storage was successful, in case the database gets deleted but the image in storage doesn't
            mDatabaseRef.child(selectedKey!!).removeValue()
            Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener{
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mDatabaseRef.isInitialized) {    //If I remove this it causes an error.. Why?
            mDatabaseRef.removeEventListener(mDBListener)
        }
    }
}