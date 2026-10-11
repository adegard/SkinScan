package com.example.skinscan.data.model

/** Which side of the body diagram a region belongs to. */
enum class BodyView { FRONT, BACK }

/**
 * A fixed catalogue of anatomical regions used both as the "drawing of the body"
 * that tells the user where to point the camera, and as the stable key that lets
 * us compare the same area across sessions (i.e. area-by-area evolution).
 *
 * [cx] / [cy] are normalised coordinates (0..1) on the body diagram for the
 * matching [view].
 */
enum class BodyRegion(
    val id: String,
    val label: String,
    val view: BodyView,
    val cx: Float,
    val cy: Float,
) {
    SCALP("scalp", "Scalp / vertex", BodyView.BACK, 0.50f, 0.045f),
    FACE("face", "Face", BodyView.FRONT, 0.50f, 0.058f),
    NECK_FRONT("neck_front", "Neck (front)", BodyView.FRONT, 0.50f, 0.115f),
    NAPE("nape", "Nape", BodyView.BACK, 0.50f, 0.115f),

    CHEST("chest", "Chest", BodyView.FRONT, 0.50f, 0.185f),
    ABDOMEN("abdomen", "Abdomen", BodyView.FRONT, 0.50f, 0.275f),
    GROIN("groin", "Groin", BodyView.FRONT, 0.50f, 0.350f),

    LEFT_SHOULDER("l_shoulder", "Left shoulder", BodyView.FRONT, 0.335f, 0.155f),
    RIGHT_SHOULDER("r_shoulder", "Right shoulder", BodyView.FRONT, 0.665f, 0.155f),
    LEFT_UPPER_ARM("l_upper_arm", "Left upper arm", BodyView.FRONT, 0.280f, 0.220f),
    RIGHT_UPPER_ARM("r_upper_arm", "Right upper arm", BodyView.FRONT, 0.720f, 0.220f),
    LEFT_FOREARM("l_forearm", "Left forearm", BodyView.FRONT, 0.260f, 0.310f),
    RIGHT_FOREARM("r_forearm", "Right forearm", BodyView.FRONT, 0.740f, 0.310f),
    LEFT_HAND("l_hand", "Left hand", BodyView.FRONT, 0.250f, 0.390f),
    RIGHT_HAND("r_hand", "Right hand", BodyView.FRONT, 0.750f, 0.390f),

    LEFT_THIGH("l_thigh", "Left thigh", BodyView.FRONT, 0.440f, 0.465f),
    RIGHT_THIGH("r_thigh", "Right thigh", BodyView.FRONT, 0.560f, 0.465f),
    LEFT_SHIN("l_shin", "Left shin", BodyView.FRONT, 0.440f, 0.600f),
    RIGHT_SHIN("r_shin", "Right shin", BodyView.FRONT, 0.560f, 0.600f),
    LEFT_FOOT("l_foot", "Left foot", BodyView.FRONT, 0.440f, 0.700f),
    RIGHT_FOOT("r_foot", "Right foot", BodyView.FRONT, 0.560f, 0.700f),

    UPPER_BACK("upper_back", "Upper back", BodyView.BACK, 0.50f, 0.185f),
    LOWER_BACK("lower_back", "Lower back", BodyView.BACK, 0.50f, 0.275f),
    BUTTOCKS("buttocks", "Buttocks", BodyView.BACK, 0.50f, 0.350f),
    LEFT_CALF("l_calf", "Left calf", BodyView.BACK, 0.440f, 0.600f),
    RIGHT_CALF("r_calf", "Right calf", BodyView.BACK, 0.560f, 0.600f);

    companion object {
        fun fromId(id: String): BodyRegion? = entries.firstOrNull { it.id == id }

        fun forView(view: BodyView): List<BodyRegion> = entries.filter { it.view == view }
    }
}
