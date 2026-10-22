#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec2 quadCoord;
in vec4 vertexColor;
in float radiusPx;
in vec3 borderParams;

out vec4 fragColor;

void main() {
    vec2 pixels = 1.0 / max(fwidth(quadCoord), vec2(0.0001));
    vec2 point = (quadCoord - 0.5) * pixels;
    vec2 halfSize = pixels * 0.5;
    float radius = min(radiusPx, min(halfSize.x, halfSize.y));

    vec2 delta = abs(point) - halfSize + radius;
    float dist = length(max(delta, 0.0)) + min(max(delta.x, delta.y), 0.0) - radius;

    float thickness = max(borderParams.x, 0.5);
    float internalSmooth = max(borderParams.y, 0.5);
    float externalSmooth = max(borderParams.z, 0.5);

    float outer = 1.0 - smoothstep(-externalSmooth, externalSmooth, dist);
    float inner = 1.0 - smoothstep(-thickness - internalSmooth, -thickness + internalSmooth, dist);
    float ring = clamp(outer - inner, 0.0, 1.0);
    if (ring <= 0.001) {
        discard;
    }

    vec4 color = vertexColor * ColorModulator;
    fragColor = vec4(color.rgb, color.a * ring);
}
