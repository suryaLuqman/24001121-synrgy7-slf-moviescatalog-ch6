package com.slf.moviescatalog.presentation.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slf.moviescatalog.data.model.User

class UserViewModel : ViewModel() {

    val user : MutableLiveData<User> = MutableLiveData()
    fun dataUser(userEntity: User){
        user.postValue(userEntity)
    }
}