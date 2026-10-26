package edu.umg.controlgastos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.umg.controlgastos.databinding.ActivityResumenBinding
import edu.umg.programacion2.gastos.servicio.formatearQuetzales
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Pantalla 2: ingreso del mes, gasto del mes y saldo restante. Con flechas para cambiar de mes. */
class ResumenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumenBinding
    private val control = ServicioApp.control

    // El mes que se esta mostrando (empieza en el mes actual).
    private val calendario: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState?.containsKey(CLAVE_MES) == true) {
            calendario.timeInMillis = savedInstanceState.getLong(CLAVE_MES)
        }

        binding.btnMesAnterior.setOnClickListener { cambiarMes(-1) }
        binding.btnMesSiguiente.setOnClickListener { cambiarMes(1) }
        binding.btnVolver.setOnClickListener { finish() }

        mostrar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(CLAVE_MES, calendario.timeInMillis)
    }

    private fun cambiarMes(delta: Int) {
        calendario.set(Calendar.DAY_OF_MONTH, 1)   // evita saltos raros (31 de enero + 1 mes)
        calendario.add(Calendar.MONTH, delta)
        mostrar()
    }

    private fun mostrar() {
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH) + 1   // Calendar cuenta los meses desde 0

        binding.tvMes.text = SimpleDateFormat("MMMM yyyy", Locale("es", "GT"))
            .format(calendario.time)
            .replaceFirstChar { it.uppercase() }

        lifecycleScope.launch {
            val r = control.resumenDelMes(anio, mes)
            binding.tvIngresos.text = formatearQuetzales(r.ingresos)
            binding.tvGastos.text = formatearQuetzales(r.gastos)
            binding.tvSaldo.text = formatearQuetzales(r.saldo)
            binding.tvSaldo.setTextColor(
                ContextCompat.getColor(this@ResumenActivity, if (r.saldo >= 0) R.color.azul else R.color.rojo)
            )
        }
    }

    private companion object {
        const val CLAVE_MES = "mes"
    }
}
