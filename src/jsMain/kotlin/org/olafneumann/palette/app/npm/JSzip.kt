package org.olafneumann.palette.app.npm

import org.w3c.files.Blob
import kotlin.js.Promise

@JsModule("JSZip")
@JsNonModule
external class JSZip {
    fun file(fileName: String, content: String)
    fun folder(folderName: String): JSZip
    fun generateAsync(type: dynamic): Promise<Blob>
}

/*
val text = "bla bla bla"
val zip = JSZip()
zip.file("testfile.txt", text)
zip.generateAsync(js("{type:'blob'}")).then {
    saveAs(it, "test.zip")
}
 */


// zip.generateAsync(mapOf("type" to "blob").toJson());
