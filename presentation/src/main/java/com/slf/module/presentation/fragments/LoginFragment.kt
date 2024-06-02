package com.slf.module.presentation.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.slf.module.presentation.R
import com.slf.module.presentation.databinding.FragmentLoginBinding
import com.slf.module.data.model.UserRepository
import com.slf.module.presentation.ui.MainActivity
import com.slf.module.utils.Constant
import com.slf.module.utils.SharedHelper
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var bind : FragmentLoginBinding? = null
    private val binding get() = bind!!
    private lateinit var userRepository: UserRepository
    private lateinit var shared: SharedHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentLoginBinding.inflate(inflater, container, false)
        userRepository = UserRepository(requireContext())
        shared = SharedHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            snackbaropen()

            // Menggabungkan dua string
            val signUpText = getString(R.string.have_account, getString(R.string.sign_up))

            // Membuat "Sign Up" menjadi tebal dan berwarna biru
            val spannableString = SpannableString(signUpText)
            val startIndex = signUpText.indexOf(getString(R.string.sign_up))
            val endIndex = startIndex + getString(R.string.sign_up).length
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(Color.BLUE), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Mengatur teks untuk TextView
            Registerw.text = spannableString

            Login.setOnClickListener {
                binding.apply {
                    val user = Username.text.toString()
                    val pass = Password.text.toString()

                    when {
                        user.isEmpty() && pass.isEmpty() -> {
                            Username.error = "Fill the username"
                            Password.error = "Fill the password"
                        }
                        user.isEmpty() -> Username.error = "Fill the username"
                        pass.isEmpty() -> Password.error = "Fill the password"
                        else -> {
                            lifecycleScope.launch(Dispatchers.IO){
                                val data = userRepository.getUser()

                                activity?.runOnUiThread {
                                    when (data.username) {
                                        user -> when (data.password) {
                                            pass -> {
                                                loginSession(user, pass)
                                                Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                                                activity?.let {
                                                    val intent = Intent(it, MainActivity::class.java)
                                                    it.startActivity(intent)}
                                            }
                                            else -> Toast.makeText(requireContext(), "Wrong Password", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> Toast.makeText(requireContext(), "Wrong Username", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    //SnackBar
    private fun snackbaropen() {
        Registerw.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            Snackbar.make(it,"Please Enter Your Data", Snackbar.LENGTH_LONG)
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                .setBackgroundTint(Color.parseColor("#000000"))
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (shared.getBoolean(Constant.LOGIN, false)){
            activity?.let {
                val intent = Intent(it, MainActivity::class.java)
                it.startActivity(intent)}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun loginSession(user: String, pass: String) {
        shared.apply {
            put(Constant.USERNAME, user)
            put(Constant.PASSWORD, pass)
            put(Constant.LOGIN, true)
        }
    }
}
