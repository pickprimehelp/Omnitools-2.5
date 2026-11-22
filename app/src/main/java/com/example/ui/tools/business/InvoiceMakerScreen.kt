package com.example.ui.tools.business

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.util.ImageExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvoiceItem(
    var name: String,
    var hsnCode: String = "",
    var qty: Double,
    var rate: Double,
    var discountPercent: Double = 0.0,
    var gstRate: Double = 18.0
) {
    val grossAmount: Double get() = qty * rate
    val discountAmount: Double get() = grossAmount * (discountPercent / 100.0)
    val totalTaxable: Double get() = (grossAmount - discountAmount).coerceAtLeast(0.0)
    val taxAmount: Double get() = totalTaxable * (gstRate / 100.0)
    val totalAmount: Double get() = totalTaxable + taxAmount
}

enum class GstTaxMode(val label: String) {
    CGST_SGST("CGST + SGST"),
    IGST("IGST (Inter-State)"),
    NO_GST("No GST Bill")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceBillMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val todayStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    var invoiceNumber by remember { mutableStateOf("INV-2026-001") }
    var invoiceDate by remember { mutableStateOf(todayStr) }
    var businessName by remember { mutableStateOf("OmniTools Enterprises") }
    var sellerGstin by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var customerGstin by remember { mutableStateOf("") }
    var upiIdOrNotes by remember { mutableStateOf("Thank you for your business!") }
    var taxMode by remember { mutableStateOf(GstTaxMode.CGST_SGST) }
    var businessLogo by remember { mutableStateOf<Bitmap?>(null) }

