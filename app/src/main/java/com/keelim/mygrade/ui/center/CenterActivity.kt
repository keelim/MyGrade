package com.keelim.mygrade.ui.center

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.RemoteInput
import com.keelim.mygrade.databinding.ActivityCenterBinding
import com.keelim.mygrade.notification.NotificationBuilder
import com.keelim.mygrade.work.MainWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CenterActivity : AppCompatActivity() {
    private val viewModel: CenterViewModel by viewModels()
    private val binding: ActivityCenterBinding by lazy {
        ActivityCenterBinding.inflate(
            layoutInflater
        )
    }

    @Inject
    lateinit var notificationBuilder: NotificationBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        MainWorker.enqueueWork(this)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let{
            handleIntent(it)
        }
    }

    private fun handleIntent(intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        remoteInput?.let {
            val inputString = it.getCharSequence(KEY_TEXT_REPLY).toString()
            viewModel.saveHistory(inputString)
        }
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}