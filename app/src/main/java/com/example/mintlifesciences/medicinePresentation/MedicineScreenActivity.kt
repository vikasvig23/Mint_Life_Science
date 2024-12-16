package com.example.mintlifesciences.medicinePresentation

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityMedicineListBinding
import com.example.mintlifesciences.databinding.ActivityMedicineScreenBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class MedicineScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineScreenBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PresentationAdapter
    private val items = listOf(
        PresentationData(
            "Item 1",
            R.drawable.baseline_image_24,
            "https://www.w3schools.com/html/movie.mp4"
        ),
        PresentationData(
            "Item 2",
            R.drawable.baseline_person_4_24,
            "https://www.example.com/audio1.mp3"
        ),
        PresentationData(
            "Item 3",
            R.drawable.baseline_image_24,
            "https://www.example.com/audio1.mp3"
        ),
        PresentationData(
            "Item 4",
            R.drawable.baseline_person_4_24,
            "https://www.w3schools.com/html/movie.mp4"
        ),
        PresentationData(
            "Item 5",
            R.drawable.baseline_image_24,
            "https://www.example.com/audio1.mp3"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        adapter = PresentationAdapter(items, binding.viewPager, this)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL


    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.presn_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_update_medicine -> {
               TODO()
                return true
            }

            R.id.action_edit_feedback -> {
                TODO()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}



