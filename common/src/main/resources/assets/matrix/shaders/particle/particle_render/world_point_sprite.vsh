#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec4 vertexColor;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    float distance = max(length(viewPosition.xyz), 0.01);
    gl_PointSize = UV0.x / distance;

    vertexColor = Color * UV0.y;
}
