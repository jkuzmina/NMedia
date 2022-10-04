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
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth
    private val viewModel: SignUpViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.signUp.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.pass.text.toString() != binding.passRepeat.text.toString()) {
                Snackbar.make(binding.root, "Passwords don't match ", Snackbar.LENGTH_LONG).show()
            } else {
                viewModel.name.value = binding.name.text.toString()
                viewModel.login.value = binding.login.text.toString()
                viewModel.pass.value = binding.pass.text.toString()
                viewModel.signUp()
            }

        }

        viewModel.authState.observe(viewLifecycleOwner, { state ->
            auth.setAuth(state.id, state.token!!)
            findNavController().navigateUp()
        })

        viewModel.userAuthResult.observe(viewLifecycleOwner, { state ->
            if(state.error){
                Snackbar.make(binding.root, getString(R.string.reg_error), Snackbar.LENGTH_LONG)
                    .show()
            }
        })
        return binding.root
    }
}