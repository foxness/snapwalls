package space.foxness.snapwalls

import android.content.Context
import space.foxness.snapwalls.database.AppDatabase

class Queue private constructor(context: Context) // todo: refactor the app to use viewmodel/livedata
{
    private val dbDao = AppDatabase.getInstance(context).postDao()
    
    private val thumbnailCache = ThumbnailCache.getInstance(context)

    val posts: List<Post> get() = dbDao.posts

    fun addPost(post: Post) = dbDao.addPost(post)

    fun getPost(id: String) = dbDao.getPostById(id)

    fun updatePost(post: Post) = dbDao.updatePost(post)

    fun deletePost(id: String)
    {
        dbDao.deletePostbyId(id)
        
        if (thumbnailCache.contains(id))
        {
            thumbnailCache.remove(id)
        }
    }

    companion object : SingletonHolder<Queue, Context>(::Queue)
}
