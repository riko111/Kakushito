package com.isoffice.kakushito.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.isoffice.kakushito.R

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
            Text(stringResource(R.string.about_app))
        },

        text = {

            Column {

                Text(stringResource(R.string.app_name))

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    stringResource(R.string.app_description)
                )
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
