package com.github.adamantcheese.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.adamantcheese.model.entity.archive.LastUsedArchiveForThreadDto
import com.github.adamantcheese.model.entity.archive.LastUsedArchiveForThreadRelationEntity

@Dao
abstract class LastUsedArchiveForThreadDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insert(
    lastUsedArchiveForThreadRelationEntity: LastUsedArchiveForThreadRelationEntity
  )

  @Query("""
    SELECT *
    FROM ${LastUsedArchiveForThreadRelationEntity.TABLE_NAME}
    WHERE ${LastUsedArchiveForThreadRelationEntity.OWNER_THREAD_ID_COLUMN_NAME} = :ownerThreadId
  """)
  abstract suspend fun select(ownerThreadId: Long): LastUsedArchiveForThreadDto?

}