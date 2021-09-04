package com.godwpfh.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view=LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container, false);

        firestore= FirebaseFirestore.getInstance()

        view.detail_fragment_recycler_view.adapter=DetailViewRecyclerViewAdapter()
        view.detail_fragment_recycler_view.layoutManager=LinearLayoutManager(activity) //보여지는 걸 linearlayout으로 보여지기 위해해
       return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                for(snapshot in querySnapshot!!.documents){ //들어오는 값 찍기
                    var item=snapshot.toObject(ContentDTO::class.java) //ContentDTO방식으로 casting
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)

                }
                notifyDataSetChanged() //새로고침
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view=LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //mapping 작업
            viewholder.detail_profile_name.text=contentDTOs!![position].userId
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.detail_image_content)
            viewholder.detail_content_explain.text=contentDTOs!![position].explain
            viewholder.detail_heart_count.text= "Likes "+contentDTOs!![position].favoriteCount.toString()


        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}