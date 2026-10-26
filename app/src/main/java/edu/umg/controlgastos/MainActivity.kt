package edu.umg.controlgastos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.umg.controlgastos.databinding.ActivityMainBinding
import edu.umg.controlgastos.databinding.DialogMovimientoBinding
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.modelo.Tipo
import edu.umg.programacion2.gastos.servicio.TecladoNumerico
import edu.umg.programacion2.gastos.servicio.formatearQuetzales
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pantalla 1: elegir Gastos/Ingresos, tocar una categoria, registrar y ver lo guardado. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // La logica vive en la libreria; aqui solo se dibuja.
    private val control = ServicioApp.control

    private var tipoActual = Tipo.GASTO

    private val categoriasAdapter = CategoriaAdapter { categoria -> abrirDialogo(categoria) }
    private val movimientosAdapter = MovimientoAdapter { gasto -> confirmarEliminar(gasto) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Al girar el telefono recordamos si estaba en Gastos o en Ingresos.
        savedInstanceState?.getString(CLAVE_TIPO)?.let { tipoActual = Tipo.valueOf(it) }

        binding.rvCategorias.layoutManager = GridLayoutManager(this, 3)
        binding.rvCategorias.adapter = categoriasAdapter

        binding.rvMovimientos.layoutManager = LinearLayoutManager(this)
        binding.rvMovimientos.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)  // la separacion entre registros
        )
        binding.rvMovimientos.adapter = movimientosAdapter

        // Primero marcamos el boton y despues ponemos el listener.
        binding.toggleTipo.check(if (tipoActual == Tipo.GASTO) R.id.btnGastos else R.id.btnIngresos)
        binding.toggleTipo.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                tipoActual = if (checkedId == R.id.btnGastos) Tipo.GASTO else Tipo.INGRESO
                mostrarCategorias()
            }
        }
        mostrarCategorias()

        binding.btnResumen.setOnClickListener {
            startActivity(Intent(this, ResumenActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarLista()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLAVE_TIPO, tipoActual.name)
    }

    private fun mostrarCategorias() {
        categoriasAdapter.items = Categoria.deTipo(tipoActual)
    }

    private fun actualizarLista() {
        // listarOrdenados() es suspend -> se llama dentro de lifecycleScope.launch.
        lifecycleScope.launch {
            val lista = control.listarOrdenados()
            movimientosAdapter.items = lista
            binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /** La ventana con fecha automatica, monto (teclado 0-9), nota, Cancelar y Confirmar. */
    private fun abrirDialogo(categoria: Categoria) {
        val d = DialogMovimientoBinding.inflate(layoutInflater)
        val fecha = fechaDeHoy()
        var monto = ""   // el monto se arma como texto con el teclado

        d.tvTitulo.text = categoria.nombreVisible
        d.tvFecha.text = "Fecha: $fecha"

        fun refrescar() {
            d.tvMonto.text = "Q " + monto.ifEmpty { "0" }
            d.btnConfirmar.isEnabled = TecladoNumerico.aMonto(monto) > 0
        }
        refrescar()

        val dialogo = MaterialAlertDialogBuilder(this).setView(d.root).create()

        // Teclado: las filas vienen de la libreria, aqui solo se dibujan los botones.
        for (fila in TecladoNumerico.FILAS) {
            val filaView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            for (tecla in fila) {
                val boton = MaterialButton(this).apply {
                    text = tecla
                    textSize = 20f
                    layoutParams = LinearLayout.LayoutParams(0, dp(56), 1f).apply {
                        setMargins(dp(3), dp(3), dp(3), dp(3))
                    }
                    setOnClickListener {
                        monto = TecladoNumerico.aplicar(monto, tecla)
                        refrescar()
                    }
                }
                filaView.addView(boton)
            }
            d.tecladoContenedor.addView(filaView)
        }

        d.btnCancelar.setOnClickListener { dialogo.dismiss() }
        d.btnConfirmar.setOnClickListener {
            val gasto = Gasto(
                nota = d.etNota.text.toString().trim(),
                monto = TecladoNumerico.aMonto(monto),
                categoria = categoria,
                fecha = fecha
            )
            lifecycleScope.launch {
                // La validacion vive en la libreria, no en la pantalla.
                val error = control.registrar(gasto)
                if (error != null) {
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                    return@launch   // return dentro de launch: sale solo del bloque
                }
                dialogo.dismiss()
                actualizarLista()
            }
        }
        dialogo.show()
    }

    private fun confirmarEliminar(gasto: Gasto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Eliminar ${gasto.categoria} por ${formatearQuetzales(gasto.monto)}?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    control.eliminar(gasto.id)
                    actualizarLista()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // dp -> pixeles
    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()

    // Cuidado: java.time.LocalDate necesita API 26; con minSdk 24 usamos SimpleDateFormat.
    private fun fechaDeHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private companion object {
        const val CLAVE_TIPO = "tipo"
    }
}
