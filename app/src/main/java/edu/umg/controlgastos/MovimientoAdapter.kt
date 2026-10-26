package edu.umg.controlgastos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.umg.controlgastos.databinding.ItemMovimientoBinding
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.modelo.Tipo
import edu.umg.programacion2.gastos.servicio.formatearQuetzales

/** Dibuja cada movimiento guardado: categoria, nota, fecha y monto. */
class MovimientoAdapter(
    private val alEliminar: (Gasto) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.Holder>() {

    var items: List<Gasto> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class Holder(val binding: ItemMovimientoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemMovimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val gasto = items[position]
        val b = holder.binding
        val esGasto = gasto.tipo == Tipo.GASTO

        b.tvCategoria.text = gasto.categoria.nombreVisible
        b.tvNota.text = gasto.nota
        b.tvNota.visibility = if (gasto.nota.isBlank()) View.GONE else View.VISIBLE
        b.tvFecha.text = gasto.fecha
        b.tvMonto.text = (if (esGasto) "- " else "+ ") + formatearQuetzales(gasto.monto)
        b.tvMonto.setTextColor(
            ContextCompat.getColor(b.root.context, if (esGasto) R.color.rojo else R.color.verde)
        )
        b.tvEliminar.setOnClickListener { alEliminar(gasto) }
    }
}
