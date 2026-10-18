#version 330

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform MotionBlur {
    float strength;
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

void main() {
    vec3 cur = texture(Sampler0, texCoord).rgb;
    vec3 prev = texture(Sampler1, texCoord).rgb;
    // Frame blend in LINEAR space (gamma-correct). Mixing directly in sRGB
    // gives dark, dirty mid-tones on contrast edges; converting both frames
    // to linear light, blending, and converting back keeps trails smooth.
    vec3 curLin = pow(cur, vec3(2.2));
    vec3 prevLin = pow(prev, vec3(2.2));
    vec3 mixed = mix(curLin, prevLin, strength);
    fragColor = vec4(pow(mixed, vec3(1.0 / 2.2)), 1.0);
}
