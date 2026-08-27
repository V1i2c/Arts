package com.artspath.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert
    suspend fun insertAll(subjects: List<Subject>)

    @Insert
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun count(): Int

    @Query("SELECT * FROM subjects ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE isMine = 1 ORDER BY sortOrder, name")
    fun observeMine(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE isMine = 1 ORDER BY sortOrder, name")
    suspend fun mine(): List<Subject>

    @Query("UPDATE subjects SET isMine = :mine WHERE id = :id")
    suspend fun setMine(id: Long, mine: Boolean)

    @Query("DELETE FROM subjects WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustom(id: Long)
}

data class ChapterWithCount(
    val id: Long,
    val subjectId: Long,
    val name: String,
    val part: String?,
    val sortOrder: Int,
    val isCustom: Boolean,
    val errorCount: Int,
    val revisedCount: Int
)

@Dao
interface ChapterDao {
    @Insert
    suspend fun insertAll(chapters: List<Chapter>)

    @Insert
    suspend fun insert(chapter: Chapter): Long

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId AND isCustom = 0")
    suspend fun deleteSeededFor(subjectId: Long)

    @Query(
        """
        SELECT c.id AS id, c.subjectId AS subjectId, c.name AS name, c.part AS part,
               c.sortOrder AS sortOrder, c.isCustom AS isCustom,
               (SELECT COUNT(*) FROM errors e WHERE e.chapterId = c.id) AS errorCount,
               (SELECT COUNT(*) FROM errors e WHERE e.chapterId = c.id AND e.revisionCount > 0) AS revisedCount
        FROM chapters c
        WHERE c.subjectId = :subjectId
        ORDER BY c.sortOrder, c.name
        """
    )
    fun observeWithCounts(subjectId: Long): Flow<List<ChapterWithCount>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY sortOrder, name")
    suspend fun forSubject(subjectId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun byId(id: Long): Chapter?

    @Query("SELECT COUNT(*) FROM chapters WHERE subjectId = :subjectId")
    suspend fun countForSubject(subjectId: Long): Int
}

data class TaskRow(
    val id: Long,
    val title: String,
    val subjectId: Long?,
    val subjectName: String?,
    val subjectColor: String?,
    val dueDay: Long?,
    val dueMinute: Int?,
    val status: String,
    val createdAt: Long,
    val completedDay: Long?,
    val completedAt: Long?
)

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun byId(id: Long): Task?

    @Query(
        """
        SELECT t.id AS id, t.title AS title, t.subjectId AS subjectId,
               s.name AS subjectName, s.colorKey AS subjectColor,
               t.dueDay AS dueDay, t.dueMinute AS dueMinute, t.status AS status,
               t.createdAt AS createdAt, t.completedDay AS completedDay, t.completedAt AS completedAt
        FROM tasks t LEFT JOIN subjects s ON s.id = t.subjectId
        WHERE t.status = 'PENDING' AND t.dueDay IS NOT NULL AND t.dueDay <= :today
        ORDER BY t.dueDay, t.dueMinute
        """
    )
    fun observeDueAndOverdue(today: Long): Flow<List<TaskRow>>

    @Query(
        """
        SELECT t.id AS id, t.title AS title, t.subjectId AS subjectId,
               s.name AS subjectName, s.colorKey AS subjectColor,
               t.dueDay AS dueDay, t.dueMinute AS dueMinute, t.status AS status,
               t.createdAt AS createdAt, t.completedDay AS completedDay, t.completedAt AS completedAt
        FROM tasks t LEFT JOIN subjects s ON s.id = t.subjectId
        WHERE t.status = 'PENDING' AND t.dueDay > :today
        ORDER BY t.dueDay, t.dueMinute
        """
    )
    fun observeUpcoming(today: Long): Flow<List<TaskRow>>

    @Query(
        """
        SELECT t.id AS id, t.title AS title, t.subjectId AS subjectId,
               s.name AS subjectName, s.colorKey AS subjectColor,
               t.dueDay AS dueDay, t.dueMinute AS dueMinute, t.status AS status,
               t.createdAt AS createdAt, t.completedDay AS completedDay, t.completedAt AS completedAt
        FROM tasks t LEFT JOIN subjects s ON s.id = t.subjectId
        WHERE t.status = 'PENDING'
        ORDER BY (t.dueDay IS NULL), t.dueDay, t.dueMinute, t.createdAt DESC
        """
    )
    fun observeAllPending(): Flow<List<TaskRow>>

    @Query(
        """
        SELECT t.id AS id, t.title AS title, t.subjectId AS subjectId,
               s.name AS subjectName, s.colorKey AS subjectColor,
               t.dueDay AS dueDay, t.dueMinute AS dueMinute, t.status AS status,
               t.createdAt AS createdAt, t.completedDay AS completedDay, t.completedAt AS completedAt
        FROM tasks t LEFT JOIN subjects s ON s.id = t.subjectId
        WHERE t.status = 'DONE' AND t.completedDay = :day
        ORDER BY t.completedAt
        """
    )
    fun observeDoneOn(day: Long): Flow<List<TaskRow>>

    @Query("UPDATE tasks SET status = 'DONE', completedDay = :day, completedAt = :ts WHERE id = :id")
    suspend fun markDone(id: Long, day: Long, ts: Long)

    @Query("UPDATE tasks SET status = 'PENDING', completedDay = NULL, completedAt = NULL WHERE id = :id")
    suspend fun markPending(id: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE'")
    fun observeTotalDone(): Flow<Int>
}

data class PlanRow(
    val id: Long,
    val title: String,
    val note: String,
    val day: Long,
    val startMinute: Int?,
    val endMinute: Int?,
    val subjectId: Long?,
    val subjectName: String?,
    val subjectColor: String?,
    val reminderMinutesBefore: Int?,
    val triggerAtMillis: Long?,
    val done: Boolean,
    val createdAt: Long
)

@Dao
interface PlanDao {
    @Insert
    suspend fun insert(entry: PlanEntry): Long

    @Update
    suspend fun update(entry: PlanEntry)

    @Query("DELETE FROM plan_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM plan_entries WHERE id = :id")
    suspend fun byId(id: Long): PlanEntry?

    @Query(
        """
        SELECT p.id AS id, p.title AS title, p.note AS note, p.day AS day,
               p.startMinute AS startMinute, p.endMinute AS endMinute,
               p.subjectId AS subjectId, s.name AS subjectName, s.colorKey AS subjectColor,
               p.reminderMinutesBefore AS reminderMinutesBefore, p.triggerAtMillis AS triggerAtMillis,
               p.done AS done, p.createdAt AS createdAt
        FROM plan_entries p LEFT JOIN subjects s ON s.id = p.subjectId
        WHERE p.day BETWEEN :fromDay AND :toDay
        ORDER BY (p.startMinute IS NULL) DESC, p.startMinute, p.createdAt
        """
    )
    fun observeBetween(fromDay: Long, toDay: Long): Flow<List<PlanRow>>
}

@Dao
interface ErrorDao {
    @Insert
    suspend fun insert(error: ErrorRecord): Long

    @Update
    suspend fun update(error: ErrorRecord)

    @Query("DELETE FROM errors WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM errors WHERE id = :id")
    suspend fun byId(id: Long): ErrorRecord?

    @Query(
        """
        SELECT e.* FROM errors e WHERE e.chapterId = :chapterId
        ORDER BY e.createdAt DESC
        """
    )
    fun observeByChapter(chapterId: Long): Flow<List<ErrorRecord>>

    @Query("SELECT COUNT(*) FROM errors")
    fun observeTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM errors")
    suspend fun total(): Int

    @Query("SELECT IFNULL(SUM(revisionCount), 0) FROM errors")
    fun observeTotalRevisions(): Flow<Int>

    @Query(
        "UPDATE errors SET revisionCount = revisionCount + 1, lastRevisedAt = :ts WHERE id = :id"
    )
    suspend fun bumpRevision(id: Long, ts: Long)

    @Query(
        """
        SELECT s.id AS subjectId, s.name AS name, s.colorKey AS colorKey,
               (SELECT COUNT(*) FROM errors e WHERE e.subjectId = s.id) AS errorCount
        FROM subjects s WHERE s.isMine = 1
        ORDER BY (SELECT COUNT(*) FROM errors e WHERE e.subjectId = s.id) DESC, s.sortOrder
        """
    )
    fun observeSubjectCounts(): Flow<List<SubjectErrorCount>>
}

data class SubjectErrorCount(
    val subjectId: Long,
    val name: String,
    val colorKey: String,
    val errorCount: Int
)

@Dao
interface AttachmentDao {
    @Insert
    suspend fun insert(attachment: Attachment): Long

    @Query("SELECT * FROM attachments WHERE errorId = :errorId ORDER BY id")
    fun observeForError(errorId: Long): Flow<List<Attachment>>

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun byId(id: Long): Attachment?

    @Query("SELECT * FROM attachments WHERE errorId = :errorId")
    suspend fun forError(errorId: Long): List<Attachment>

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun delete(id: Long)
}

data class DayStat(
    val day: Long,
    val tasksDone: Int,
    val errorsAdded: Int,
    val errorsRevised: Int
) {
    val total: Int get() = tasksDone + errorsAdded + errorsRevised
}

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(event: ActivityEvent)

    @Query("DELETE FROM activity_events WHERE type = :type AND refId = :refId")
    suspend fun removeFor(type: String, refId: Long)

    @Query(
        """
        SELECT day,
               SUM(CASE WHEN type = 'TASK_COMPLETED' THEN 1 ELSE 0 END) AS tasksDone,
               SUM(CASE WHEN type = 'ERROR_ADDED' THEN 1 ELSE 0 END) AS errorsAdded,
               SUM(CASE WHEN type = 'ERROR_REVISED' THEN 1 ELSE 0 END) AS errorsRevised
        FROM activity_events
        WHERE day BETWEEN :fromDay AND :toDay
        GROUP BY day
        ORDER BY day DESC
        """
    )
    fun observeDayStats(fromDay: Long, toDay: Long): Flow<List<DayStat>>

    @Query("SELECT DISTINCT day FROM activity_events ORDER BY day")
    fun observeActiveDays(): Flow<List<Long>>

    @Query(
        """
        SELECT * FROM activity_events
        ORDER BY ts DESC LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<ActivityEvent>>
}
