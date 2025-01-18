package heckerpowered.matrix.core

fun lerp(delta: Double, from: Double, to: Double): Double {
    return from + delta * (to - from)
}

fun wrapDegrees(degrees: Double): Double {
    var wrapDegrees = degrees % 360.0
    if (wrapDegrees >= 180.0) {
        wrapDegrees -= 360.0
    }

    if (wrapDegrees < -180.0f) {
        wrapDegrees += 360.0f
    }

    return wrapDegrees
}

fun toDegrees(radians: Double): Double {
    return Math.toDegrees(radians)
}

fun toRadians(degrees: Double): Double {
    return Math.toRadians(degrees)
}