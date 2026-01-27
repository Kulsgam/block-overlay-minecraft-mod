#version 150

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
out vec4 fragColor;

float d(vec2 uv) { return texture(DepthSampler, uv).r; }

void main() {
    vec2 px = vec2(1.0) / textureSize(DepthSampler, 0);

    float c  = d(texCoord);
    float dx = abs(c - d(texCoord + vec2(px.x, 0.0)));
    float dy = abs(c - d(texCoord + vec2(0.0, px.y)));

    float edge = step(0.001, max(dx, dy));

    vec4 base = texture(InSampler, texCoord);
    fragColor = mix(base, vec4(1.0, 1.0, 1.0, 1.0), edge);
}
