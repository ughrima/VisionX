precision mediump float;
varying vec2 v_TexCoord;
uniform sampler2D u_Texture;
uniform int u_Mode; // 0 = normal, 1 = grayscale, 2 = invert

void main() {
    vec4 color = texture2D(u_Texture, v_TexCoord);

    if (u_Mode == 1) {
        float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
        gl_FragColor = vec4(vec3(gray), color.a);
    } else if (u_Mode == 2) {
        gl_FragColor = vec4(vec3(1.0) - color.rgb, color.a);
    } else {
        gl_FragColor = color;
    }
}
