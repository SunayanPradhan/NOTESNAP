package com.sunayanpradhan.notesnap.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sunayanpradhan.notesnap.repository.NoteRepository

class NoteActivityViewModelFactory(private val repository: NoteRepository) :ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return NoteActivityViewModel(repository) as T
    }



}