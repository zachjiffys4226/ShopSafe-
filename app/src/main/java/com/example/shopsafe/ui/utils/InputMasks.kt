package com.example.shopsafe.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Formats digits into standard US phone number format: (XXX) XXX-XXXX
 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(10)
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 0) append("(")
                if (i == 3) append(") ")
                if (i == 6) append("-")
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, digits.length)
                return when {
                    clamped <= 0 -> 0
                    clamped <= 3 -> clamped + 1 // "("
                    clamped <= 6 -> clamped + 3 // "(", ") "
                    else -> clamped + 4         // "(", ") ", "-"
                }.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return when {
                    clamped <= 1 -> 0
                    clamped <= 5 -> clamped - 1
                    clamped <= 9 -> clamped - 3
                    else -> (clamped - 4).coerceAtMost(digits.length)
                }.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * Formats 16 digits into 4-digit card blocks: XXXX XXXX XXXX XXXX
 */
class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(16)
        val formatted = buildString {
            for (i in digits.indices) {
                if (i > 0 && i % 4 == 0) append(" ")
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, digits.length)
                val spaces = if (clamped > 0) (clamped - 1) / 4 else 0
                return (clamped + spaces).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                val spaces = clamped / 5
                return (clamped - spaces).coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * Formats expiration date digits into MM/YY format automatically.
 */
class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(4)
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 2) append("/")
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, digits.length)
                return if (clamped <= 2) clamped else clamped + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return if (clamped <= 2) clamped else clamped - 1
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * Formats 6-digit verification codes as: XXX - XXX
 */
class TwoFactorCodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(6)
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 3) append(" - ")
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, digits.length)
                return if (clamped <= 3) clamped else clamped + 3
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return if (clamped <= 3) clamped else (clamped - 3).coerceAtLeast(0)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
