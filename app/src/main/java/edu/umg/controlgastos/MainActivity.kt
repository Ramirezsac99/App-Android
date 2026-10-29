package edu.umg.controlgastos

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.umg.controlgastos.databinding.ActivityMainBinding
import edu.umg.controlgastos.databinding.DialogCategoriasBinding
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

/** Pantalla 1: balance, elegir Gastos/Ingresos, tocar una categoria, registrar y ver lo guardado. */
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

        binding.rvCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.rvCategorias.adapter = categoriasAdapter

        binding.rvMovimientos.layoutManager = LinearLayoutManager(this)
        binding.rvMovimientos.adapter = movimientosAdapter

        binding.btnGastos.setOnClickListener { cambiarTipo(Tipo.GASTO) }
        binding.btnIngresos.setOnClickListener { cambiarTipo(Tipo.INGRESO) }
        binding.tvVerTodas.setOnClickListener { abrirTodasLasCategorias() }
        binding.btnResumen.setOnClickListener {
            startActivity(Intent(this, ResumenActivity::class.java))
        }

        marcarTipo()
        mostrarCategorias()
    }

    override fun onResume() {
        super.onResume()
        actualizarLista()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLAVE_TIPO, tipoActual.name)
    }

    private fun cambiarTipo(tipo: Tipo) {
        tipoActual = tipo
        marcarTipo()
        mostrarCategorias()
    }

    /** Pinta de morado el boton del tipo elegido (el selector del XML reacciona a isSelected). */
    private fun marcarTipo() {
        binding.btnGastos.isSelected = tipoActual == Tipo.GASTO
        binding.btnIngresos.isSelected = tipoActual == Tipo.INGRESO
    }

    private fun mostrarCategorias() {
        val todas = Categoria.deTipo(tipoActual)
        // En Gastos mostramos 6 "principales"; el resto se ve con "Ver todas".
        val visibles = if (tipoActual == Tipo.GASTO) PRINCIPALES_GASTO else todas.take(6)

        categoriasAdapter.items = visibles
        binding.tvTituloCategorias.text =
            if (tipoActual == Tipo.GASTO) "CATEGORÍAS PRINCIPALES" else "CATEGORÍAS DE INGRESOS"
        binding.tvVerTodas.text = "Ver todas (${todas.size})"
        binding.tvVerTodas.visibility = if (todas.size > visibles.size) View.VISIBLE else View.GONE
    }

    private fun actualizarLista() {
        // listarOrdenados() es suspend -> se llama dentro de lifecycleScope.launch.
        lifecycleScope.launch {
            val lista = control.listarOrdenados()
            movimientosAdapter.items = lista
            binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE

            // Balance actual = todo lo que ha entrado menos todo lo que ha salido.
            val balance = lista.sumOf { if (it.tipo == Tipo.INGRESO) it.monto else -it.monto }
            binding.tvBalance.text = formatearQuetzales(balance)
        }
    }

    /** "Ver todas": una ventana con la cuadricula completa de categorias del tipo elegido. */
    private fun abrirTodasLasCategorias() {
        val d = DialogCategoriasBinding.inflate(layoutInflater)
        val dialogo = MaterialAlertDialogBuilder(this).setView(d.root).create()
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val adapter = CategoriaAdapter { categoria ->
            dialogo.dismiss()
            abrirDialogo(categoria)
        }
        adapter.items = Categoria.deTipo(tipoActual)
        d.rvTodas.layoutManager = GridLayoutManager(this, 3)
        d.rvTodas.adapter = adapter
        d.btnCerrar.setOnClickListener { dialogo.dismiss() }
        dialogo.show()
    }

    /** La ventana con icono, fecha automatica, monto (teclado 0-9), nota, Cancelar y Confirmar. */
    private fun abrirDialogo(categoria: Categoria) {
        val d = DialogMovimientoBinding.inflate(layoutInflater)
        val fecha = fechaDeHoy()
        val esGasto = categoria.tipo == Tipo.GASTO
        var monto = ""   // el monto se arma como texto con el teclado

        categoria.pintarIcono(d.contenedorIcono, d.ivIcono)
        d.tvTitulo.text = categoria.nombreVisible
        d.tvFecha.text = "Fecha: $fecha"
        d.tvMonto.setTextColor(ContextCompat.getColor(this, if (esGasto) R.color.rojo else R.color.verde))

        fun refrescar() {
            d.tvMonto.text = (if (esGasto) "- Q " else "+ Q ") + monto.ifEmpty { "0" }
            d.btnConfirmar.isEnabled = TecladoNumerico.aMonto(monto) > 0
        }
        refrescar()

        val dialogo = MaterialAlertDialogBuilder(this).setView(d.root).create()
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Teclado: las filas vienen de la libreria, aqui solo se dibujan los botones.
        val colorTecla = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tecla))
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
                    setTextColor(ContextCompat.getColor(context, R.color.texto))
                    backgroundTintList = colorTecla
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

        // Las 6 categorias de gasto que se ven sin tocar "Ver todas".
        val PRINCIPALES_GASTO = listOf(
            Categoria.TRANSPORTE, Categoria.COMIDA, Categoria.HOGAR,
            Categoria.MEDICO, Categoria.TECNOLOGIA, Categoria.DIARIO
        )
    }
}
