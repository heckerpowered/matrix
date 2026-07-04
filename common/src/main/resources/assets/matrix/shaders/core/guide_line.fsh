#version 330

layout(std140) uniform GuideLineTransform {
    mat4 ViewProjMat;
    vec4 LineParams;
};

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    fragColor = vec4(vertexColor.rgb * LineParams.z, vertexColor.a);
}
