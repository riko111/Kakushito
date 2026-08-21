package com.isoffice.kakushito.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// ==================================================
// マーカー設定ダイアログ
// ==================================================
@Composable
fun MarkerSettingsDialog(
    selectedWidth: Float,
    selectedColor: Int,
    onWidthSelected: (Float) -> Unit,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("マーカー設定")
        },

        text = {

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // ------------------------------------------
                // 太さ
                // ------------------------------------------

                Text(
                    text = "太さ",
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    MarkerWidths.forEach { width ->

                        Box(
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .then(
                                        if (
                                            width ==
                                            selectedWidth
                                        ) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFFFFC107),
                                                CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .pointerInput(width) {
                                        detectTapGestures {
                                            onWidthSelected(width)
                                        }
                                    },

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Box(
                                modifier =
                                    Modifier
                                        .size(width.dp)
                                        .background(
                                            Color(0xFFFFC107),
                                            CircleShape
                                        )
                            )
                        }
                    }
                }


                Spacer(
                    modifier = Modifier.height(20.dp)
                )


                // ------------------------------------------
                // 色
                // ------------------------------------------

                Text(
                    text = "色",
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    MarkerColors.forEach { color ->

                        Box(
                            modifier =
                                Modifier
                                    .size(52.dp)
                                    .then(
                                        if (
                                            color ==
                                            selectedColor
                                        ) {
                                            Modifier.border(
                                                2.dp,
                                                Color.DarkGray,
                                                CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .pointerInput(color) {
                                        detectTapGestures {
                                            onColorSelected(color)
                                        }
                                    },

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Box(
                                modifier =
                                    Modifier
                                        .size(28.dp)
                                        .background(
                                            Color(color),
                                            CircleShape
                                        )
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
                Text("閉じる")
            }
        }
    )
}