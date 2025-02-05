package org.olafneumann.palette.app.npm

import org.olafneumann.palette.app.utils.toJson
import org.w3c.files.Blob
import kotlin.js.Promise

@JsModule("jszip")
@JsNonModule
external class JSZip {
    fun file(fileName: String, content: String)
    fun file(fileName: String, content: Blob)
    fun folder(folderName: String): JSZip
    fun generateAsync(type: dynamic): Promise<Blob>
}

fun JSZip.generateAsync(): Promise<Blob> =
    generateAsync(mapOf("type" to "blob").toJson())

/*
val text = "bla bla bla"
val zip = JSZip()
zip.file("testfile.txt", text)
zip.generateAsync(js("{type:'blob'}")).then {
    saveAs(it, "test.zip")
}
 */


// zip.generateAsync(mapOf("type" to "blob").toJson());
