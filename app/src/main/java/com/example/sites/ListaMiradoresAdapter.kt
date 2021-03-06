package com.example.sites

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.sites.Miradores
import com.example.sites.R

import com.squareup.picasso.Picasso
import java.io.File

class ListaMiradoresAdapter(private val context: Context, private val dataSource: Miradores) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //1
    override fun getCount(): Int {
        return dataSource.getCount()
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource.arrayNombres[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.list_miradores, parent, false)

        // Get title element
        val titleTextView = rowView.findViewById(R.id.recipe_list_title) as TextView

// Get subtitle element
        val subtitleTextView = rowView.findViewById(R.id.recipe_list_subtitle) as TextView

// Get detail element
        val detailTextView = rowView.findViewById(R.id.recipe_list_detail) as TextView

// Get thumbnail element
        val thumbnailImageView = rowView.findViewById(R.id.recipe_list_thumbnail) as ImageView

        // 1
        val mirador = getItem(position) as String

        // 2
        titleTextView.text = mirador
        subtitleTextView.text = ""
        detailTextView.text = dataSource.zona[position]

        if(dataSource.zona[position] == "Albaicín"){
            rowView.setBackgroundColor(Color.parseColor("#bbdefb"))
        }else if(dataSource.zona[position] == "Alhambra"){
            rowView.setBackgroundColor(Color.parseColor("#fff9c4"))
        }else if(dataSource.zona[position] == "Cartuja"){
            rowView.setBackgroundColor(Color.parseColor("#e1bee7"))
        }else if(dataSource.zona[position] == "Realejo"){
            rowView.setBackgroundColor(Color.parseColor("#c8e6c9"))
        }else if(dataSource.zona[position] == "Centro"){
            rowView.setBackgroundColor(Color.parseColor("#cfd8dc"))
        }

        // 3    image
        Picasso.get().load(dataSource.image[position] ).placeholder(R.mipmap.ic_launcher).into(thumbnailImageView) //"@drawable/miradorSanNicolas"
        //thumbnailImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.miradorsannicolas));

        return rowView
    }

}