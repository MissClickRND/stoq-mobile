package bob.colbaskin.stoq.common.drafts.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.ColumnInfo
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drafts", indices = [Index(value = ["type", "updatedAtEpochMillis"])])
data class DraftEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val summary: String,
    val updatedAtEpochMillis: Long,
    val payloadJson: String,
    @ColumnInfo(defaultValue = "''") val ownerKey: String = ""
)

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts WHERE ownerKey = :ownerKey AND (:type IS NULL OR type = :type) ORDER BY updatedAtEpochMillis DESC")
    fun observeOwned(ownerKey: String, type: String?): Flow<List<DraftEntity>>
    @Query("SELECT * FROM drafts ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE type = :type ORDER BY updatedAtEpochMillis DESC")
    fun observeByType(type: String): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DraftEntity)

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Database(entities = [DraftEntity::class], version = 2, exportSchema = false)
abstract class StoqDatabase : RoomDatabase() {
    abstract fun draftDao(): DraftDao
}

val DraftOwnerMigration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE drafts ADD COLUMN ownerKey TEXT NOT NULL DEFAULT ''")
    }
}
