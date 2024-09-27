@file:JsModule("file-saver")
@file:JsNonModule

package org.olafneumann.palette.app

import org.w3c.files.Blob

// FileSaver saveAs(Blob/File/Url, optional DOMString filename, optional Object { autoBom })
external fun saveAs(data: String, filename: String)
external fun saveAs(data: Blob, filename: String)
