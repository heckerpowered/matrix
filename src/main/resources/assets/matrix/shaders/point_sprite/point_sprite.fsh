#version 410 core

out vec4 fragColor;

void main()
{
    vec2 coord = gl_PointCoord * 2.0 - 1.0;
    if (dot(coord, coord) > 1.0){
        discard;
    }

    fragColor = vec4(0.2, 0.5, 1.0, 1.0);
    fragColor *= 2;
}