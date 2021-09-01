package com.hegsam.moviebook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.hegsam.moviebook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieList = ArrayList<MovieData>()
        binding.recyclerview.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        val adapter = RVAdapter(movieList)
        binding.recyclerview.adapter = adapter

        try {
            val database = this.openOrCreateDatabase("Movies", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM movies",null)
            val movieNameIx = cursor.getColumnIndex("moviename")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext())
            {
                val name = cursor.getString(movieNameIx)
                val id = cursor.getInt(idIx)
                val movie = MovieData(name,id)
                movieList.add(movie)
            }

            cursor.close()
            adapter.notifyDataSetChanged()


        }catch (e:Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_image_menuitem)
        {
            val intent = Intent(this,DetailActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}