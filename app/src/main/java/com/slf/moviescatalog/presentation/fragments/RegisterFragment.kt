package com.slf.moviescatalog.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.slf.moviescatalog.R
import com.slf.moviescatalog.databinding.FragmentRegisterBinding
import com.slf.moviescatalog.data.model.User
import com.slf.moviescatalog.data.model.UserRepository
import com.slf.moviescatalog.utils.DatePickerFragment
import com.slf.moviescatalog.utils.Validation
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var bind : FragmentRegisterBinding? = null
    private val binding get() = bind!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentRegisterBinding.inflate(inflater, container, false)
        userRepository = UserRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            birthday.setOnClickListener { showDatePickerDialog() }
            submit.setOnClickListener {
                var isValid = true

                if (fullname.text.isNullOrEmpty()) {
                    FullNameLayout.error = "Fill column name"
                    isValid = false
                } else {
                    FullNameLayout.error = null
                }

                if (email_id.text.isNullOrEmpty()) {
                    EmailLayout.error = "Fill column email"
                    isValid = false
                } else if (!Validation.isValidEmail(email_id.text.toString())) {
                    EmailLayout.error = "Enter a valid email"
                    isValid = false
                } else {
                    EmailLayout.error = null
                }

                if (birthday.text.isNullOrEmpty()) {
                    BirthdayLayout.error = "Fill column birthday"
                    isValid = false
                } else {
                    BirthdayLayout.error = null
                }

                if (address.text.isNullOrEmpty()) {
                    AddressLayout.error = "Fill column address"
                    isValid = false
                } else {
                    AddressLayout.error = null
                }

                if (usernameig.text.isNullOrEmpty()) {
                    UsernameLayout.error = "Fill column username"
                    isValid = false
                } else {
                    UsernameLayout.error = null
                }

                if (pws.text.isNullOrEmpty()) {
                    PasswordRegisterLayout.error = "Fill column password"
                    isValid = false
                } else if (!Validation.isValidPassword(pws.text.toString())) {
                    PasswordRegisterLayout.error = "Password must be at least 8 characters"
                    isValid = false
                } else {
                    PasswordRegisterLayout.error = null
                }

                if (isValid) {
                    val objDataUser = User(
                        null,
                        fullname.text.toString(),
                        null,
                        email_id.text.toString(),
                        birthday.text.toString(),
                        address.text.toString(),
                        usernameig.text.toString(),
                        pws.text.toString()
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        val user = userRepository.getUser()
                        if (user.username != usernameig.text.toString()) {
                            userRepository.saveUser(objDataUser)
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Register User Success", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            }
                        } else {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Username has taken", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    fun showDatePickerDialog() {
        val newFragment = DatePickerFragment()
        newFragment.show(childFragmentManager, "datePicker")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }
}
