package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.model.DiaryEntry
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.service.DiaryService
import de.idrinth.habitevaluator.shared.service.EventCorrelationService
import de.idrinth.habitevaluator.shared.service.HabitScoringService
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun PdfExportScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habits by viewModel.habits.collectAsState()
    val sleepEntries by viewModel.sleepEntries.collectAsState()
    val localUser by viewModel.localUser.collectAsState()

    val scoringService = remember { HabitScoringService() }
    val diaryService = remember { DiaryService() }
    val sleepService = remember { SleepEvaluationService() }
    val correlationService = remember { EventCorrelationService() }

    val dateFormat = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    var fromDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var includeHabits by remember { mutableStateOf(true) }
    var includeDiary by remember { mutableStateOf(true) }
    var includeSleep by remember { mutableStateOf(true) }
    var includeEmotions by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isExporting = true
        status = context.getString(R.string.generating)

        scope.launch(Dispatchers.IO) {
            try {
                val userId = localUser?.id ?: return@launch
                val diaryEntries = if (includeDiary) viewModel.diaryEntryRepository.findByUserId(userId) else emptyList()
                val emotionEntries = if (includeEmotions) viewModel.emotionEntryRepository.findByUserId(userId) else emptyList()
                val emotionPairs = if (includeEmotions) viewModel.emotionPairRepository.findByUserId(userId) else emptyList()

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val document = Document(PageSize.A4)
                    PdfWriter.getInstance(document, outputStream)
                    document.open()

                    val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)
                    val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)
                    val bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f)

                    document.add(Paragraph("Habit Evaluator Report", titleFont))
                    document.add(Paragraph("${fromDate.format(dateFormat)} to ${toDate.format(dateFormat)}", bodyFont))
                    document.add(Paragraph(" "))

                    if (includeHabits && habits.isNotEmpty()) {
                        document.add(Paragraph("Habits", headerFont))
                        document.add(Paragraph(" "))
                        val table = PdfPTable(3)
                        table.widthPercentage = 100f
                        table.addCell(PdfPCell(Phrase("Habit", bodyFont)))
                        table.addCell(PdfPCell(Phrase("Total Points", bodyFont)))
                        table.addCell(PdfPCell(Phrase("Days Active", bodyFont)))
                        habits.forEach { habit ->
                            val pts = scoringService.getCurrentMonthScore(habit)
                            table.addCell(PdfPCell(Phrase(habit.name ?: "", bodyFont)))
                            table.addCell(PdfPCell(Phrase("$pts", bodyFont)))
                            val daysActive = habit.entries?.filter {
                                val d = it.completedAt?.toLocalDate()
                                d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
                            }?.map { it.completedAt?.toLocalDate() }?.distinct()?.size ?: 0
                            table.addCell(PdfPCell(Phrase("$daysActive", bodyFont)))
                        }
                        document.add(table)
                        document.add(Paragraph(" "))
                    }

                    if (includeDiary && diaryEntries.isNotEmpty()) {
                        document.add(Paragraph("Diary", headerFont))
                        document.add(Paragraph(" "))
                        val filtered = diaryEntries.filter {
                            val d = it.eventDate
                            d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
                        }
                        filtered.forEach { entry ->
                            val desc = entry.diaryReference?.description ?: entry.legacyDescription ?: ""
                            document.add(Paragraph("${entry.eventDate?.format(dateFormat) ?: ""} - $desc (${entry.significance?.name ?: ""})", bodyFont))
                        }
                        document.add(Paragraph(" "))
                    }

                    if (includeSleep && sleepEntries.isNotEmpty()) {
                        document.add(Paragraph("Sleep", headerFont))
                        document.add(Paragraph(" "))
                        val filtered = sleepEntries.filter {
                            val d = it.date
                            d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
                        }
                        filtered.forEach { entry ->
                            document.add(Paragraph("${entry.date?.format(dateFormat) ?: ""}: ${entry.fromTime ?: ""} - ${entry.untilTime ?: ""}", bodyFont))
                        }
                        document.add(Paragraph(" "))
                    }

                    if (includeEmotions && emotionEntries.isNotEmpty()) {
                        document.add(Paragraph("Emotions", headerFont))
                        document.add(Paragraph(" "))
                        emotionEntries.filter {
                            val d = it.recordedAt?.toLocalDate()
                            d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
                        }.forEach { entry ->
                            val pair = emotionPairs.find { it.id == entry.emotionPair?.id }
                            val pairLabel = if (pair != null) "${pair.negativeLabel} / ${pair.positiveLabel}" else ""
                            document.add(Paragraph("${entry.recordedAt?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""}: $pairLabel = ${entry.strength}", bodyFont))
                        }
                        document.add(Paragraph(" "))
                    }

                    document.close()
                }

                withContext(Dispatchers.Main) {
                    status = context.getString(R.string.export_success)
                    isExporting = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    status = context.getString(R.string.export_error) + ": ${e.message}"
                    isExporting = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.pdf_export), style = MaterialTheme.typography.headlineMedium)

        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    val picked = LocalDate.of(y, m + 1, d)
                    fromDate = picked
                    if (picked.isAfter(toDate)) toDate = picked
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("${stringResource(R.string.from_date)}: ${fromDate.format(dateFormat)}") }

        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    val picked = LocalDate.of(y, m + 1, d)
                    toDate = picked
                    if (picked.isBefore(fromDate)) fromDate = picked
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("${stringResource(R.string.to_date)}: ${toDate.format(dateFormat)}") }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeHabits, onCheckedChange = { includeHabits = it })
            Text(stringResource(R.string.include_habits))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeDiary, onCheckedChange = { includeDiary = it })
            Text(stringResource(R.string.include_diary))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeSleep, onCheckedChange = { includeSleep = it })
            Text(stringResource(R.string.include_sleep))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeEmotions, onCheckedChange = { includeEmotions = it })
            Text(stringResource(R.string.include_emotions))
        }

        Button(
            onClick = {
                if (!includeHabits && !includeDiary && !includeSleep && !includeEmotions) {
                    Toast.makeText(context, R.string.select_at_least_one, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val filename = "habit_report_${fromDate.format(dateFormat)}_to_${toDate.format(dateFormat)}.pdf"
                createDocument.launch(filename)
            },
            enabled = !isExporting,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.export_pdf)) }

        if (status.isNotEmpty()) {
            Text(
                status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (status.contains("error", ignoreCase = true)) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
