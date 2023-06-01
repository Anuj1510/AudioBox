package com.example.audiobox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.audiobox.databinding.ActivityAboutBinding

class aboutActivity : AppCompatActivity() {

    lateinit var binding:ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
        binding.aboutText.text = aboutText()
    }

    private fun aboutText(): String {
        return "Developed By: Anuj Shahi" +
                "\n\nIf you want to provide feedback, I will love to hear that."
    }
}