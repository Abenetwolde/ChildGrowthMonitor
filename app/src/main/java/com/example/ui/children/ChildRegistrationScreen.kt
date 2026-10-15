package com.example.ui.children

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalizedStrings
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRegistrationScreen(
    strings: LocalizedStrings,
    onBack: () -> Unit,
    onChildRegistered: (childId: String, startScreeningImmediately: Boolean) -> Unit,
    onSaveChild: (
        firstName: String,
        lastName: String,
        sex: String,
        dateOfBirth: String?,
        estimatedAgeMonths: Int,
        caregiverName: String,
        caregiverPhone: String,
        region: String,
        zone: String,
        woreda: String,
        kebele: String,
        gotCommunity: String,
        isDraft: Boolean,
        startScreeningImmediately: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("MALE") }
    var ageMonthsInput by remember { mutableStateOf("18") }
    var caregiverName by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }

    val ethiopianRegions = listOf("Oromia", "Amhara", "Tigray", "Sidama", "Somali", "SNNP", "Addis Ababa")
    var selectedRegion by remember { mutableStateOf(ethiopianRegions[0]) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    var zone by remember { mutableStateOf("East Shewa") }
    var woreda by remember { mutableStateOf("Ada'a") }
    var kebele by remember { mutableStateOf("Kebele 03 (Dukam)") }
    var gotCommunity by remember { mutableStateOf("Got 01") }

    val isFormValid = firstName.isNotBlank() && lastName.isNotBlank() && caregiverName.isNotBlank() &&
        (ageMonthsInput.toIntOrNull() != null && ageMonthsInput.toInt() > 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.registerChild,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Child Demographics Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Child Demographics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(strings.firstName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("child_first_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(strings.lastName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("child_last_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sex selector
                    Text(strings.sex, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("MALE" to strings.male, "FEMALE" to strings.female).forEach { (code, label) ->
                            val isSelected = sex == code
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) TealContainer else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isSelected) TealPrimary else SlateBorder, RoundedCornerShape(8.dp))
                                    .clickable { sex = code }
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ageMonthsInput,
                        onValueChange = { ageMonthsInput = it },
                        label = { Text(strings.ageMonths) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("child_age_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Caregiver Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Caregiver Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = caregiverName,
                        onValueChange = { caregiverName = it },
                        label = { Text(strings.caregiverName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caregiver_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = caregiverPhone,
                        onValueChange = { caregiverPhone = it },
                        label = { Text(strings.caregiverPhone) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caregiver_phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Administrative Geography Card (Ethiopian Structure)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Administrative Location", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Region Selector
                    Box {
                        OutlinedTextField(
                            value = selectedRegion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.region) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { regionDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = regionDropdownExpanded,
                            onDismissRequest = { regionDropdownExpanded = false }
                        ) {
                            ethiopianRegions.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        selectedRegion = r
                                        regionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = zone,
                            onValueChange = { zone = it },
                            label = { Text(strings.zone) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = woreda,
                            onValueChange = { woreda = it },
                            label = { Text(strings.woreda) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = kebele,
                            onValueChange = { kebele = it },
                            label = { Text(strings.kebele) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = gotCommunity,
                            onValueChange = { gotCommunity = it },
                            label = { Text(strings.gotCommunity) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    onSaveChild(
                        firstName,
                        lastName,
                        sex,
                        null,
                        ageMonthsInput.toIntOrNull() ?: 18,
                        caregiverName,
                        caregiverPhone,
                        selectedRegion,
                        zone,
                        woreda,
                        kebele,
                        gotCommunity,
                        false, // isDraft
                        true // startScreeningImmediately
                    )
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_child_registration_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register & Begin Screening", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    onSaveChild(
                        firstName.ifBlank { "Draft Child" },
                        lastName.ifBlank { "Draft" },
                        sex,
                        null,
                        ageMonthsInput.toIntOrNull() ?: 18,
                        caregiverName.ifBlank { "Draft Caregiver" },
                        caregiverPhone,
                        selectedRegion,
                        zone,
                        woreda,
                        kebele,
                        gotCommunity,
                        true, // isDraft
                        false
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.saveDraft)
            }
        }
    }
}
