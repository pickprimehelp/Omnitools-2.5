package com.example.ui.tools.business

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstCalculatorScreen(
    initialMode: Int = 0, // 0 = GST, 1 = Income Tax Estimator
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }

    // GST State
    var amountText by remember { mutableStateOf("1000") }
    var gstRate by remember { mutableDoubleStateOf(18.0) }
    var isInclusive by remember { mutableStateOf(false) }

    // Income Tax State (FY 25-26 New Regime)
    var annualIncomeText by remember { mutableStateOf("1400000") }
    var stdDeductionText by remember { mutableStateOf("75000") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val netPrice = if (isInclusive) amount / (1.0 + gstRate / 100.0) else amount
    val gstAmount = if (isInclusive) amount - netPrice else amount * (gstRate / 100.0)
    val totalAmount = if (isInclusive) amount else amount + gstAmount
    val halfGst = gstAmount / 2.0

    val grossIncome = annualIncomeText.toDoubleOrNull() ?: 0.0
    val stdDed = stdDeductionText.toDoubleOrNull() ?: 75000.0
    val taxableIncome = (grossIncome - stdDed).coerceAtLeast(0.0)
    val estIncomeTax = computeNewRegimeTax(taxableIncome)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (activeTab == 0) "GST Calculator Pro" else "Income Tax Estimator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("GST (CGST/SGST)") })
                    FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("Income Tax Slab") })
                }
            }

            if (activeTab == 0) {
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Base / Final Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !isInclusive, onClick = { isInclusive = false }, label = { Text("Add GST (Exclusive)") })
                        FilterChip(selected = isInclusive, onClick = { isInclusive = true }, label = { Text("Remove GST (Inclusive)") })
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(5) { idx ->
                            val slab = listOf(3.0, 5.0, 12.0, 18.0, 28.0)[idx]
                            FilterChip(selected = gstRate == slab, onClick = { gstRate = slab }, label = { Text("${slab.toInt()}%") })
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Net Amount: ₹${String.format(Locale.US, "%.2f", netPrice)}")
                            Text("CGST (${gstRate / 2}%): ₹${String.format(Locale.US, "%.2f", halfGst)}")
                            Text("SGST (${gstRate / 2}%): ₹${String.format(Locale.US, "%.2f", halfGst)}")
                            Text("Total GST / IGST: ₹${String.format(Locale.US, "%.2f", gstAmount)}")
                            Text(
                                "Final Amount: ₹${String.format(Locale.US, "%.2f", totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = annualIncomeText,
                            onValueChange = { annualIncomeText = it.filter { c -> c.isDigit() } },
                            label = { Text("Annual Income (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = stdDeductionText,
                            onValueChange = { stdDeductionText = it.filter { c -> c.isDigit() } },
                            label = { Text("Std Deduction") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Taxable Income: ₹${String.format(Locale.US, "%.0f", taxableIncome)}")
                            Text(
                                "Estimated Tax (+4% Cess): ₹${String.format(Locale.US, "%.0f", estIncomeTax)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Monthly Tax Deduction: ₹${String.format(Locale.US, "%.0f", estIncomeTax / 12.0)}")
                            Text(
                                "Net Annual Take-Home: ₹${String.format(Locale.US, "%.0f", (grossIncome - estIncomeTax).coerceAtLeast(0.0))}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun computeNewRegimeTax(taxable: Double): Double {
    if (taxable <= 1200000.0) return 0.0 // Rebate up to 12L under updated new regime
    var tax = 0.0
    val slabs = listOf(
        400000.0 to 0.0,
        400000.0 to 0.05,
        400000.0 to 0.10,
        400000.0 to 0.15,
        400000.0 to 0.20,
        400000.0 to 0.25
    )
    var rem = taxable
    for ((band, rate) in slabs) {
        val slice = minOf(rem, band)
        tax += slice * rate
        rem -= slice
        if (rem <= 0.0) break
    }
    if (rem > 0.0) tax += rem * 0.30
    return tax * 1.04 // + 4% Health & Education Cess
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    initialMode: Int = 0, // 0 = EMI, 1 = Loan Compare, 2 = Salary In-Hand
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }

    var principalText by remember { mutableStateOf("500000") }
    var rateText by remember { mutableStateOf("9.5") }
    var monthsText by remember { mutableStateOf("36") }
    var compareRateText by remember { mutableStateOf("8.75") }

    var ctcText by remember { mutableStateOf("1200000") }
    var pfPercentText by remember { mutableStateOf("12") }
    var profTaxText by remember { mutableStateOf("200") }

    val principal = principalText.toDoubleOrNull() ?: 0.0
    val annualRate = rateText.toDoubleOrNull() ?: 0.0
    val months = (monthsText.toIntOrNull() ?: 1).coerceAtLeast(1)
    val emi = calcEmi(principal, annualRate, months)
    val totalPayable = emi * months
    val totalInterest = (totalPayable - principal).coerceAtLeast(0.0)

    val rateB = compareRateText.toDoubleOrNull() ?: 0.0
    val emiB = calcEmi(principal, rateB, months)
    val totalB = emiB * months

    val ctc = ctcText.toDoubleOrNull() ?: 0.0
    val monthlyGross = ctc / 12.0
    val pfPct = pfPercentText.toDoubleOrNull() ?: 12.0
    val monthlyPf = (monthlyGross * 0.5) * (pfPct / 100.0) // PF on 50% basic
    val pt = profTaxText.toDoubleOrNull() ?: 200.0
    val monthlyInHand = (monthlyGross - monthlyPf - pt).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (activeTab) {
                            1 -> "Loan Comparison Studio"
                            2 -> "Salary In-Hand Calculator"
                            else -> "EMI & Amortization Calc"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("EMI Calculator") }) }
                    item { FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("Loan Compare") }) }
                    item { FilterChip(selected = activeTab == 2, onClick = { activeTab = 2 }, label = { Text("Salary In-Hand") }) }
                }
            }

            if (activeTab == 0 || activeTab == 1) {
                item {
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { principalText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Loan Principal (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(if (activeTab == 1) "Bank A Rate %" else "Interest % (p.a.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (activeTab == 1) {
                            OutlinedTextField(
                                value = compareRateText,
                                onValueChange = { compareRateText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Bank B Rate %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = monthsText,
                            onValueChange = { monthsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Months") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Monthly EMI: ₹${String.format(Locale.US, "%.2f", emi)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Principal: ₹${String.format(Locale.US, "%.0f", principal)} • Total Interest: ₹${String.format(Locale.US, "%.0f", totalInterest)}")
                            Text("Total Payable: ₹${String.format(Locale.US, "%.0f", totalPayable)}", fontWeight = FontWeight.SemiBold)

                            val principalRatio = if (totalPayable > 0.0) (principal / totalPayable).toFloat().coerceIn(0.05f, 0.95f) else 0.7f
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Principal vs Interest Ratio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Box(modifier = Modifier.weight(principalRatio).height(12.dp).background(Color(0xFF10B981)))
                                Box(modifier = Modifier.weight(1f - principalRatio).height(12.dp).background(Color(0xFFF43F5E)))
                            }

                            if (activeTab == 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Bank B EMI: ₹${String.format(Locale.US, "%.2f", emiB)} (Difference: ₹${String.format(Locale.US, "%.0f", totalPayable - totalB)} total)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = ctcText,
                        onValueChange = { ctcText = it.filter { c -> c.isDigit() } },
                        label = { Text("Annual Gross CTC (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = pfPercentText,
                            onValueChange = { pfPercentText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("EPF % of Basic") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = profTaxText,
                            onValueChange = { profTaxText = it.filter { c -> c.isDigit() } },
                            label = { Text("Prof. Tax / mo (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Monthly Gross: ₹${String.format(Locale.US, "%.0f", monthlyGross)}")
                            Text("Monthly EPF Deduction: -₹${String.format(Locale.US, "%.0f", monthlyPf)}")
                            Text("Professional Tax: -₹${String.format(Locale.US, "%.0f", pt)}")
                            Text(
                                "Estimated In-Hand / Month: ₹${String.format(Locale.US, "%.0f", monthlyInHand)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calcEmi(principal: Double, annualRate: Double, months: Int): Double {
    val r = annualRate / 12.0 / 100.0
    return if (r > 0.0) {
        val factor = (1.0 + r).pow(months)
        (principal * r * factor) / (factor - 1.0)
    } else {
        principal / months
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitDiscountCalculatorScreen(
    initialMode: Int = 0, // 0 = Profit, 1 = Discount, 2 = Percentage, 3 = Tip Split
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }

    var costText by remember { mutableStateOf("1000") }
    var sellText by remember { mutableStateOf("1350") }
    var percentText by remember { mutableStateOf("20") }
    var peopleText by remember { mutableStateOf("4") }

    val a = costText.toDoubleOrNull() ?: 0.0
    val b = sellText.toDoubleOrNull() ?: 0.0
    val pct = percentText.toDoubleOrNull() ?: 0.0
    val people = (peopleText.toIntOrNull() ?: 1).coerceAtLeast(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (activeTab) {
                            1 -> "Discount & Savings Calc"
                            2 -> "4-in-1 Percentage Tool"
                            3 -> "Tip & Bill Split Tool"
                            else -> "Profit Margin Calculator"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("Profit Margin") }) }
                    item { FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("Discount") }) }
                    item { FilterChip(selected = activeTab == 2, onClick = { activeTab = 2 }, label = { Text("Percentage") }) }
                    item { FilterChip(selected = activeTab == 3, onClick = { activeTab = 3 }, label = { Text("Tip & Split") }) }
                }
            }

            when (activeTab) {
                0 -> {
                    val profit = b - a
                    val margin = if (b > 0.0) (profit / b) * 100.0 else 0.0
                    val markup = if (a > 0.0) (profit / a) * 100.0 else 0.0
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Cost Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sellText,
                                onValueChange = { sellText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Selling Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        ResultCard(
                            headline = "Net Profit: ₹${String.format(Locale.US, "%.2f", profit)}",
                            lines = listOf(
                                "Profit Margin: ${String.format(Locale.US, "%.2f", margin)}%",
                                "Markup %: ${String.format(Locale.US, "%.2f", markup)}%"
                            )
                        )
                    }
                }
                1 -> {
                    val savings = a * (pct / 100.0)
                    val finalP = (a - savings).coerceAtLeast(0.0)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("MRP Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = percentText,
                                onValueChange = { percentText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Discount (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        ResultCard(
                            headline = "Payable Price: ₹${String.format(Locale.US, "%.2f", finalP)}",
                            lines = listOf("You Save: ₹${String.format(Locale.US, "%.2f", savings)} ($pct% OFF)")
                        )
                    }
                }
                2 -> {
                    val xPctOfY = (pct / 100.0) * a
                    val xIsWhatPctOfY = if (b != 0.0) (a / b) * 100.0 else 0.0
                    val pctChange = if (a != 0.0) ((b - a) / a) * 100.0 else 0.0
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = percentText,
                                onValueChange = { percentText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("X %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Value A") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sellText,
                                onValueChange = { sellText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Value B") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        ResultCard(
                            headline = "$pct% of $a = ${String.format(Locale.US, "%.2f", xPctOfY)}",
                            lines = listOf(
                                "$a is ${String.format(Locale.US, "%.2f", xIsWhatPctOfY)}% of $b",
                                "Change ($a → $b): ${String.format(Locale.US, "%+.2f", pctChange)}%"
                            )
                        )
                    }
                }
                else -> {
                    val tipAmt = a * (pct / 100.0)
                    val totalBill = a + tipAmt
                    val perPerson = totalBill / people
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Total Bill (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = percentText,
                                onValueChange = { percentText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Tip %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                            OutlinedTextField(
                                value = peopleText,
                                onValueChange = { peopleText = it.filter { c -> c.isDigit() } },
                                label = { Text("People") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                    item {
                        ResultCard(
                            headline = "Per Person: ₹${String.format(Locale.US, "%.2f", perPerson)}",
                            lines = listOf(
                                "Tip Amount ($pct%): ₹${String.format(Locale.US, "%.2f", tipAmt)}",
                                "Grand Bill: ₹${String.format(Locale.US, "%.2f", totalBill)}"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(headline: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = headline,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            lines.forEach { line -> Text(line, fontSize = 14.sp) }
        }
    }
}
