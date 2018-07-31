package space.foxness.snapwalls

import android.content.Context
import space.foxness.snapwalls.database.AppDatabase

// todo: use cache instead of doing IO on every method call

class Queue private constructor(context: Context) // todo: refactor the app to use viewmodel/livedata
{
    private val postDao = AppDatabase.getInstance(context).postDao()
    
    private val thumbnailCache = ThumbnailCache.getInstance(context)

    val posts: List<Post> get() = postDao.posts

    fun addPost(post: Post) = postDao.addPost(post)

    fun getPost(id: String) = postDao.getPostById(id)

    fun updatePost(post: Post) = postDao.updatePost(post)

    fun deletePost(id: String)
    {
        postDao.deletePostbyId(id)
        
        if (thumbnailCache.contains(id))
        {
            thumbnailCache.remove(id)
        }
    }

    companion object : SingletonHolder<Queue, Context>(::Queue)
}
