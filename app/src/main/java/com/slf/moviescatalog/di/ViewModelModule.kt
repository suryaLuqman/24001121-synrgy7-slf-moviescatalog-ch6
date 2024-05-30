package com.slf.moviescatalog.di

import androidx.lifecycle.ViewModel
import com.slf.moviescatalog.presentation.ViewModel.UserViewModel
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    abstract fun bindUserViewModel(userViewModel: UserViewModel): ViewModel


    // Add bindings for other ViewModels here
}
