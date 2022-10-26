package ru.netology.nmedia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.CardTimeSeparatorBinding
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeSeparator
import ru.netology.nmedia.enumeration.TimeSeparatorValue

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onPhotoClick(post: Post) {}
}

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,
    @ApplicationContext private val context: Context,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {
    private val typeTimeSeparator = 0
    private val typePost = 1

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimeSeparator -> typeTimeSeparator
            is Post -> typePost
            null -> throw IllegalArgumentException("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            typeTimeSeparator -> TimeSeparatorViewHolder(
                CardTimeSeparatorBinding.inflate(layoutInflater, parent, false),
                onInteractionListener,
                context
            )
            typePost -> PostViewHolder(
                CardPostBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // FIXME: students will do in HW
        getItem(position)?.let {
            when (it) {
                is Post -> (holder as? PostViewHolder)?.bind(it)
                is TimeSeparator -> (holder as? TimeSeparatorViewHolder)?.bind(it)
            }
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {


    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"
            Glide.with(avatar)
                .load("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .circleCrop()
                .into(binding.avatar)
            if(post.attachment?.url != null) {
                imageAttachment.isVisible = true
                Glide.with(imageAttachment)
                    .load("${BuildConfig.BASE_URL}/media/${post.attachment.url}")
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .fitCenter()
                    .into(binding.imageAttachment)
            } else{
                imageAttachment.isVisible = false
                Glide.with(imageAttachment).clear(binding.imageAttachment)
            }

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            imageAttachment.setOnClickListener {
                onInteractionListener.onPhotoClick(post)
            }
        }
    }
}

class TimeSeparatorViewHolder(
    private val binding: CardTimeSeparatorBinding,
    private val onInteractionListener: OnInteractionListener,
    @ApplicationContext private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(timeSeparator: TimeSeparator) {
        binding.apply {
            when (timeSeparator.value){
                TimeSeparatorValue.TODAY -> timeText.text = context.getString(R.string.today)
                TimeSeparatorValue.YESTERDAY -> timeText.text = context.getString(R.string.yesterday)
                TimeSeparatorValue.LAST_WEEK -> timeText.text = context.getString(R.string.last_week)
            }
        }
    }
}

class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
