#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float median(vec3 value) {
    return max(min(value.r, value.g), min(max(value.r, value.g), value.b));
}

void main() {
    vec3 field = texture(Sampler0, texCoord0).rgb;
    float distance = median(field) - 0.5;
    vec2 texelDerivative = vec2(dFdx(texCoord0.x), dFdy(texCoord0.y))
        * vec2(textureSize(Sampler0, 0));
    float pixelRange = 8.0 * inversesqrt(max(dot(texelDerivative, texelDerivative), 1.0e-8));
    float alpha = smoothstep(-0.5, 0.5, distance * pixelRange);
    if (alpha <= 0.001) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha) * ColorModulator;
}
