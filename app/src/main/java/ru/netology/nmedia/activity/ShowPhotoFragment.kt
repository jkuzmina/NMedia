package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentShowPhotoBinding
private const val URI = "uri"
@AndroidEntryPoint
class ShowPhotoFragment : Fragment() {
    private var uri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uri = it.getString(URI)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentShowPhotoBinding.inflate(inflater, container, false)

        Glide.with(binding.photo)
            .load("${BuildConfig.BASE_URL}/media/${uri}")
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .fitCenter()
            .into(binding.photo)

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(uri: String) =
            ShowPhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(URI, uri)
                }
            }
    }
}