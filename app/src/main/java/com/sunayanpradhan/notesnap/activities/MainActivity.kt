package com.sunayanpradhan.notesnap.activities

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sunayanpradhan.notesnap.R
import com.sunayanpradhan.notesnap.databinding.ActivityMainBinding
import com.sunayanpradhan.notesnap.db.NoteDatabase
import com.sunayanpradhan.notesnap.fragments.noteFragment
import com.sunayanpradhan.notesnap.repository.NoteRepository
import com.sunayanpradhan.notesnap.viewModel.NoteActivityViewModel
import com.sunayanpradhan.notesnap.viewModel.NoteActivityViewModelFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding= ActivityMainBinding.inflate(layoutInflater)


        try{
            setContentView(binding.root)
            val noteRepository=NoteRepository(NoteDatabase(this))
            val noteActivityViewModelFactory=NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel=ViewModelProvider(this,noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
        } catch (e:Exception){
            Log.d("TAG","Error")
        }
    }


}