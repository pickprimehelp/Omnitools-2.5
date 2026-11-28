package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.findActivity
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent

private enum class PendingExportAction {
    SAVE,
    SHARE
}

@Composable
fun WatermarkExportBar(
    hasWatermark: Boolean,
    onWatermarkUnlocked: () -> Unit,
    onExportSave: (includeWatermark: Boolean) -> Unit,
    onExportShare: (includeWatermark: Boolean) -> Unit,
    saveLabel: String = "Save HD",
    shareLabel: String = "Share",
    saveTestTag: String = "save_export_button",
    shareTestTag: String = "share_export_button"
) {
    val context = LocalContext.current
    val activity: Activity? = remember(context) { context.findActivity() }
    var isLoadingAd by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingExportAction?>(null) }

    fun triggerRewardedUnlock(thenExport: PendingExportAction? = null) {
        if (activity == null || isLoadingAd) return
        AdManager.showRewardedAd(
            activity = activity,
            onLoadingChanged = { loading -> isLoadingAd = loading },
            onRewardEarned = {
                onWatermarkUnlocked()
            },
            onAdUnavailable = {
                // Keep watermark ON when rewarded ad could not load
            },
            onDismissed = { rewardEarned ->
                if (rewardEarned) {
                    when (thenExport) {
                        PendingExportAction.SAVE -> onExportSave(false)
                        PendingExportAction.SHARE -> onExportShare(false)
                        null -> Unit
                    }
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasWatermark) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                } else {
                    EmeraldSuccess.copy(alpha = 0.14f)
                }
            ),
            border = BorderStroke(
                1.dp,
                if (hasWatermark) GoldAccent.copy(alpha = 0.5f) else EmeraldSuccess.copy(alpha = 0.55f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasWatermark) Icons.Default.PlayCircle else Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (hasWatermark) GoldAccent else EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasWatermark) "Watermark Active" else "No Watermark Unlocked",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (hasWatermark) {
                            "Watch a rewarded ad to save clean HD image without watermark."
                        } else {
                            "Your image will save & share without any watermark."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }

                if (hasWatermark) {
                    Button(
                        onClick = { triggerRewardedUnlock(thenExport = null) },
                        enabled = !isLoadingAd,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("remove_watermark_ad_button")
                    ) {
                        if (isLoadingAd) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Loading...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Watch Ad", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Watermark Removed",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (!hasWatermark) {
                        onExportSave(false)
                    } else {
                        pendingAction = PendingExportAction.SAVE
                    }
                },
                enabled = !isLoadingAd,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag(saveTestTag),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(saveLabel, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {
                    if (!hasWatermark) {
                        onExportShare(false)
                    } else {
                        pendingAction = PendingExportAction.SHARE
                    }
                },
                enabled = !isLoadingAd,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag(shareTestTag),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(shareLabel, fontWeight = FontWeight.Bold)
            }
        }
    }

    pendingAction?.let { action ->
        val actionLabel = if (action == PendingExportAction.SAVE) "Save" else "Share"
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = {
                Text("$actionLabel Clean Image?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Watch a short Rewarded Ad to $actionLabel this image cleanly without watermark. " +
                        "If you continue without watching the ad, the OmniTools watermark will be included.",
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction = null
                        triggerRewardedUnlock(thenExport = action)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_watch_ad_clean_export")
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch Ad (No Watermark)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            pendingAction = null
                            if (action == PendingExportAction.SAVE) {
                                onExportSave(true)
                            } else {
                                onExportShare(true)
                            }
                        },
                        modifier = Modifier.testTag("export_with_watermark_button")
                    ) {
                        Text("$actionLabel With Watermark")
                    }
                }
            }
        )
    }
}
