#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform vec2 screen;
uniform float value;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

void main()
{
    vec4 tc = texture2D(u_texture, v_texCoords.xy).rgba;

    // Ramp colors
    float target_c = dot(tc.rgb, vec3(0.299, 0.587, 0.114));

	gl_FragColor = vec4(mix(tc.r, target_c, value), mix(tc.g, target_c, value), mix(tc.b, target_c, value), tc.a);
}