package com.slf.module.di


import com.slf.module.presentation.fragments.EditProfileFragment
import com.slf.module.presentation.fragments.RegisterFragment
import com.slf.module.presentation.ui.MovieDetailsActivity
import com.slf.module.presentation.ui.UserProfileActivity
import com.slf.module.di.ViewModelModule
import com.slf.module.presentation.ui.MainActivity
import com.slf.module.presentation.ui.LoginActivity
import com.slf.module.presentation.fragments.LoginFragment
import com.slf.module.presentation.fragments.ProfileUserFragment
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
