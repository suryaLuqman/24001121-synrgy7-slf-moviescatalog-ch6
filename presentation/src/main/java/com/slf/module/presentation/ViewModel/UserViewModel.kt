package com.slf.module.presentation.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slf.module.data.model.User

class UserViewModel : ViewModel() {

    val user : MutableLiveData<User> = MutableLiveData()
    fun dataUser(userEntity: User){
        user.postValue(userEntity)
    }
}