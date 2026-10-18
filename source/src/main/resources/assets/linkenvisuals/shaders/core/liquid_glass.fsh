#version 330

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

layout(std140) uniform LiquidGlass {
    vec2 size;
    vec4 radius;
    vec4 params1;
    vec4 params2;
    vec4 fresnelColor;
    vec2 backdropSize;
    float distortStrength;
    float fresnelInvert;
};

uniform sampler2D Sampler0;

const float TAU = 6.28318530718;
const float RING_STEP = TAU / 10.0;
const float STEP_SIZE = 1.0 / 3.0;

float roundedBoxSDF(vec2 p, vec2 b, vec4 r, float smoothness) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;

    vec2 q = abs(p) - b + r.x;
    vec2 q_clamped = max(q, 0.0);
    float len = pow(pow(q_clamped.x, smoothness) + pow(q_clamped.y, smoothness), 1.0 / smoothness);
    return min(max(q.x, q.y), 0.0) + len - r.x;
}

vec4 sampleScene(vec2 coord) {
    vec2 uv = clamp(coord, vec2(0.0), vec2(1.0));
    return texture(Sampler0, uv) * vertexColor;
}

vec4 blurScene(vec2 coord, float blurRadius, vec2 framebufferSize) {
    float radius = max(blurRadius, 0.0);
    if (radius < 0.001 || framebufferSize.x <= 0.0 || framebufferSize.y <= 0.0) {
        return sampleScene(coord);
    }

    vec2 invSize = vec2(1.0) / max(framebufferSize, vec2(1.0));
    vec4 accum = sampleScene(coord);
    float weightSum = 1.0;

    for (float angle = 0.0; angle < TAU; angle += RING_STEP) {
        vec2 direction = vec2(cos(angle), sin(angle));
        for (float step = STEP_SIZE; step <= 1.001; step += STEP_SIZE) {
            float ringWeight = 1.0 - 0.28 * step;
            vec2 offset = direction * (radius * step) * invSize;
            accum += sampleScene(coord + offset) * ringWeight;
            weightSum += ringWeight;
        }
    }

    return accum / weightSum;
}

void main() {
    float smoothness = params1.x;
    float cornerSmoothness = params1.y;
    float globalAlpha = params1.z;
    float blurRadius = params1.w;
    float fresnelPower = params2.x;
    float fresnelAlpha = params2.y;
    float baseAlpha = params2.z;
    float fresnelMix = params2.w;

    vec2 center = size * 0.5;
    vec2 box_half_size = center - 1.0;
    vec2 pos = (texCoord * size) - center;

    float distance = roundedBoxSDF(-pos, box_half_size, radius, cornerSmoothness);
    float gradient = fwidth(distance);
    if (!(gradient > 0.0)) {
        gradient = 0.0;
    }
    float aa = max(gradient * 0.5, 0.0001);
    float softness = max(smoothness * 0.5, 0.0);
    float alpha = smoothstep(-softness - aa, softness + aa, -distance);

    float distToEdge = abs(distance);

    float max_dist_norm = min(box_half_size.x, box_half_size.y);
    float edge_gradient = 1.0 - clamp(distToEdge / max_dist_norm, 0.0, 1.0);

    float base = fresnelInvert > 0.5 ? edge_gradient : (1.0 - edge_gradient);
    float fresnel;

    if (fresnelPower > 20.0) {
        fresnel = exp(fresnelPower * log(clamp(base, 0.001, 1.0)));
    } else {
        fresnel = pow(base, fresnelPower);
    }
    fresnel = clamp(fresnel, 0.0, 1.0);

    vec2 dir = vec2(0.0);
    float lenPos = length(pos);
    if (lenPos > 0.0001) {
        dir = pos / lenPos;
    }

    vec2 backdropUV = gl_FragCoord.xy / max(backdropSize, vec2(1.0));
    vec2 distortedUV = backdropUV + dir * fresnel * distortStrength;

    vec4 texColor = blurScene(distortedUV, blurRadius, backdropSize);

    vec3 finalColor = mix(texColor.rgb, fresnelColor.rgb, fresnel * fresnelMix);
    float finalAlpha = mix(baseAlpha, fresnelAlpha, fresnel) * alpha;

    if (finalAlpha < 0.001) {
        discard;
    }

    fragColor = vec4(finalColor, finalAlpha * globalAlpha);
}
