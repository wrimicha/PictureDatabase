package sheridan.wrimicha.moviedatabase

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ImageAdapter(private val context: Context, uploads : List<Upload>): RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var mContext : Context? = null
    private var mUploads : List<Upload>? = null
    private lateinit var mListener : OnItemClickListener
    private val picasso = Picasso.get()

    init{
        mContext = context
        mUploads = uploads
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener  {
        val textViewName : TextView = view.findViewById(R.id.text_view_name)
        val imageView : ImageView = view.findViewById(R.id.image_view_upload)
        init{
            view.setOnClickListener(this)
            view.setOnCreateContextMenuListener(this)

        }

        override fun onClick(v: View?) {
                val position = adapterPosition //get the position of this image list element
                if (position != RecyclerView.NO_POSITION) { //click position must  still be valid
                    mListener.onItemClick(position)
                }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.setHeaderTitle("Select Action")
            val edit = menu.add(Menu.NONE, 1, 1, "Edit")
            val delete = menu.add(Menu.NONE, 2, 2, "Delete")

            edit.setOnMenuItemClickListener(this)
            delete.setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
                val position = adapterPosition //get the position of this image list element
                if (position != RecyclerView.NO_POSITION) { //click position must  still be valid
                    when (item!!.itemId) {
                        1 -> {
                            mListener.onEditClick(position)
                            return true
                        }
                        2 -> {
                            mListener.onDeleteClick(position)
                            return true
                        }
                        }
                    }
            return false
        }
    }


    override fun onBindViewHolder(holder: ImageAdapter.ViewHolder, position: Int) {
        val uploadCurrent : Upload = mUploads!![position]
        holder.textViewName.text = uploadCurrent.getName()
        picasso.load(uploadCurrent.getImageUri())
            .fit()
            .centerCrop()
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mUploads!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.images_item, parent, false)
        return ViewHolder(view)
    }

    public interface OnItemClickListener{
        fun onItemClick(position: Int)

        fun onEditClick(position: Int)

        fun onDeleteClick(position: Int)
    }

    public fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

}