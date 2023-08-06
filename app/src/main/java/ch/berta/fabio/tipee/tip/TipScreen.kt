package ch.berta.fabio.tipee.tip

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.theme.TipeeTheme

@Composable
fun TipScreen(modifier: Modifier = Modifier) {
    val tipState = rememberTipState(Countries.first())
    val countryCode = tipState.selectedCountry.code
    Column(
        modifier =
            modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .scrollable(rememberScrollState(), Orientation.Vertical)
    ) {
        SubHeading(stringResource(R.string.bill_amount_and_persons))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.bill_amount)) },
                value = tipState.billAmount,
                onValueChange = tipState::onBillAmountChange,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = tipState::onBillAmountClear) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.clear_amount)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = tipState::onPersonRemove, modifier = Modifier.weight(1f)) {
                Text("-")
            }
            OutlinedTextField(
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text(stringResource(R.string.persons)) },
                value = tipState.persons.toString(),
                onValueChange = tipState::onPersonsChange,
                modifier = Modifier.weight(3f)
            )
            TextButton(onClick = tipState::onPersonAdd, modifier = Modifier.weight(1f)) {
                Text("+")
            }
        }
        SubHeading(stringResource(R.string.country_and_tip_rate))
        CountryMenu(
            selected = tipState.selectedCountry,
            onChange = tipState::onSelectedCountryChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = tipState.percentage,
                onValueChange = tipState::onPercentageChange,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = tipState.tipPercentage.formatted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            TipResult(
                heading = stringResource(R.string.tip_amount),
                value = tipState.calculation.tip.currencyFormatted(countryCode),
                modifier = Modifier.weight(1f),
            )
            TipResult(
                heading = stringResource(R.string.total_amount),
                value = tipState.calculation.total.currencyFormatted(countryCode),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TipResult(
            heading = stringResource(R.string.total_per_person),
            value = tipState.calculation.totalPerPerson.currencyFormatted(countryCode)
        )
    }
}

@Composable
fun SubHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.height(48.dp).wrapContentHeight(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryMenu(selected: Country, onChange: (Country) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            label = { Text("Country") },
            value = selected.name.value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (country in Countries) {
                DropdownMenuItem(
                    text = { Text(country.name.value) },
                    onClick = {
                        onChange(country)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TipResult(heading: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SubHeading(heading)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun TipScreenPreview() {
    TipeeTheme { TipScreen() }
}
