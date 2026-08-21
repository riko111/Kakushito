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
import androidx.compose.ui.unit.dp


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
            Text("使い方")
        },

        text = {

            Column {

                Text("① PDFを開く")

                Text(
                    "「PDFを開く」からPDFを選択します。",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text("② マーカーを引く")

                Text(
                    "マーカーをタップしてONにし、PDFを指でなぞります。",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text("③ マーカー部分を隠す")

                Text(
                    "「隠す」をタップすると、マーカー部分を隠して暗記できます。",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text("④ マーカーを消す")

                Text(
                    "消しゴムをタップして、不要なマーカーを消します。",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text("⑤ マーカーを長押し")

                Text(
                    "マーカーを長押しすると、太さと色を変更できます。",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text("⑥ 隠すを長押し")

                Text(
                    "隠すを長押しすると、隠す対象の色を選択できます。",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
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