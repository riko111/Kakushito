package com.isoffice.kakushito.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.isoffice.kakushito.R
import com.isoffice.kakushito.document.DocumentStore
// ==================================================
// 最近使ったファイル
// ==================================================

@Composable
fun RecentFilesDialog(
    store: DocumentStore,
    onFileSelected: (Uri) -> Unit,
    onDismiss: () -> Unit
) {

    val recentFiles =
        remember {
            store.loadRecentFiles()
        }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(stringResource(R.string.recent_files))
        },

        text = {

            if (recentFiles.isEmpty()) {

                Text(
                    stringResource(R.string.no_recent_files)
                )

            } else {

                Column {

                    recentFiles.forEach { file ->

                        TextButton(

                            onClick = {
                                onFileSelected(file.uri)
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = file.fileName,

                                maxLines = 1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
