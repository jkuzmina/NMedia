package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PagingLoadStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.SignInDialogFragment
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels()
    @Inject
    lateinit var auth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("onCreate", "hited")
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if(!auth.authenticated()){
                    showSignInDialog()
                }else {
                    viewModel.likeById(post)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onPhotoClick(post: Post) {
                val bundle = Bundle()
                bundle.putLong("postId", post.id)
                bundle.putString("uri", post.attachment?.url)
                val navController = findNavController()
                navController.navigate(R.id.action_feedFragment_to_showPhotoFragment, bundle)
            }
        })
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

        binding.newPosts.setOnClickListener {
            viewModel.readNewPosts()
            binding.newPosts.isVisible = false
            binding.list.smoothScrollToPosition(0)
        }

        binding.fab.setOnClickListener {
            if(!auth.authenticated()){
                showSignInDialog()
            }else{
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        return binding.root
    }

    fun showSignInDialog(){
        val dialog = SignInDialogFragment()
        dialog.show(getParentFragmentManager(), getString(R.string.authentication))
    }
}
