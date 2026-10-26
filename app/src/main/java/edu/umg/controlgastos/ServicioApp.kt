package edu.umg.controlgastos

import edu.umg.programacion2.gastos.datos.GastoRepositoryEnMemoria
import edu.umg.programacion2.gastos.servicio.ControlGastos

/**
 * Un SOLO ControlGastos para toda la app.
 *
 * Por que un object y no una propiedad dentro de MainActivity:
 *  - Las dos pantallas (Registro y Resumen) deben ver los MISMOS datos.
 *  - Al girar el telefono Android destruye y recrea la Activity; si el control viviera
 *    dentro de ella, los datos en memoria se perderian.
 *
 * En la semana 4 solo cambia esta linea:
 *   val control = ControlGastos(GastoRepositoryRoom(dao))
 */
object ServicioApp {
    val control = ControlGastos(GastoRepositoryEnMemoria())
}
