package ap.panini.notflashy

import android.app.Application
import ap.panini.notflashy.data.AppContainer
import ap.panini.notflashy.data.AppDataContainer

class NotFlashyApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        container = AppDataContainer(this)
    }
}
