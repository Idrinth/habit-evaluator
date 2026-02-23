package de.idrinth.habitevaluator.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.idrinth.habitevaluator.android.BuildConfig
import de.idrinth.habitevaluator.android.R

@Composable
fun ImprintScreen() {
    val context = LocalContext.current
    val version = BuildConfig.VERSION_NAME

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.imprint_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Version $version",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Contact section
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.imprint_contact),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.imprint_name_label),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.imprint_name_value),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.imprint_email_label),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.imprint_email_value),
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:self@idrinth.de"))
                context.startActivity(intent)
            }
        )

        // Divider
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        // License section
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.imprint_license),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.imprint_license_text),
            style = MaterialTheme.typography.bodyMedium
        )

        // Divider
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        // Data Protection section
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.imprint_data_protection),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.imprint_no_remote_storage),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.imprint_no_tracking),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.imprint_no_data_usage),
            style = MaterialTheme.typography.bodyMedium
        )

        // Divider
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        // Disclaimers section
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.imprint_disclaimers),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.imprint_not_professional_help),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.imprint_no_data_sharing),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.imprint_variant_disclaimer),
            style = MaterialTheme.typography.bodyMedium
        )

        // Divider
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        // Third-Party Libraries section
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.imprint_libraries),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.imprint_lib_gson), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_slf4j), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_snakeyaml), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_jakarta), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_jackson), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_slf4j_nop), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_appcompat), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_material), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_compose), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_activity_compose), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_navigation_compose), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_lifecycle_viewmodel), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_lifecycle_runtime), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_room), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_coroutines), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_openpdf), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_documentfile), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.imprint_lib_desugaring), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
    }
}
