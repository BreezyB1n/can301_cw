package com.example.can301_cw.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    defaultOffsetMinutes: Int = 5,
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    // Use LocalDateTime for accurate current time in system zone
    val now = LocalDateTime.now()
    
    // Calculate start of today in UTC for DatePicker (which expects UTC milliseconds)
    val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayUtcMillis
    )
    
    // Initial time: now + defaultOffsetMinutes minutes
    val initialTime = now.plusMinutes(defaultOffsetMinutes.toLong())

    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        // Time Picker
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select Time",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Back")
                        }
                        TextButton(onClick = {
                            // Combine Date (UTC) + Time (Local)
                            val selectedDateUtc = datePickerState.selectedDateMillis ?: todayUtcMillis
                            
                            // Convert UTC timestamp from DatePicker back to LocalDate
                            val selectedLocalDate = java.time.Instant.ofEpochMilli(selectedDateUtc)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            
                            // Combine with selected time
                            val selectedLocalDateTime = LocalDateTime.of(
                                selectedLocalDate,
                                java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                            )
                            
                            // Convert final LocalDateTime to system zone epoch millis
                            val finalTimestamp = selectedLocalDateTime
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                                
                            onConfirm(finalTimestamp)
                        }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    } else {
        // Date Picker
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            tonalElevation = 0.dp,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
