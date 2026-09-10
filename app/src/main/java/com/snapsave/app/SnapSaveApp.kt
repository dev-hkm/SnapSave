package com.snapsave.app

import android.app.Application
import androidx.room.Room
import com.snapsave.app.core.FileStore
import com.snapsave.app.core.SettingsRepository
import com.snapsave.app.data.AppDatabase
import com.snapsave.app.data.SnippetRepository

class SnapSaveApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual DI container — đủ gọn, tránh rủi ro Hilt/KAPT khi build. */
class AppContainer(context: Application) {

    private val database: AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "snapsave.db"
    ).fallbackToDestructiveMigration().build()

    val fileStore: FileStore = FileStore(context)
    val repository: SnippetRepository = SnippetRepository(database.snippetDao(), fileStore)
    val settings: SettingsRepository = SettingsRepository(context)
}
