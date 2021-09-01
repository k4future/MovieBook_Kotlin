package com.hegsam.moviebook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hegsam.moviebook.databinding.RecyclerviewRowBinding

/**
 * Created by Salih ÇABUK on 31.08.2021.
 */
class RVAdapter(private val movieList : ArrayList<MovieData>) : RecyclerView.Adapter<RVAdapter.MovieHolder>() {

    class MovieHolder(val binding : RecyclerviewRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val binding = RecyclerviewRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MovieHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.binding.textView.text = movieList[position].movie_name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,DetailActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",movieList[position].movie_id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return movieList.size
    }
}