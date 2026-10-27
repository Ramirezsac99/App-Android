package edu.umg.controlgastos.datos

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import edu.umg.programacion2.gastos.datos.GastoRepository
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto

/** La tabla "gastos" de SQLite. Es la version "de base de datos" de la clase Gasto de la libreria. */
@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nota: String,
    val monto: Double,
    val categoria: String,   // se guarda el NOMBRE INTERNO del enum, ej. "TRANSPORTE"
    val fecha: String
)

/** Las operaciones SQL. Room genera el codigo por nosotros. */
@Dao
interface GastoDao {
    @Insert suspend fun insertar(g: GastoEntity): Long
    @Query("SELECT * FROM gastos") suspend fun todos(): List<GastoEntity>
    @Query("SELECT * FROM gastos WHERE id = :id") suspend fun buscar(id: Int): GastoEntity?
    @Update suspend fun actualizar(g: GastoEntity): Int
    @Query("DELETE FROM gastos WHERE id = :id") suspend fun eliminar(id: Int): Int
}

@Database(entities = [GastoEntity::class], version = 1, exportSchema = false)
abstract class BaseDatos : RoomDatabase() {
    abstract fun gastoDao(): GastoDao
}

/**
 * Cumple el contrato GastoRepository de la libreria, pero guarda en el telefono.
 * ControlGastos no se entera del cambio: solo conoce la interface.
 */
class GastoRepositoryRoom(private val dao: GastoDao) : GastoRepository {

    override suspend fun agregar(gasto: Gasto): Gasto {
        val id = dao.insertar(gasto.aEntity()).toInt()   // Room devuelve el id que le asigno
        return gasto.copy(id = id)
    }

    override suspend fun listar(): List<Gasto> = dao.todos().map { it.aGasto() }

    override suspend fun buscarPorId(id: Int): Gasto? = dao.buscar(id)?.aGasto()

    override suspend fun actualizar(gasto: Gasto): Boolean = dao.actualizar(gasto.aEntity()) > 0

    override suspend fun eliminar(id: Int): Boolean = dao.eliminar(id) > 0
}

private fun Gasto.aEntity() = GastoEntity(id, nota, monto, categoria.name, fecha)
private fun GastoEntity.aGasto() = Gasto(id, nota, monto, Categoria.valueOf(categoria), fecha)
