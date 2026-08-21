package com.isoffice.kakushito.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// ==================================================
// 隠す色選択ダイアログ
// ==================================================

@Composable
fun HideColorDialog(
    selectedColors: Set<Int>,
    onToggle: (Int) -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("隠す色")
        },

        text = {

            Column {

                Text(
                    text = "隠す対象のマーカー色を選択してください。",
                    modifier = Modifier.padding(
                        bottom = 12.dp
                    )
                )

                MarkerColors.forEach { color ->

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .pointerInput(color) {
                                    detectTapGestures {
                                        onToggle(color)
                                    }
                                },

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked =
                                color in selectedColors,

                            onCheckedChange = {
                                onToggle(color)
                            }
                        )

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