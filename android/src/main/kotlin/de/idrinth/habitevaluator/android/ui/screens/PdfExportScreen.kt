package de.idrinth.habitevaluator.android.ui.screens

import android.app.DatePickerDialog
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
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
import de.idrinth.habitevaluator.android.AppViewModel
import de.idrinth.habitevaluator.android.R
import de.idrinth.habitevaluator.shared.service.HabitScoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842
private const val MARGIN = 40f
private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

@Composable
fun PdfExportScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habits by viewModel.habits.collectAsState()
    val sleepEntries by viewModel.sleepEntries.collectAsState()
    val localUser by viewModel.localUser.collectAsState()

    val scoringService = remember { HabitScoringService() }

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
                val userId = localUser?.id
                if (userId == null) {
                    withContext(Dispatchers.Main) {
                        status = context.getString(R.string.export_error) + ": No user found"
                        isExporting = false
                    }
                    return@launch
                }
                val diaryEntries = if (includeDiary) viewModel.diaryEntryRepository.findByUserId(userId) else emptyList()
                val emotionEntries = if (includeEmotions) viewModel.emotionEntryRepository.findByUserId(userId) else emptyList()
                val emotionPairs = if (includeEmotions) viewModel.emotionPairRepository.findByUserId(userId) else emptyList()

                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    withContext(Dispatchers.Main) {
                        status = context.getString(R.string.export_error) + ": Could not open file"
                        isExporting = false
                    }
                    return@launch
                }

                outputStream.use { stream ->
                    generatePdf(stream, fromDate, toDate, dateFormat, includeHabits, habits, scoringService,
                        includeDiary, diaryEntries, includeSleep, sleepEntries,
                        includeEmotions, emotionEntries, emotionPairs)
                }

                withContext(Dispatchers.Main) {
                    status = context.getString(R.string.export_success)
                    isExporting = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    status = context.getString(R.string.export_error) + ": ${e.message ?: "Unknown error"}"
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

private fun generatePdf(
    outputStream: OutputStream,
    fromDate: LocalDate,
    toDate: LocalDate,
    dateFormat: DateTimeFormatter,
    includeHabits: Boolean,
    habits: List<de.idrinth.habitevaluator.shared.model.Habit>,
    scoringService: HabitScoringService,
    includeDiary: Boolean,
    diaryEntries: List<de.idrinth.habitevaluator.shared.model.DiaryEntry>,
    includeSleep: Boolean,
    sleepEntries: List<de.idrinth.habitevaluator.shared.model.SleepEntry>,
    includeEmotions: Boolean,
    emotionEntries: List<de.idrinth.habitevaluator.shared.model.EmotionEntry>,
    emotionPairs: List<de.idrinth.habitevaluator.shared.model.EmotionPair>
) {
    val document = PdfDocument()
    var pageNumber = 1
    var page = document.startPage(
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
    )
    var canvas = page.canvas
    var yPos = MARGIN

    val titlePaint = TextPaint().apply {
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    val headerPaint = TextPaint().apply {
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    val bodyPaint = TextPaint().apply {
        textSize = 10f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }
    val tablePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.5f
        isAntiAlias = true
    }

    fun startNewPage() {
        document.finishPage(page)
        pageNumber++
        page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        canvas = page.canvas
        yPos = MARGIN
    }

    fun ensureSpace(needed: Float) {
        if (yPos + needed > PAGE_HEIGHT - MARGIN) {
            startNewPage()
        }
    }

    fun drawText(text: String, paint: TextPaint, maxWidth: Float = CONTENT_WIDTH): Float {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .build()
        val height = layout.height.toFloat()
        ensureSpace(height)
        canvas.save()
        canvas.translate(MARGIN, yPos)
        layout.draw(canvas)
        canvas.restore()
        yPos += height
        return height
    }

    fun drawSpacing(height: Float = 12f) {
        yPos += height
    }

    // Title
    drawText("Habit Evaluator Report", titlePaint)
    drawSpacing(4f)
    drawText("${fromDate.format(dateFormat)} to ${toDate.format(dateFormat)}", bodyPaint)
    drawSpacing(16f)

    // Habits section
    if (includeHabits && habits.isNotEmpty()) {
        drawText("Habits", headerPaint)
        drawSpacing(8f)

        val col1Width = CONTENT_WIDTH * 0.50f
        val col2Width = CONTENT_WIDTH * 0.25f
        val col3Width = CONTENT_WIDTH * 0.25f
        val cellPadding = 4f
        val rowHeight = bodyPaint.textSize + 2 * cellPadding

        // Table header
        ensureSpace(rowHeight)
        val headerY = yPos
        canvas.drawRect(MARGIN, headerY, MARGIN + CONTENT_WIDTH, headerY + rowHeight, tablePaint)
        canvas.drawLine(MARGIN + col1Width, headerY, MARGIN + col1Width, headerY + rowHeight, tablePaint)
        canvas.drawLine(MARGIN + col1Width + col2Width, headerY, MARGIN + col1Width + col2Width, headerY + rowHeight, tablePaint)
        val headerTextPaint = TextPaint(bodyPaint).apply { typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Habit", MARGIN + cellPadding, headerY + cellPadding + bodyPaint.textSize, headerTextPaint)
        canvas.drawText("Total Points", MARGIN + col1Width + cellPadding, headerY + cellPadding + bodyPaint.textSize, headerTextPaint)
        canvas.drawText("Days Active", MARGIN + col1Width + col2Width + cellPadding, headerY + cellPadding + bodyPaint.textSize, headerTextPaint)
        yPos = headerY + rowHeight

        // Table rows
        habits.forEach { habit ->
            ensureSpace(rowHeight)
            val rowY = yPos
            canvas.drawRect(MARGIN, rowY, MARGIN + CONTENT_WIDTH, rowY + rowHeight, tablePaint)
            canvas.drawLine(MARGIN + col1Width, rowY, MARGIN + col1Width, rowY + rowHeight, tablePaint)
            canvas.drawLine(MARGIN + col1Width + col2Width, rowY, MARGIN + col1Width + col2Width, rowY + rowHeight, tablePaint)

            val pts = scoringService.getCurrentMonthScore(habit)
            val daysActive = habit.entries?.filter {
                val d = it.completedAt?.toLocalDate()
                d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
            }?.map { it.completedAt?.toLocalDate() }?.distinct()?.size ?: 0

            val name = truncateToFit(habit.name ?: "", bodyPaint, col1Width - 2 * cellPadding)
            canvas.drawText(name, MARGIN + cellPadding, rowY + cellPadding + bodyPaint.textSize, bodyPaint)
            canvas.drawText("$pts", MARGIN + col1Width + cellPadding, rowY + cellPadding + bodyPaint.textSize, bodyPaint)
            canvas.drawText("$daysActive", MARGIN + col1Width + col2Width + cellPadding, rowY + cellPadding + bodyPaint.textSize, bodyPaint)
            yPos = rowY + rowHeight
        }
        drawSpacing(16f)
    }

    // Diary section
    if (includeDiary && diaryEntries.isNotEmpty()) {
        drawText("Diary", headerPaint)
        drawSpacing(8f)
        val filtered = diaryEntries.filter {
            val d = it.eventDate
            d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
        }
        filtered.forEach { entry ->
            val desc = entry.diaryReference?.description ?: entry.legacyDescription ?: ""
            drawText("${entry.eventDate?.format(dateFormat) ?: ""} - $desc (${entry.significance?.name ?: ""})", bodyPaint)
            drawSpacing(2f)
        }
        drawSpacing(12f)
    }

    // Sleep section
    if (includeSleep && sleepEntries.isNotEmpty()) {
        drawText("Sleep", headerPaint)
        drawSpacing(8f)
        val filtered = sleepEntries.filter {
            val d = it.date
            d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
        }
        filtered.forEach { entry ->
            drawText("${entry.date?.format(dateFormat) ?: ""}: ${entry.fromTime ?: ""} - ${entry.untilTime ?: ""}", bodyPaint)
            drawSpacing(2f)
        }
        drawSpacing(12f)
    }

    // Emotions section
    if (includeEmotions && emotionEntries.isNotEmpty()) {
        drawText("Emotions", headerPaint)
        drawSpacing(8f)
        val dtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        emotionEntries.filter {
            val d = it.recordedAt?.toLocalDate()
            d != null && !d.isBefore(fromDate) && !d.isAfter(toDate)
        }.forEach { entry ->
            val pair = emotionPairs.find { it.id == entry.emotionPair?.id }
            val pairLabel = if (pair != null) "${pair.negativeLabel} / ${pair.positiveLabel}" else ""
            drawText("${entry.recordedAt?.format(dtFormat) ?: ""}: $pairLabel = ${entry.strength}", bodyPaint)
            drawSpacing(2f)
        }
        drawSpacing(12f)
    }

    document.finishPage(page)
    document.writeTo(outputStream)
    document.close()
}

private fun truncateToFit(text: String, paint: TextPaint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text
    val ellipsis = "..."
    val ellipsisWidth = paint.measureText(ellipsis)
    var end = text.length
    while (end > 0 && paint.measureText(text, 0, end) + ellipsisWidth > maxWidth) {
        end--
    }
    return text.substring(0, end) + ellipsis
}
