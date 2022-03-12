#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

void main()
{
    vec4 tc = texture2D(u_texture, v_texCoords.xy).rgba;

    // Ramp colors
    float target_c = dot(tc.rgb, vec3(0.299, 0.587, 0.114)) * 0.5;
	gl_FragColor = vec4(target_c, target_c, target_c, tc.a);
}