#version 330 core

in vec2 fragTexCoord;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define progress MatrixPostData0.x
#define radius MatrixPostData0.y
#define thickness MatrixPostData0.z
#define center MatrixPostData1.xy
#define color MatrixPostData2

out vec4 fragColor;

#define PI 3.141592653589793

void main() {
    vec2 direction = fragTexCoord - center;
    float distance = length(direction);
    if (distance < radius - thickness || distance > radius) {
        discard;
    }

    float angle = atan(direction.y, direction.x);
    if (angle < 0.0) {
        // Map angle from (-π, π] to [0, 2π)
        angle += 2.0 * PI;
    }

    // Ring starts at +Y axis (top)
    float start = 0.5 * PI;
    float sweep = progress * 2.0 * PI;
    float end = start - sweep;

    bool inRange;
    if (end < 0.0) {
        inRange = angle <= start && angle >= 0.0 || angle >= end + 2.0 * PI && angle <= 2.0 * PI;
    } else {
        inRange = angle <= start && angle >= end;
    }

    if (!inRange) {
        discard;
    }
    fragColor = color;
}
