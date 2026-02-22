package de.idrinth.habitevaluator.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.BuildConfig
import de.idrinth.habitevaluator.android.R

@Composable
fun ImprintScreen() {
    val context = LocalContext.current
    val version = BuildConfig.VERSION_NAME

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.imprint), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Version $version", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.imprint_contact), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "self@idrinth.de",
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:self@idrinth.de"))
                context.startActivity(intent)
            }
        )
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.imprint_license), style = MaterialTheme.typography.bodyMedium)
    }
}
