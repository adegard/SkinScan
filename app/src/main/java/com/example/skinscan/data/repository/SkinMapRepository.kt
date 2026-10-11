package com.example.skinscan.data.repository

import android.graphics.Bitmap
import com.example.skinscan.data.db.dao.LesionDao
import com.example.skinscan.data.db.dao.PhotoDao
import com.example.skinscan.data.db.dao.SessionDao
import com.example.skinscan.data.db.entity.LesionEntity
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.model.BodyRegion
import com.example.skinscan.data.model.LesionMatch
import com.example.skinscan.data.model.MatchStatus
import com.example.skinscan.data.model.RegionComparison
import com.example.skinscan.data.storage.PhotoStore
import com.example.skinscan.util.DemoPhotoFactory
import com.example.skinscan.util.ImageSignature
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.hypot

/**
 * Single entry point the UI talks to. Everything is on-device: photos live in
 * private app storage, the database is Room/SQLite, and change detection is a
 * local average-hash comparison.
 */
class SkinMapRepository(
    private val sessionDao: SessionDao,
    private val photoDao: PhotoDao,
    private val lesionDao: LesionDao,
    private val photoStore: PhotoStore,
) {

    // ---- Sessions -----------------------------------------------------------

    val sessions: Flow<List<SessionEntity>> = sessionDao.observeAll()

    fun observeSession(id: Long): Flow<SessionEntity?> = sessionDao.observe(id)

    suspend fun latestSession(): SessionEntity? = sessionDao.latest()

    suspend fun createSession(label: String? = null): Long {
        val now = System.currentTimeMillis()
        val name = label?.takeIf { it.isNotBlank() } ?: defaultLabel(now)
        return sessionDao.insert(SessionEntity(createdAt = now, label = name))
    }

    suspend fun deleteSession(id: Long) {
        photoStore.delete(photoStore.sessionDir(id))
        sessionDao.delete(id)
    }

    // ---- Photos / skin map --------------------------------------------------

    fun observePhotos(sessionId: Long): Flow<List<PhotoEntity>> =
        photoDao.observeForSession(sessionId)

    fun observePhoto(sessionId: Long, region: BodyRegion): Flow<PhotoEntity?> =
        photoDao.observeForRegion(sessionId, region.id)

    suspend fun photoFor(sessionId: Long, region: BodyRegion): PhotoEntity? =
        photoDao.getForRegion(sessionId, region.id)

    /**
     * Persists a freshly captured [bitmap] for [region], replacing any previous
     * shot for the same region in the same session, and fingerprints it.
     */
    suspend fun savePhoto(sessionId: Long, region: BodyRegion, bitmap: Bitmap): Long {
        val now = System.currentTimeMillis()
        val file = photoStore.fileFor(sessionId, region.id)
        photoStore.write(bitmap, file)
        val signature = ImageSignature.averageHash(bitmap)

        photoDao.getForRegion(sessionId, region.id)?.let { old ->
            photoStore.delete(old.filePath)
            photoDao.delete(old.id)
        }

        return photoDao.insert(
            PhotoEntity(
                sessionId = sessionId,
                regionId = region.id,
                filePath = file.absolutePath,
                capturedAt = now,
                signature = signature,
            ),
        )
    }

    /**
     * Fallback used when no camera is available: synthesises a photo for
     * [region] and saves it through the normal [savePhoto] path.
     */
    suspend fun captureDemo(sessionId: Long, region: BodyRegion): Long {
        val bitmap = DemoPhotoFactory.create(seed = sessionId * 131L + region.ordinal * 17L)
        val id = savePhoto(sessionId, region, bitmap)
        bitmap.recycle()
        return id
    }

    suspend fun deletePhoto(photo: PhotoEntity) {
        photoStore.delete(photo.filePath)
        photoDao.delete(photo.id)
    }

    // ---- Lesions ------------------------------------------------------------

    fun observeLesions(photoId: Long): Flow<List<LesionEntity>> =
        lesionDao.observeForPhoto(photoId)

    fun observeLesionCount(sessionId: Long): Flow<Int> =
        lesionDao.observeCountForSession(sessionId)

    suspend fun addLesion(
        photo: PhotoEntity,
        x: Float,
        y: Float,
        radius: Float = 0.05f,
        diameterMm: Float? = null,
        label: String = "",
        note: String = "",
    ): Long {
        val signature = photoStore.read(photo.filePath)?.let { full ->
            val patch = ImageSignature.crop(full, x, y, radius)
            val hash = ImageSignature.averageHash(patch)
            patch.recycle()
            full.recycle()
            hash
        } ?: 0L

        return lesionDao.insert(
            LesionEntity(
                photoId = photo.id,
                sessionId = photo.sessionId,
                regionId = photo.regionId,
                x = x,
                y = y,
                radius = radius,
                diameterMm = diameterMm,
                label = label,
                note = note,
                signature = signature,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteLesion(id: Long) = lesionDao.delete(id)

    // ---- Comparison ---------------------------------------------------------

    suspend fun compareRegion(
        region: BodyRegion,
        sessionA: Long,
        sessionB: Long,
    ): RegionComparison {
        val photoA = photoDao.getForRegion(sessionA, region.id)
        val photoB = photoDao.getForRegion(sessionB, region.id)
        val lesionsA = if (photoA != null) lesionDao.getForSessionRegion(sessionA, region.id) else emptyList()
        val lesionsB = if (photoB != null) lesionDao.getForSessionRegion(sessionB, region.id) else emptyList()
        return RegionComparison(region, photoA, photoB, matchLesions(lesionsA, lesionsB))
    }

    /**
     * Pairs lesions across the two sessions by nearest normalised position.
     * [POSITION_THRESHOLD] is the max distance (in normalised photo units) for
     * two marks to be considered "the same mole".
     */
    private fun matchLesions(
        a: List<LesionEntity>,
        b: List<LesionEntity>,
    ): List<LesionMatch> {
        val used = HashSet<Long>()
        val result = mutableListOf<LesionMatch>()

        for (la in a) {
            var best: LesionEntity? = null
            var bestDist = Float.MAX_VALUE
            for (lb in b) {
                if (lb.id in used) continue
                val d = hypot(la.x - lb.x, la.y - lb.y)
                if (d < bestDist) {
                    bestDist = d
                    best = lb
                }
            }
            if (best != null && bestDist <= POSITION_THRESHOLD) {
                used.add(best.id)
                val sim = if (la.signature != 0L && best.signature != 0L) {
                    ImageSignature.similarity(la.signature, best.signature)
                } else {
                    0f
                }
                val status = if (sim >= CHANGE_THRESHOLD) MatchStatus.UNCHANGED else MatchStatus.CHANGED
                result += LesionMatch(la, best, sim, status)
            } else {
                result += LesionMatch(la, null, 0f, MatchStatus.MISSING)
            }
        }
        for (lb in b) {
            if (lb.id !in used) result += LesionMatch(null, lb, 0f, MatchStatus.NEW)
        }
        return result
    }

    private fun defaultLabel(millis: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))

    companion object {
        private const val POSITION_THRESHOLD = 0.18f
        private const val CHANGE_THRESHOLD = 0.75f
    }
}
