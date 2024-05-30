package com.slf.moviescatalog.di

import com.slf.moviescatalog.presentation.ui.MainActivity
import com.slf.moviescatalog.presentation.ui.MovieDetailsActivity
import com.slf.moviescatalog.presentation.ui.UserProfileActivity
import com.slf.moviescatalog.presentation.ui.LoginActivity
import com.slf.moviescatalog.presentation.fragments.EditProfileFragment
import com.slf.moviescatalog.presentation.fragments.LoginFragment
import com.slf.moviescatalog.presentation.fragments.ProfileUserFragment
import com.slf.moviescatalog.presentation.fragments.RegisterFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(movieDetailsActivity: MovieDetailsActivity)
    fun inject(userProfileActivity: UserProfileActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(editProfileFragment: EditProfileFragment)
    fun inject(loginFragment: LoginFragment)
    fun inject(profileUserFragment: ProfileUserFragment)
    fun inject(registerFragment: RegisterFragment)
    // Add injections for other activities and fragments as needed
}
