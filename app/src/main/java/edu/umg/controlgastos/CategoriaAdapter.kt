package edu.umg.controlgastos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.umg.controlgastos.databinding.ItemCategoriaBinding
import edu.umg.programacion2.gastos.modelo.Categoria

/** Dibuja cada categoria como un boton de la cuadricula. */
class CategoriaAdapter(
    private val alTocar: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.Holder>() {

    var items: List<Categoria> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class Holder(val binding: ItemCategoriaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val categoria = items[position]
        holder.binding.btnCategoria.text = categoria.nombreVisible
        holder.binding.btnCategoria.setOnClickListener { alTocar(categoria) }
    }
}
