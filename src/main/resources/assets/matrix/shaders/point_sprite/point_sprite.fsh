#version 410 core

layout (location = 7) in vec4 InColor;

out vec4 fragColor;

void main()
{
    vec2 coord = gl_PointCoord * 2.0 - 1.0;
    float squaredDistance = dot(coord, coord);
    if (squaredDistance > 1.0) {
        discard;
    }

    fragColor = InColor;
}