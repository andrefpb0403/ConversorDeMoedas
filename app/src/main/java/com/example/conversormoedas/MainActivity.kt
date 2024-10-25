package com.example.conversormoedas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Currency(val name: String, val code: String, val flagResId: Int)

class MainActivity : AppCompatActivity() {

    object RetrofitCliente {
        private const val BASE_URL: String = "http://data.fixer.io/api/"

        val instance: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    private lateinit var inputAmount: TextInputEditText
    private lateinit var tvAmountConverted: TextView
    private lateinit var tvRecuperedRate: TextView
    lateinit var fromCurrency: Currency
    lateinit var toCurrency: Currency
    val apiKey = "712d0ed55ba215a620001575d466b348"
    val apiService = RetrofitCliente.instance.create(CurrencyApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inputAmount = findViewById(R.id.tie_amount_to_convert)
        tvAmountConverted = findViewById(R.id.tv_result_converted_amount)
        tvRecuperedRate = findViewById(R.id.tv_indicated_rate)
        val spinnerAmountToConvert: Spinner = findViewById(R.id.spinner_amount_to_convert)
        val spinnerAmountConverted: Spinner = findViewById(R.id.spinner_amount_converted)
        val ivFlagToConvert = findViewById<ImageView>(R.id.iv_flags_to_convert)
        val ivFlagConverted = findViewById<ImageView>(R.id.iv_flags_converted)

        val currencys = listOf(
            Currency("Real Brasileiro", "BRL", R.drawable.brazil),
            Currency("Euro", "EUR", R.drawable.europe),
            Currency("Ien Japonês", "JPY", R.drawable.japan),
            Currency("Dólar Americano", "USD", R.drawable.united_states),
            Currency("Libra Esterlina", "GBP", R.drawable.united_kingdom),
            Currency("Dólar Australiano", "AUD", R.drawable.australia),
            Currency("Dólar Canadense", "CAD", R.drawable.canada),
            Currency("Renminbi Chinês", "CNY", R.drawable.china),
            Currency("Dólar de Hong Kong", "HKD", R.drawable.hong_kong),
            Currency("Dólar de Singapura", "SGD", R.drawable.singapore),
            Currency("Rupia Indiana", "INR", R.drawable.india),
            Currency("Rublo Russo", "RUB", R.drawable.russia),
            Currency("Peso Mexicano", "MXN", R.drawable.mexico),
            Currency("Rand Sul-Africano", "ZAR", R.drawable.south_africa),
            Currency("Won Sul-Coreano", "KRW", R.drawable.south_korea),
            Currency("Lira Turca", "TRY", R.drawable.turkey),
            Currency("Rial Saudita", "SAR", R.drawable.saudi_arabia),
            Currency("Peso Argentino", "ARS", R.drawable.argentina),
            Currency("Dirham dos Emirados Árabes Unidos", "AED", R.drawable.united_arab_emirates),
        )

        val currencyName = currencys.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyName)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAmountToConvert.adapter = adapter
        spinnerAmountConverted.adapter = adapter

        spinnerAmountToConvert.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    fromCurrency = currencys[position]
                    ivFlagToConvert.setImageResource(fromCurrency.flagResId)
                    performConversion() // Chama a conversão ao selecionar a moeda
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        spinnerAmountConverted.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    toCurrency = currencys[position]
                    ivFlagConverted.setImageResource(toCurrency.flagResId)
                    performConversion() // Chama a conversão ao selecionar a moeda
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        inputAmount.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    performConversion()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                }
            })
    }

    fun performConversion() {
        val amountStr = inputAmount.text.toString()
        if (amountStr.isNotEmpty()) {
            val amountToConvert = amountStr.toDouble()

            // Chamada à API para obter as taxas de conversão
            val call = apiService.getExchangeRates(apiKey, fromCurrency.code)
            call.enqueue(object : Callback<CurrencyResponse> {
                override fun onResponse(
                    call: Call<CurrencyResponse>,
                    response: Response<CurrencyResponse>,
                ) {
                    if (response.isSuccessful) {
                        val rates = response.body()?.rates
                        rates?.let {
                            val conversionRate = rates[toCurrency.code]
                            if (conversionRate != null) {
                                val convertedAmount = amountToConvert * conversionRate
                                tvAmountConverted.text =
                                    String.format("%.2f", convertedAmount)
                                tvRecuperedRate.text = "Taxa: $conversionRate"
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Taxa não disponível",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Erro ao obter taxas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CurrencyResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Erro: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        }
    }
}

