#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 coord = gl_PointCoord * 2.0 - 1.0;
    float squaredDistance = dot(coord, coord);
    if (squaredDistance > 1.0) {
        discard;
    }

    fragColor = vertexColor * ColorModulator;
}
