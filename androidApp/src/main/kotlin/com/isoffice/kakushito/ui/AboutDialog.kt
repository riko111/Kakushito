package com.isoffice.kakushito.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ==================================================
// このアプリについて
// ==================================================

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("このアプリについて")
        },

        text = {

            Column {

                Text("かくしーと")

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    "PDFにマーカーを引いて、重要な部分を隠しながら暗記できる学習アプリです。"
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("閉じる")
            }
        }
    )
}