    var itemName by remember { mutableStateOf("") }
    var itemHsn by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemRate by remember { mutableStateOf("") }
    var itemDiscount by remember { mutableStateOf("0") }
    var itemGst by remember { mutableDoubleStateOf(18.0) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    val items = remember {
        mutableStateListOf(
            InvoiceItem("Consulting / Product", "998314", 1.0, 1000.0, 0.0, 18.0)
        )
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = try {
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                } catch (_: Exception) {
                    null
                }
                withContext(Dispatchers.Main) { businessLogo = bmp }
            }
        }
    }

    val effectiveItems = items.map {
        if (taxMode == GstTaxMode.NO_GST) it.copy(gstRate = 0.0) else it
    }
    val grandTaxable = effectiveItems.sumOf { it.totalTaxable }
    val grandTax = effectiveItems.sumOf { it.taxAmount }
    val grandTotal = effectiveItems.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GST Invoice & Bill Maker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Seller & Buyer GSTIN, Logo & PDF Export", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("1. Seller & GSTIN Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = { logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (businessLogo == null) "Add Logo" else "Logo Set", fontSize = 11.sp)
                            }
                            if (businessLogo != null) {
                                IconButton(onClick = { businessLogo = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove Logo", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        businessLogo?.let { logo ->
                            Image(
                                bitmap = logo.asImageBitmap(),
                                contentDescription = "Business Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Your Business / Shop Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = sellerGstin,
                                onValueChange = { sellerGstin = it.uppercase(Locale.getDefault()).take(15) },
                                label = { Text("Your GSTIN Number") },
                                placeholder = { Text("22AAAAA0000A1Z5", fontSize = 12.sp) },
                                supportingText = {
                                    if (sellerGstin.isNotEmpty() && sellerGstin.length != 15) {
                                        Text("${sellerGstin.length}/15 chars", fontSize = 10.sp)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("seller_gstin_input")
                            )
                            OutlinedTextField(
                                value = businessPhone,
                                onValueChange = { businessPhone = it },
                                label = { Text("Phone / City") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("Invoice No.") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = invoiceDate,
                                onValueChange = { invoiceDate = it },
                                label = { Text("Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("2. Customer & Tax Type", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer / Party Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customerGstin,
                                onValueChange = { customerGstin = it.uppercase(Locale.getDefault()).take(15) },
                                label = { Text("Customer GSTIN") },
                                placeholder = { Text("Optional B2B", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("customer_gstin_input")
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GstTaxMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = taxMode == mode,
                                    onClick = { taxMode = mode },
                                    label = { Text(mode.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("3. Add Product / Service Line", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = itemName,
                                onValueChange = { itemName = it },
                                label = { Text("Item Description") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                            OutlinedTextField(
                                value = itemHsn,
                                onValueChange = { itemHsn = it.filter { c -> c.isDigit() }.take(8) },
                                label = { Text("HSN/SAC") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = itemQty,
                                onValueChange = { itemQty = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                            OutlinedTextField(
                                value = itemRate,
                                onValueChange = { itemRate = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Rate ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = itemDiscount,
                                onValueChange = { itemDiscount = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Disc %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                        if (taxMode != GstTaxMode.NO_GST) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 12.0, 18.0, 28.0).forEach { slab ->
                                    FilterChip(
                                        selected = itemGst == slab,
                                        onClick = { itemGst = slab },
                                        label = { Text("${slab.toInt()}%", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                val qty = (itemQty.toDoubleOrNull() ?: 1.0).coerceAtLeast(0.01)
                                val rate = itemRate.toDoubleOrNull() ?: 0.0
                                val disc = (itemDiscount.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 95.0)
                                if (itemName.isNotBlank() && rate > 0.0) {
                                    items.add(InvoiceItem(itemName.trim(), itemHsn.trim(), qty, rate, disc, itemGst))
                                    itemName = ""
                                    itemHsn = ""
                                    itemRate = ""
                                    itemDiscount = "0"
                                    generatedPdfFile = null
                                } else {
                                    Toast.makeText(context, "Enter item name and rate", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Line Item", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            itemsIndexed(items) { index, item ->
                val activeGst = if (taxMode == GstTaxMode.NO_GST) 0.0 else item.gstRate
                val itemTotal = item.totalTaxable * (1.0 + activeGst / 100.0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (item.hsnCode.isNotBlank()) "${item.name} (HSN: ${item.hsnCode})" else item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${item.qty} × ₹${item.rate}" +
                                    (if (item.discountPercent > 0) " - ${item.discountPercent}%" else "") +
                                    (if (taxMode != GstTaxMode.NO_GST) " + ${activeGst.toInt()}% GST" else ""),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", itemTotal)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            items.removeAt(index)
                            generatedPdfFile = null
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Item")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = upiIdOrNotes,
                    onValueChange = { upiIdOrNotes = it },
                    label = { Text("UPI ID / Payment Terms / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Invoice Summary ($invoiceNumber)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        HorizontalDivider()
                        Text("Taxable Subtotal: ₹${String.format(Locale.US, "%.2f", grandTaxable)}")
                        when (taxMode) {
                            GstTaxMode.CGST_SGST -> {
                                Text("CGST: ₹${String.format(Locale.US, "%.2f", grandTax / 2.0)}")
                                Text("SGST: ₹${String.format(Locale.US, "%.2f", grandTax / 2.0)}")
                            }
                            GstTaxMode.IGST -> {
                                Text("IGST: ₹${String.format(Locale.US, "%.2f", grandTax)}")
                            }
                            GstTaxMode.NO_GST -> Unit
                        }
                        Text(
                            "Grand Total: ₹${String.format(Locale.US, "%.2f", grandTotal)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (items.isEmpty()) return@Button
                                    val file = generateInvoicePdf(
                                        context = context,
                                        invoiceNumber = invoiceNumber,
                                        invoiceDate = invoiceDate,
                                        businessName = businessName,
                                        sellerGstin = sellerGstin,
                                        businessPhone = businessPhone,
                                        customerName = customerName,
                                        customerGstin = customerGstin,
                                        taxMode = taxMode,
                                        items = effectiveItems,
                                        notes = upiIdOrNotes,
                                        logo = businessLogo
                                    )
                                    if (file != null) {
                                        generatedPdfFile = file
                                        Toast.makeText(context, "GST Invoice PDF Ready!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("create_invoice_pdf"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create PDF", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val sb = buildInvoiceTextSummary(
                                        invoiceNumber, invoiceDate, businessName, sellerGstin,
                                        customerName, customerGstin, taxMode, effectiveItems,
                                        grandTaxable, grandTax, grandTotal, upiIdOrNotes
                                    )
                                    ImageExportUtils.shareText(context, sb, "Share Invoice")
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Text")
                            }
                        }
                    }
                }
            }

            generatedPdfFile?.let { pdf ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${pdf.name} Ready (${pdf.length() / 1024} KB)", fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { ImageExportUtils.sharePdfFile(context, pdf, "Share Tax Invoice") },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share PDF")
                                }
                                OutlinedButton(
                                    onClick = { ImageExportUtils.openPdfFile(context, pdf) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Open PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildInvoiceTextSummary(
    invoiceNumber: String,
    invoiceDate: String,
    businessName: String,
    sellerGstin: String,
    customerName: String,
    customerGstin: String,
    taxMode: GstTaxMode,
    items: List<InvoiceItem>,
    grandTaxable: Double,
    grandTax: Double,
    grandTotal: Double,
    notes: String
): String {
    val sb = StringBuilder()
    sb.appendLine(if (taxMode == GstTaxMode.NO_GST) "BILL / INVOICE" else "TAX INVOICE")
    sb.appendLine("$businessName | $invoiceNumber ($invoiceDate)")
    if (sellerGstin.isNotBlank()) sb.appendLine("Seller GSTIN: $sellerGstin")
    sb.appendLine("Billed To: $customerName")
    if (customerGstin.isNotBlank()) sb.appendLine("Customer GSTIN: $customerGstin")
    sb.appendLine("-------------------------")
    items.forEachIndexed { idx, it ->
        sb.appendLine("${idx + 1}. ${it.name} (${it.qty} × ₹${it.rate}) = ₹${String.format(Locale.US, "%.2f", it.totalAmount)}")
    }
    sb.appendLine("-------------------------")
    sb.appendLine("Taxable: ₹${String.format(Locale.US, "%.2f", grandTaxable)}")
    when (taxMode) {
        GstTaxMode.CGST_SGST -> {
            sb.appendLine("CGST: ₹${String.format(Locale.US, "%.2f", grandTax / 2.0)}")
            sb.appendLine("SGST: ₹${String.format(Locale.US, "%.2f", grandTax / 2.0)}")
        }
        GstTaxMode.IGST -> sb.appendLine("IGST: ₹${String.format(Locale.US, "%.2f", grandTax)}")
        GstTaxMode.NO_GST -> Unit
    }
    sb.appendLine("Grand Total: ₹${String.format(Locale.US, "%.2f", grandTotal)}")
    if (notes.isNotBlank()) sb.appendLine("Note: $notes")
    return sb.toString()
}

private fun generateInvoicePdf(
    context: Context,
    invoiceNumber: String,
    invoiceDate: String,
    businessName: String,
    sellerGstin: String,
    businessPhone: String,
    customerName: String,
    customerGstin: String,
    taxMode: GstTaxMode,
    items: List<InvoiceItem>,
    notes: String,
    logo: Bitmap?
): File? {
    return try {
        val doc = PdfDocument()
        val pageW = 595
        val pageH = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
        val page = doc.startPage(pageInfo)
        val c: Canvas = page.canvas

        val headerBg = Paint().apply { color = android.graphics.Color.rgb(30, 41, 59) }
        c.drawRect(0f, 0f, pageW.toFloat(), 118f, headerBg)

        var textLeft = 32f
        if (logo != null) {
            c.drawBitmap(logo, null, RectF(32f, 22f, 102f, 92f), Paint(Paint.FILTER_BITMAP_FLAG))
            textLeft = 116f
        }

        val titleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText(businessName.ifBlank { "OmniTools Store" }, textLeft, 48f, titleP)

        val subHeaderP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(203, 213, 225)
            textSize = 11f
        }
        if (sellerGstin.isNotBlank()) c.drawText("GSTIN: $sellerGstin", textLeft, 68f, subHeaderP)
        if (businessPhone.isNotBlank()) c.drawText("Contact: $businessPhone", textLeft, 85f, subHeaderP)

        val badgeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(250, 204, 21)
            textSize = 14f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText(if (taxMode == GstTaxMode.NO_GST) "INVOICE" else "TAX INVOICE", pageW - 32f, 46f, badgeP)
        val invMetaP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }
        c.drawText("# $invoiceNumber", pageW - 32f, 66f, invMetaP)
        c.drawText("Date: $invoiceDate", pageW - 32f, 84f, invMetaP)

        val darkBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(15, 23, 42)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        val darkNormal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(51, 65, 85)
            textSize = 11f
        }
        c.drawText("BILL TO:", 32f, 148f, darkBold)
        c.drawText(customerName, 32f, 166f, darkNormal)
        if (customerGstin.isNotBlank()) c.drawText("Buyer GSTIN: $customerGstin", 32f, 182f, darkNormal)

        // Table Header
        var y = 212f
        val tblHead = Paint().apply { color = android.graphics.Color.rgb(241, 245, 249) }
        c.drawRect(32f, y - 16f, pageW - 32f, y + 10f, tblHead)
        c.drawText("Item / HSN", 38f, y, darkBold)
        c.drawText("Qty", 290f, y, darkBold)
        c.drawText("Rate", 345f, y, darkBold)
        c.drawText("Tax", 425f, y, darkBold)
        c.drawText("Total", pageW - 38f, y, darkBold.apply { textAlign = Paint.Align.RIGHT })
        darkBold.textAlign = Paint.Align.LEFT
        y += 28f

        val rightNormal = Paint(darkNormal).apply { textAlign = Paint.Align.RIGHT }
        items.forEach { it ->
            if (y > pageH - 170f) return@forEach
            val label = if (it.hsnCode.isNotBlank()) "${it.name} [${it.hsnCode}]" else it.name
            c.drawText(label.take(34), 38f, y, darkNormal)
            c.drawText("${it.qty}", 290f, y, darkNormal)
            c.drawText("₹${String.format(Locale.US, "%.0f", it.rate)}", 345f, y, darkNormal)
            c.drawText("${it.gstRate.toInt()}%", 425f, y, darkNormal)
            c.drawText("₹${String.format(Locale.US, "%.2f", it.totalAmount)}", pageW - 38f, y, rightNormal)
            y += 22f
        }

        val lineP = Paint().apply {
            color = android.graphics.Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }
        c.drawLine(32f, y + 6f, pageW - 32f, y + 6f, lineP)
        y += 28f

        val subtotal = items.sumOf { it.totalTaxable }
        val totalTax = items.sumOf { it.taxAmount }
        val grand = items.sumOf { it.totalAmount }

        c.drawText("Taxable Amount:", 350f, y, darkNormal)
        c.drawText("₹${String.format(Locale.US, "%.2f", subtotal)}", pageW - 38f, y, rightNormal)
        y += 18f

        when (taxMode) {
            GstTaxMode.CGST_SGST -> {
                c.drawText("CGST:", 350f, y, darkNormal)
                c.drawText("₹${String.format(Locale.US, "%.2f", totalTax / 2.0)}", pageW - 38f, y, rightNormal)
                y += 18f
                c.drawText("SGST:", 350f, y, darkNormal)
                c.drawText("₹${String.format(Locale.US, "%.2f", totalTax / 2.0)}", pageW - 38f, y, rightNormal)
                y += 22f
            }
            GstTaxMode.IGST -> {
                c.drawText("IGST:", 350f, y, darkNormal)
                c.drawText("₹${String.format(Locale.US, "%.2f", totalTax)}", pageW - 38f, y, rightNormal)
                y += 22f
            }
            GstTaxMode.NO_GST -> y += 6f
        }

        val grandP = Paint(darkBold).apply {
            textSize = 15f
            color = android.graphics.Color.rgb(67, 56, 202)
        }
        c.drawText("GRAND TOTAL:", 350f, y, grandP)
        c.drawText("₹${String.format(Locale.US, "%.2f", grand)}", pageW - 38f, y, Paint(grandP).apply { textAlign = Paint.Align.RIGHT })

        if (notes.isNotBlank()) {
            c.drawText("Notes / UPI: $notes", 32f, pageH - 54f, darkNormal)
        }
        c.drawText("Generated via OmniTools GST Invoice Studio", 32f, pageH - 34f, darkNormal)

        doc.finishPage(page)
        val outDir = File(context.getExternalFilesDir(null), "PDF_Documents").apply { mkdirs() }
        val safeName = invoiceNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(outDir, "Invoice_${safeName}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        file
    } catch (e: Exception) {
        Toast.makeText(context, "PDF creation failed: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}
