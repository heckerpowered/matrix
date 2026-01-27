#version 410 core

in vec4 Color;

out vec4 FragColor;

void main()
{
    vec2 coord = gl_PointCoord * 2.0 - 1.0;
    float dist = dot(coord, coord);
    if (dist > 1.0) {
        discard;
    }

    FragColor = Color;
}