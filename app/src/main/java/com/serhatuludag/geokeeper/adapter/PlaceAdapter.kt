package com.serhatuludag.geokeeper.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serhatuludag.geokeeper.databinding.RecyclerRowBinding
import com.serhatuludag.geokeeper.model.Place
import com.serhatuludag.geokeeper.view.MapsActivity

class PlaceAdapter(var placeList: List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val recyclerRowBinding : RecyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = placeList[position].name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MapsActivity::class.java)
            intent.putExtra("place",placeList[position])
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }
    class PlaceHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root){

    }

}