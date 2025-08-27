package com.example.aibudgetapp.ui.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _userName = MutableLiveData("Spencer")
    val userName: LiveData<String> = _userName

}
