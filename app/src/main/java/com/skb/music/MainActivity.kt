package com.skb.music

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.skb.music.ui.SkbApp
import com.skb.music.ui.theme.SkbMusicTheme
import com.skb.music.viewmodel.LibraryViewModel

class MainActivity : ComponentActivity() {

    private val libraryVm: LibraryViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) libraryVm.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            perms += Manifest.permission.READ_MEDIA_AUDIO
            perms += Manifest.permission.POST_NOTIFICATIONS
        } else {
            perms += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        permissionLauncher.launch(perms.toTypedArray())

        setContent {
            SkbMusicTheme {
                SkbApp(libraryVm)
            }
        }
    }
}
