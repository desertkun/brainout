#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

void main()
{
	vec2 uv = v_texCoords.xy;
 
	vec4 col = texture2D(u_texture, uv);
	gl_FragColor = vec4(0, 0, 0, col.a);
}