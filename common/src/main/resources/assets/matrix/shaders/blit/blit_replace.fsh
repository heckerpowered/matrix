#version 330

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform BlitConfig {
    float lod;
};

out vec4 fragColor;

// linearCopyTo/nearestCopyTo were raw glBlitFramebuffer calls in 1.21: a full REPLACE of
// every destination pixel, black/transparent included. blit_no_depth.fsh's black-pixel
// discard belongs to the compositing copyFramebuffer path only — reusing it here left the
// destination's previous contents behind in transparent regions (the acrylic-wash leak).
void main() {
    fragColor = textureLod(framebuffer, fragTexCoord, lod);
}
