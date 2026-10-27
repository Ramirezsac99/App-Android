package edu.umg.controlgastos

import android.app.Application

/** Android la crea UNA vez al abrir la app, antes que cualquier pantalla. */
class GastosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServicioApp.iniciar(this)
    }
}
