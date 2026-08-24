package com.isoffice.kakushito.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.isoffice.kakushito.R


// ==================================================
// 使い方
// ==================================================

@Composable
fun HowToUseDialog(
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(stringResource(R.string.how_to_use))
        },

        text = {

            Column {

                Text(stringResource(R.string.how_to_open_pdf_title))

                Text(
                    stringResource(R.string.how_to_open_pdf_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_draw_marker_title))

                Text(
                    stringResource(R.string.how_to_draw_marker_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_hide_marker_title))

                Text(
                    stringResource(R.string.how_to_hide_marker_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_erase_marker_title))

                Text(
                    stringResource(R.string.how_to_erase_marker_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_marker_settings_title))

                Text(
                    stringResource(R.string.how_to_marker_settings_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_hide_color_title))

                Text(
                    stringResource(R.string.how_to_hide_color_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_zoom_title))

                Text(
                    stringResource(R.string.how_to_zoom_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.how_to_page_title))

                Text(
                    stringResource(R.string.how_to_page_description),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )
            }
        },

        confirmButton = {

            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
