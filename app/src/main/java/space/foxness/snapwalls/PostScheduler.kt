package space.foxness.snapwalls

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.github.debop.kodatimes.times
import org.joda.time.DateTime
import org.joda.time.Duration

// todo: make reddit not a singleton and rename it to reddit account

class PostScheduler private constructor(context: Context)
{ // todo: throw exception if schedule time is in the past

    private val context = context.applicationContext!!

    private val queue = Queue.getInstance(context)

    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // this method expects the queue to be divided into 2 segments
    // the first segment is the scheduled segment at the beginning
    // the last segment is the segment that will be scheduled
    fun scheduleUnscheduledPostsPeriodic(period: Duration) // todo: refactor this to use schedulePost()
    {
        val posts = queue.posts

        if (posts.size < 2)
        {
            throw Exception("Need at least 2 posts")
        }

        if (posts.first().intendedSubmitDate == null)
        {
            throw Exception("Can't infer the periodic schedule")
        }

        var onlyUnscheduledNow = false
        for (i in 1 until posts.size)
        {
            if (posts[i].intendedSubmitDate == null)
            {
                posts[i].intendedSubmitDate = posts[i - 1].intendedSubmitDate!! + period
                posts[i].scheduled = true
                queue.updatePost(posts[i])
                onlyUnscheduledNow = true

            }
            else if (onlyUnscheduledNow)
            {
                throw Exception("You can't switch from unscheduled to scheduled posts")
            }
        }
    }

    fun cancelScheduledPosts(posts: List<Post>) = posts.forEach { cancelScheduledPost(it) }

    fun cancelScheduledPost(post: Post)
    {
        val esp = getEarliestScheduledPost()!!

        post.scheduled = false
        queue.updatePost(post)

        if (esp.id == post.id)
        {
            cancelScheduledService()

            val esp2 = getEarliestScheduledPost()
            if (esp2 != null)
            {
                scheduleService(esp2.intendedSubmitDate!!)
            }
        }
    }

    fun schedulePeriodicPosts(posts: List<Post>, period: Duration, initialDelay: Duration)
    {
        val now = DateTime.now()
        posts.forEachIndexed({ index, post ->
                                 val datetime = now + initialDelay + period * index.toLong()
                                 schedulePost(post, datetime)
                             })
    }

    fun schedulePost(post: Post, datetime: DateTime)
    {
        val esp = getEarliestScheduledPost()

        post.intendedSubmitDate = datetime
        post.scheduled = true
        queue.updatePost(post)

        val firstPost = esp == null
        val earlierPost = !firstPost && esp!!.intendedSubmitDate!! > datetime

        if (firstPost || earlierPost)
        {
            if (earlierPost)
            {
                cancelScheduledService()
            }

            scheduleService(datetime)
        }
    }

    fun scheduleServiceForNextPost()
    {
        val esp = getEarliestScheduledPost() ?: throw Exception("No scheduled posts found")
        scheduleService(esp.intendedSubmitDate!!)
    }

    private fun scheduleService(datetime: DateTime)
    {
        val type = getAlarmType(false, true)
        val pi = getPendingIntent()!!

        val delay = Duration(DateTime.now(), datetime)
        val datetimeElapsed = Duration(SystemClock.elapsedRealtime()) + delay

        alarmManager.setExact(type, datetimeElapsed.millis, pi)
    }

    private fun cancelScheduledService()
    {
        val pi = getPendingIntent()!!
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun isServiceScheduled() = getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null

    private fun getEarliestScheduledPost() =
            queue.posts.filter { it.scheduled }.minBy { it.intendedSubmitDate!!.millis }

    private fun getPendingIntent(flags: Int = 0): PendingIntent?
    {
        val intent = AutosubmitService.newIntent(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            PendingIntent.getForegroundService(context, REQUEST_CODE, intent, flags)
        }
        else
        {
            PendingIntent.getService(context, REQUEST_CODE, intent, flags)
        }
    }

    companion object : SingletonHolder<PostScheduler, Context>(::PostScheduler)
    {
        private const val REQUEST_CODE = 0

        private fun getAlarmType(rtc: Boolean = false, wakeup: Boolean = true): Int
        {
            return if (rtc)
            {
                if (wakeup)
                {
                    AlarmManager.RTC_WAKEUP
                }
                else
                {
                    AlarmManager.RTC
                }
            }
            else
            {
                if (wakeup)
                {
                    AlarmManager.ELAPSED_REALTIME_WAKEUP
                }
                else
                {
                    AlarmManager.ELAPSED_REALTIME
                }
            }
        }
    }
}