import android.net.Uri
import kotlinx.coroutines.*
import ly.img.engine.*

fun saveSceneToString() = CoroutineScope(Dispatchers.Main).launch {
    val engine = Engine(id = "ly.img.engine.example")
    engine.start()
    engine.bindOffscreen(width = 100, height = 100)

    val sceneUri = Uri.parse("https://cdn.img.ly/assets/demo/v1/ly.img.template/templates/cesdk_postcard_1.scene")
    val scene = engine.scene.load(sceneUri = sceneUri)

    // highlight-save
    val savedSceneString = engine.scene.saveToString(scene = scene)
    // highlight-save

    // highlight-result
    println(savedSceneString)
    // highlight-result

    engine.stop()
}
