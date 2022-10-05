package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel
import ru.netology.nmedia.util.AndroidUtils
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.signIn.setOnClickListener{
            AndroidUtils.hideKeyboard(requireView())
            viewModel.login.value = binding.login.text.toString()
            viewModel.pass.value = binding.pass.text.toString()
            viewModel.signIn()
        }

        viewModel.authState.observe(viewLifecycleOwner, { state ->
            auth.setAuth(state.id, state.token!!)
            findNavController().navigateUp()
        })

        viewModel.userAuthResult.observe(viewLifecycleOwner, { state ->
            if(state.error){
                Snackbar.make(binding.root, getString(R.string.auth_error), Snackbar.LENGTH_LONG)
                    .show()
            }
        })
        return binding.root
    }


}