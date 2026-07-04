#version 330

layout(std140) uniform GuideLineTransform {
    mat4 ViewProjMat;
    vec4 LineParams;
};

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;

// 1.21 drew these as plain GL_LINES through position_color — hardware line rasterization,
// no width-quad expansion. The expansion variant's screen-direction sign-flip degenerated
// at near-vertical angles (segments vanished view-dependently), so this matches the old
// mechanism exactly: project and let the rasterizer draw 1px lines on both backends.
void main() {
    gl_Position = ViewProjMat * vec4(Position, 1.0);
    vertexColor = Color;
}
