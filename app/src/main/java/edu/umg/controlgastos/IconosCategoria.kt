package edu.umg.controlgastos

import android.view.View
import android.widget.ImageView
import edu.umg.programacion2.gastos.modelo.Categoria

/**
 * Interruptor de los iconos:
 *  false -> solo texto, sin iconos (como esta ahora)
 *  true  -> las 6 categorias principales muestran su icono vectorial (res/drawable/ic_*.xml)
 * Nunca se usan emojis.
 */
const val MOSTRAR_ICONOS = false

fun Categoria.iconoVector(): Int? = when (this) {
    Categoria.TRANSPORTE -> R.drawable.ic_transporte
    Categoria.COMIDA -> R.drawable.ic_comida
    Categoria.HOGAR -> R.drawable.ic_hogar
    Categoria.MEDICO -> R.drawable.ic_medico
    Categoria.TECNOLOGIA -> R.drawable.ic_tecnologia
    Categoria.DIARIO -> R.drawable.ic_diario
    else -> null
}

/** Muestra el cuadrito del icono solo si hay un icono que mostrar; si no, lo oculta del todo. */
fun Categoria.pintarIcono(contenedor: View, imagen: ImageView) {
    val vector = if (MOSTRAR_ICONOS) iconoVector() else null
    if (vector == null) {
        contenedor.visibility = View.GONE
        return
    }
    imagen.setImageResource(vector)
    contenedor.visibility = View.VISIBLE
}
