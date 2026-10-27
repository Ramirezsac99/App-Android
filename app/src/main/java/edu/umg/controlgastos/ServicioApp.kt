package edu.umg.controlgastos

import android.content.Context
import androidx.room.Room
import edu.umg.controlgastos.datos.BaseDatos
import edu.umg.controlgastos.datos.GastoRepositoryRoom
import edu.umg.programacion2.gastos.servicio.ControlGastos

/**
 * Un SOLO ControlGastos para toda la app (compartido por las dos pantallas).
 * Ahora guarda en la base de datos del telefono (Room/SQLite) en lugar de la memoria.
 */
object ServicioApp {
    lateinit var control: ControlGastos
        private set

    fun iniciar(context: Context) {
        val bd = Room.databaseBuilder(
            context.applicationContext,
            BaseDatos::class.java,
            "control_gastos.db"
        ).build()
        control = ControlGastos(GastoRepositoryRoom(bd.gastoDao()))
    }
}
