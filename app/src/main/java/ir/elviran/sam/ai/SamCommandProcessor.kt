
package ir.elviran.sam.ai

import java.math.BigDecimal
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Local command processor.
 * This is a deterministic first layer, not an LLM.
 */
object SamCommandProcessor {

    fun respond(message: String): String {
        val input = normalize(message)

        if (input.isBlank()) {
            return "صدایت را دریافت کردم، اما متوجه درخواستت نشدم. دوباره بگو."
        }

        if (containsAny(input, listOf("سلام", "درود", "صبح بخیر", "عصر بخیر"))) {
            return "سلام! من سام هستم. گوش می‌دهم؛ چه کاری برایت انجام بدهم؟"
        }

        if (containsAny(input, listOf("تو کی هستی", "اسمت چیه", "معرفی کن"))) {
            return "من سام هستم؛ دستیار شخصی تو. در حال حاضر قابلیت‌های پایه محلی من فعال است."
        }

        if (containsAny(input, listOf("ساعت چنده", "ساعت چند است", "زمان چنده"))) {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            return "الان ساعت ${toPersianDigits(time)} است."
        }

        evaluateMath(input)?.let { return it }

        return "درخواستت را دریافت کردم، اما هنوز موتور هوش مصنوعی آنلاین به سام متصل نشده است. فعلاً می‌توانم محاسبات و فرمان‌های پایه را انجام بدهم."
    }

    private fun normalize(value: String): String {
        return value
            .lowercase()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('۰', '0').replace('۱', '1').replace('۲', '2')
            .replace('۳', '3').replace('۴', '4').replace('۵', '5')
            .replace('۶', '6').replace('۷', '7').replace('۸', '8')
            .replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2')
            .replace('٣', '3').replace('٤', '4').replace('٥', '5')
            .replace('٦', '6').replace('٧', '7').replace('٨', '8')
            .replace('٩', '9')
            .replace("به علاوه", "+")
            .replace("بعلاوه", "+")
            .replace("به اضافه", "+")
            .replace("منهای", "-")
            .replace("منها", "-")
            .replace("ضربدر", "*")
            .replace("ضرب در", "*")
            .replace("ضرب", "*")
            .replace("تقسیم بر", "/")
            .replace("تقسیم", "/")
            .replace("چند میشه", "")
            .replace("چقدر میشه", "")
            .replace("حاصل", "")
            .replace("حساب کن", "")
            .replace("محاسبه کن", "")
            .replace("لطفا", "")
            .replace("لطفاً", "")
            .replace("؟", "")
            .trim()
    }

    private fun containsAny(text: String, phrases: List<String>): Boolean =
        phrases.any(text::contains)

    private fun evaluateMath(input: String): String? {
        val expressionPattern = Regex(
            """-?\d+(?:[.,]\d+)?(?:\s*[+\-*/]\s*-?\d+(?:[.,]\d+)?)+"""
        )
        val expression = expressionPattern.find(input)?.value ?: return null

        return try {
            val normalizedExpression = expression.replace(',', '.')
            val tokens = Regex("""\d+(?:\.\d+)?|[+\-*/()]""")
                .findAll(normalizedExpression)
                .map { it.value }
                .toList()

            if (tokens.joinToString("") != normalizedExpression.replace(" ", "")) {
                return null
            }

            val parser = ExpressionParser(tokens)
            val result = parser.parse()

            if (!result.isFinite()) {
                return "نتیجه محاسبه قابل نمایش نیست."
            }

            val formatted = BigDecimal.valueOf(result)
                .stripTrailingZeros()
                .toPlainString()

            "نتیجه برابر است با ${toPersianDigits(formatted)}"
        } catch (exception: ArithmeticException) {
            "تقسیم بر صفر امکان‌پذیر نیست."
        } catch (exception: Exception) {
            null
        }
    }

    private fun toPersianDigits(value: String): String {
        val digits = "۰۱۲۳۴۵۶۷۸۹"
        return value.map { char ->
            if (char in '0'..'9') digits[char - '0'] else char
        }.joinToString("")
    }

    private class ExpressionParser(private val tokens: List<String>) {
        private var position = 0

        fun parse(): Double {
            val result = parseExpression()
            require(position == tokens.size) { "Unexpected token" }
            return result
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (position < tokens.size) {
                when (tokens[position]) {
                    "+" -> {
                        position++
                        value += parseTerm()
                    }
                    "-" -> {
                        position++
                        value -= parseTerm()
                    }
                    else -> return value
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (position < tokens.size) {
                when (tokens[position]) {
                    "*" -> {
                        position++
                        value *= parseFactor()
                    }
                    "/" -> {
                        position++
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        value /= divisor
                    }
                    else -> return value
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            require(position < tokens.size) { "Missing value" }
            val token = tokens[position++]

            return when (token) {
                "+" -> parseFactor()
                "-" -> -parseFactor()
                "(" -> {
                    val value = parseExpression()
                    require(position < tokens.size && tokens[position] == ")")
                    position++
                    value
                }
                else -> token.toDouble()
            }
        }
    }
}
