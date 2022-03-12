#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform vec2 screen;
uniform float time;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

void main()
{
	vec2 uv = v_texCoords.xy;

	float amount = 0.0;
	
	amount = sin(time * 3.1415);

	amount *= 4.0 / screen.x;
	//amount *= 0.5 + sin(uv.y * screen.y / 20.0) * 0.5;
	//amount *= 0.5 + sin(uv.y * screen.y / 12.0) * 0.5;
	//amount *= 0.5 + sin(uv.y * screen.y / 4.0) * 0.5;
	
	vec4 c = texture2D( u_texture, uv );

    vec3 col;

    col.r = texture2D( u_texture, vec2(uv.x+amount,uv.y) ).r;
    col.g = c.g;
    col.b = texture2D( u_texture, vec2(uv.x-amount,uv.y) ).b;

	col *= (1.0 - amount * 0.5);

	gl_FragColor = vec4(col, c.a);
}

