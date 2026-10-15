package com.example.ui.screening

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.clinical.ClinicalAssessmentEngine
import com.example.data.local.ChildEntity
import com.example.model.ClinicalAssessment
import com.example.model.EdemaGrade
import com.example.model.LocalizedStrings
import com.example.model.MalnutritionCategory
import com.example.ui.camera.CameraGuidanceView
import com.example.ui.components.MalnutritionBadge
import com.example.ui.components.MuacTapeMeter
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberContainer
import com.example.ui.theme.MedicalAmberDark
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenContainer
import com.example.ui.theme.MedicalGreenDark
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedContainer
import com.example.ui.theme.MedicalRedDark
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewScreeningWizard(
    strings: LocalizedStrings,
    children: List<ChildEntity>,
    preselectedChildId: String? = null,
    onNavigateToRegisterChild: () -> Unit,
    onScreeningFinished: () -> Unit,
    onSaveScreening: (
        childId: String,
        childFullName: String,
        category: String,
        hasEdema: Boolean,
        edemaGrade: String,
        muacCm: Double,
        weightKg: Double?,
        heightCm: Double?,
        dangerSignsPresent: Boolean,
        dangerSignsNotes: String,
        actionTaken: String,
        primaryReason: String,
        followUpDate: Long?,
        imageVerdict: String,
        manualOverride: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // 7 Wizard steps (1 to 7)
    var currentStep by remember { mutableIntStateOf(if (preselectedChildId != null) 2 else 1) }

    // Step 1: Selected child
    var selectedChild by remember {
        mutableStateOf(children.find { it.id == preselectedChildId })
    }
    var childSearchQuery by remember { mutableStateOf("") }

    // Step 2: Consent
    var caregiverConsentGiven by remember { mutableStateOf(true) }

    // Step 3: Anthropometrics
    var muacInput by remember { mutableStateOf("12.0") }
    var selectedEdema by remember { mutableStateOf(EdemaGrade.NONE) }
    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var hasDangerSigns by remember { mutableStateOf(false) }
    var dangerSignsNotes by remember { mutableStateOf("") }

    // Step 4: Camera QA
    var imageQualityVerdict by remember { mutableStateOf("SKIPPED") }
    var cameraBypassed by remember { mutableStateOf(false) }

    // Step 5 & 6: Assessment results
    var showReferralDialog by remember { mutableStateOf(false) }

    // Step 7: Finalize & Follow-up
    var followUpDaysInterval by remember { mutableIntStateOf(14) }
    var fieldNotes by remember { mutableStateOf("") }

    val filteredChildren = remember(childSearchQuery, children) {
        if (childSearchQuery.isBlank()) children
        else children.filter {
            it.firstName.contains(childSearchQuery, ignoreCase = true) ||
            it.lastName.contains(childSearchQuery, ignoreCase = true) ||
            it.localId.contains(childSearchQuery, ignoreCase = true)
        }
    }

    val muacValidation = remember(muacInput) {
        ClinicalAssessmentEngine.validateMuacInput(muacInput)
    }

    val parsedMuac = remember(muacInput) {
        muacInput.toDoubleOrNull() ?: 12.0
    }

    val assessment: ClinicalAssessment = remember(parsedMuac, selectedEdema, hasDangerSigns, selectedChild) {
        ClinicalAssessmentEngine.evaluateScreening(
            muacCm = parsedMuac,
            edemaGrade = selectedEdema,
            hasDangerSigns = hasDangerSigns,
            ageMonths = selectedChild?.estimatedAgeMonths ?: 24
        )
    }

    // Back handling within multi-step wizard
    BackHandler(enabled = currentStep > 1) {
        currentStep -= 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Step indicator bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentStep > 1) {
                        IconButton(
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STEP $currentStep OF 7",
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> strings.stepChildSelection
                                2 -> strings.stepConsent
                                3 -> strings.stepMeasurements
                                4 -> strings.stepCameraQa
                                5, 6 -> strings.stepResults
                                7 -> strings.stepFinalize
                                else -> ""
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Step progress dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..7) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            i == currentStep -> TealPrimary
                                            i < currentStep -> MedicalGreen
                                            else -> SlateBorder
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Wizard Step Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                // STEP 1: Select Child
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = childSearchQuery,
                            onValueChange = { childSearchQuery = it },
                            placeholder = { Text(strings.searchChildren) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_child_screening"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToRegisterChild,
                            colors = ButtonDefaults.buttonColors(containerColor = TealContainer, contentColor = TealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.registerChild, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Registered Children (${filteredChildren.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredChildren) { child ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedChild = child
                                            currentStep = 2
                                        }
                                        .border(
                                            width = if (selectedChild?.id == child.id) 2.dp else 1.dp,
                                            color = if (selectedChild?.id == child.id) TealPrimary else SlateBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(TealContainer)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = TealPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${child.firstName} ${child.lastName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "${child.localId} • ${child.estimatedAgeMonths} months • ${child.sex}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Caregiver: ${child.caregiverName} • ${child.kebele}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        MalnutritionBadge(category = child.latestCategory)
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 2: Confirm Information & Consent
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Selected Child Information",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TealPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Name", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${selectedChild?.firstName} ${selectedChild?.lastName}", fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(selectedChild?.localId ?: "--", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Age / Sex", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${selectedChild?.estimatedAgeMonths} months • ${selectedChild?.sex}", fontWeight = FontWeight.Medium)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Location", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${selectedChild?.woreda}, ${selectedChild?.kebele}", fontWeight = FontWeight.Medium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Caregiver", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${selectedChild?.caregiverName} (${selectedChild?.caregiverPhone?.ifBlank { "No phone" }})", fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Caregiver Consent Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TealContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = TealPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.caregiverConsent,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.consentAffirmed,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .clickable { caregiverConsentGiven = !caregiverConsentGiven }
                                        .padding(8.dp)
                                ) {
                                    Checkbox(
                                        checked = caregiverConsentGiven,
                                        onCheckedChange = { caregiverConsentGiven = it },
                                        modifier = Modifier.testTag("consent_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Verbal consent affirmed by caregiver",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { currentStep = 3 },
                            enabled = caregiverConsentGiven,
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("proceed_to_measurements_button")
                        ) {
                            Text(strings.next, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }

                // STEP 3: Record Anthropometrics
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // MUAC Input Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = strings.muacLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.muacHelp,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = muacInput,
                                    onValueChange = { muacInput = it },
                                    label = { Text("MUAC in centimeters") },
                                    suffix = { Text("cm", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    isError = !muacValidation.isValid,
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("muac_input_field"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                if (muacValidation.warningMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = muacValidation.warningMessage!!,
                                        color = MedicalAmberDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Visual tape meter gauge
                                MuacTapeMeter(currentValueCm = parsedMuac)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bilateral Pitting Edema Selector
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = strings.edemaLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Press firmly with both thumbs for 3 seconds on tops of feet.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                EdemaGrade.values().forEach { grade ->
                                    val isSelected = selectedEdema == grade
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) TealContainer else Color(0xFFF8FAFC))
                                            .border(
                                                1.dp,
                                                if (isSelected) TealPrimary else SlateBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedEdema = grade }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (isSelected) TealPrimary else SlateBorder, CircleShape)
                                                .background(if (isSelected) TealPrimary else Color.Transparent)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = grade.description,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (grade != EdemaGrade.NONE) MedicalRedDark else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Optional Height & Weight
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Secondary Anthropometrics (Optional)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text("Weight") },
                                        suffix = { Text("kg") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = { heightInput = it },
                                        label = { Text("Height") },
                                        suffix = { Text("cm") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Danger signs check
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (hasDangerSigns) MedicalRedContainer else Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (hasDangerSigns) MedicalRed else SlateBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { hasDangerSigns = !hasDangerSigns }
                                ) {
                                    Checkbox(checked = hasDangerSigns, onCheckedChange = { hasDangerSigns = it })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = strings.dangerSigns,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasDangerSigns) MedicalRedDark else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = strings.dangerSignsPrompt,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { currentStep = 4 },
                            enabled = muacValidation.isValid,
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("proceed_to_camera_button")
                        ) {
                            Text("Next: Camera Positioning QA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }

                // STEP 4: Camera Positioning QA
                4 -> {
                    CameraGuidanceView(
                        strings = strings,
                        onQualityConfirmed = { verdict ->
                            imageQualityVerdict = verdict
                            cameraBypassed = false
                            currentStep = 5
                        },
                        onSkipManual = {
                            imageQualityVerdict = "SKIPPED"
                            cameraBypassed = true
                            currentStep = 5
                        }
                    )
                }

                // STEP 5 & 6: Assessment Results & Recommendations
                5, 6 -> {
                    val isSam = assessment.category == MalnutritionCategory.SAM
                    val isMam = assessment.category == MalnutritionCategory.MAM

                    val categoryBg = when {
                        isSam -> MedicalRedContainer
                        isMam -> MedicalAmberContainer
                        else -> MedicalGreenContainer
                    }
                    val categoryBorder = when {
                        isSam -> MedicalRed
                        isMam -> MedicalAmber
                        else -> MedicalGreen
                    }
                    val categoryText = when {
                        isSam -> MedicalRedDark
                        isMam -> MedicalAmberDark
                        else -> MedicalGreenDark
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // High impact triage banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = categoryBg),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, categoryBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSam) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = categoryText,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = when (assessment.category) {
                                            MalnutritionCategory.SAM -> strings.categorySam
                                            MalnutritionCategory.MAM -> strings.categoryMam
                                            MalnutritionCategory.NORMAL -> strings.categoryNormal
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = categoryText
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = assessment.primaryReason,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = categoryText
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = categoryBorder.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "ACTION REQUIRED:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = categoryText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = assessment.actionPlan,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = categoryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Screening metrics summary
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Recorded Measurements Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("MUAC", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${assessment.muacCm} cm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Bilateral Edema", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(assessment.edemaGrade.description, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (weightInput.isNotBlank() || heightInput.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Weight", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(if (weightInput.isNotBlank()) "$weightInput kg" else "--", fontWeight = FontWeight.Medium)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Height", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(if (heightInput.isNotBlank()) "$heightInput cm" else "--", fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        // Referral Slip Generation Button for SAM or complicated cases
                        if (assessment.requiresUrgentReferral) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showReferralDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Digital Referral Slip", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Medical Disclaimer Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.clinicalDisclaimer,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                followUpDaysInterval = assessment.recommendedFollowUpDays
                                currentStep = 7
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Next: Schedule Follow-Up & Finalize", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }

                // STEP 7: Finalize & Schedule Follow-Up
                7 -> {
                    val now = System.currentTimeMillis()
                    val calculatedFollowUpDate = remember(followUpDaysInterval) {
                        now + (followUpDaysInterval * 24 * 60 * 60 * 1000L)
                    }
                    val dateFormatted = remember(calculatedFollowUpDate) {
                        SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date(calculatedFollowUpDate))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(strings.scheduleFollowUp, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(strings.followUpIntervalHelp, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(7 to "7 Days (SAM)", 14 to "14 Days (MAM)", 30 to "30 Days (Normal)").forEach { (days, label) ->
                                        FilterChip(
                                            selected = followUpDaysInterval == days,
                                            onClick = { followUpDaysInterval = days },
                                            label = { Text(label, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TealContainer.copy(alpha = 0.5f))
                                        .padding(12.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = TealPrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Next Screening Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(dateFormatted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = fieldNotes,
                                    onValueChange = { fieldNotes = it },
                                    label = { Text("Health Extension Worker Outreach Notes") },
                                    placeholder = { Text("Enter counseling provided, ration batch, or caregiver feedback...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    minLines = 3
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val child = selectedChild ?: return@Button
                                onSaveScreening(
                                    child.id,
                                    "${child.firstName} ${child.lastName}",
                                    assessment.category.code,
                                    selectedEdema != EdemaGrade.NONE,
                                    selectedEdema.code,
                                    parsedMuac,
                                    weightInput.toDoubleOrNull(),
                                    heightInput.toDoubleOrNull(),
                                    hasDangerSigns,
                                    dangerSignsNotes,
                                    assessment.actionPlan,
                                    assessment.primaryReason,
                                    calculatedFollowUpDate,
                                    imageQualityVerdict,
                                    cameraBypassed
                                )
                                onScreeningFinished()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("save_and_complete_screening_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.saveAndComplete, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // Referral Slip Dialog Modal
    if (showReferralDialog) {
        val child = selectedChild
        AlertDialog(
            onDismissRequest = { showReferralDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MedicalRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.referralSlipTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Referring Post: ${child?.kebele} Health Post", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Target: Woreda Primary Hospital / OTP", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Child: ${child?.firstName} ${child?.lastName} (${child?.localId})", fontWeight = FontWeight.Bold)
                    Text("Age: ${child?.estimatedAgeMonths} months | Sex: ${child?.sex}")
                    Text("Caregiver: ${child?.caregiverName} | Tel: ${child?.caregiverPhone}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Clinical Findings:", fontWeight = FontWeight.Bold)
                    Text("• MUAC: ${assessment.muacCm} cm (${assessment.category.labelEn})")
                    Text("• Bilateral Edema: ${assessment.edemaGrade.description}")
                    if (hasDangerSigns) {
                        Text("• Clinical Danger Signs Observed: YES", color = MedicalRedDark, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recommended Care: OTP Admission & Medical Assessment.", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReferralDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(strings.close)
                }
            }
        )
    }
}
