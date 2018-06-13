package space.foxness.snapwalls

import android.content.Context

class Autoreddit private constructor(context: Context)
{
    private val settingsManager = SettingsManager.getInstance(context)

    private val saver = Saver()

    val reddit = Reddit.getInstance(saver)

    init
    {
        restoreConfig()
    }

    private fun restoreConfig()
    {
        reddit.accessToken = settingsManager.redditAccessToken
        reddit.refreshToken = settingsManager.redditRefreshToken
        reddit.accessTokenExpirationDate = settingsManager.redditAccessTokenExpirationDate
        reddit.lastSubmissionDate = settingsManager.redditLastSubmissionDate
    }

    private inner class Saver : Reddit.Callbacks
    {
        override fun onNewAccessToken()
        {
            settingsManager.redditAccessToken = reddit.accessToken
            settingsManager.redditAccessTokenExpirationDate = reddit.accessTokenExpirationDate
        }

        override fun onNewRefreshToken()
        {
            settingsManager.redditRefreshToken = reddit.refreshToken
        }

        override fun onNewLastSubmissionDate()
        {
            settingsManager.redditLastSubmissionDate = reddit.lastSubmissionDate
        }
    }

    companion object : SingletonHolder<Autoreddit, Context>(::Autoreddit)
}