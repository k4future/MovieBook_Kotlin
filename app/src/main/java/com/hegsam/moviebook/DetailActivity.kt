package com.hegsam.moviebook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.hegsam.moviebook.databinding.ActivityDetailBinding
import java.io.ByteArrayOutputStream

class DetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var selectedBitmap : Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerLaunchers()

        val info = intent.getStringExtra("info")
        if (info.equals("old"))
        {
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",-1)
            val database = this.openOrCreateDatabase("Movies", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM movies WHERE id = ?", arrayOf(selectedId.toString()))
            val nameIx = cursor.getColumnIndex("moviename")
            val directorIx = cursor.getColumnIndex("directorname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")
            while (cursor.moveToNext())
            {
                val name = cursor.getString(nameIx)
                val directorName = cursor.getString(directorIx)
                val year = cursor.getString(yearIx)
                val image = cursor.getBlob(imageIx)

                binding.movieNameEditText.setText(name)
                binding.directorNameEditText.setText(directorName)
                binding.movieYearEditText.setText(year)

                val imageBitmap = BitmapFactory.decodeByteArray(image,0,image.size)

                binding.selectedImageView.setImageBitmap(imageBitmap)

            }

            cursor.close()
        }

    }

    fun selectImage (view : View)
    {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Snackbar.make(view,getString(R.string.permission_needed_text),Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.give_permission_text)){
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
            }
            else
            {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        else
        {
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }

    }

    fun saveImage (view : View)
    {
        val directorName = binding.directorNameEditText.text.toString().trim()
        val movieName = binding.movieNameEditText.text.toString().trim()
        val movieYear = binding.movieYearEditText.text.toString().trim()

        if (selectedBitmap != null)
        {
            val resizedBitmap = resizeImage(selectedBitmap,300)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                val database = this.openOrCreateDatabase("Movies", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS movies(id INTEGER PRIMARY KEY,moviename VARCHAR,directorname VARCHAR,year VARCHAR,image BLOB)")
                val sqlString = "INSERT INTO movies(moviename,directorname,year,image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,movieName)
                statement.bindString(2,directorName)
                statement.bindString(3,movieYear)
                statement.bindBlob(4,byteArray)

                statement.execute()
            } catch (e:Exception)
            {
                e.printStackTrace()
            }

            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }

    }

    private fun registerLaunchers ()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if (result.resultCode == RESULT_OK)
            {
                val resultData = result.data
                if (resultData != null)
                {
                    val imageUri = resultData.data

                    if (imageUri != null)
                    {
                        try {
                            if (Build.VERSION.SDK_INT >= 28)
                            {
                                val source = ImageDecoder.createSource(contentResolver,imageUri)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.selectedImageView.setImageBitmap(selectedBitmap)
                            }
                            else
                            {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                                binding.selectedImageView.setImageBitmap(selectedBitmap)
                            }

                        } catch (e:Exception)
                        {
                            e.printStackTrace()
                        }
                    }

                }

            }

        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result)
            {
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            }
            else
            {
                Toast.makeText(this,getString(R.string.permission_needed_text),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resizeImage (image : Bitmap,maximumSize : Int) : Bitmap
    {
        var width = image.width
        var height = image.height

        val bitmapRatio : Float = width.toFloat() / height.toFloat()

        if (bitmapRatio > 1)
        {
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }
        else
        {
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }
}