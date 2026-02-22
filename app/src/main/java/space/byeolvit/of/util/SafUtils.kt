package space.byeolvit.of.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun takePersistablePermission(context: Context, uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(uri, flags)
}

fun isUriPermissionValid(context: Context, uri: Uri): Boolean {
    return context.contentResolver.persistedUriPermissions.any {
        it.uri == uri && it.isReadPermission && it.isWritePermission
    }
}
