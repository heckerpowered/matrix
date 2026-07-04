#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 dissolveParams;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vertexColor = Color;
    dissolveParams = Normal * 0.5 + 0.5;
}
