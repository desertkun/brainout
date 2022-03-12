#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform vec2 screen;
uniform float radius;
uniform float time;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

#define ITERATIONS 128
#define RADIUS .1

//-------------------------------------------------------------------------------------------
void main()
{
	vec2 uv = v_texCoords.xy;

	vec3 sum = texture2D(u_texture, uv).xyz;

    for(int i = 0; i < ITERATIONS / 4; i++) {

        sum += texture2D(u_texture, uv + vec2(float(i) / screen.x, 0.) * RADIUS).xyz;

    }

    for(int i = 0; i < ITERATIONS / 4; i++) {

        sum += texture2D(u_texture, uv - vec2(float(i) / screen.x, 0.) * RADIUS).xyz;

    }

    for(int i = 0; i < ITERATIONS / 4; i++) {

        sum += texture2D(u_texture, uv + vec2(0., float(i) / screen.y) * RADIUS).xyz;

    }

    for(int i = 0; i < ITERATIONS / 4; i++) {

        sum += texture2D(u_texture, uv - vec2(0., float(i) / screen.y) * RADIUS).xyz;

    }

	vec4 clr = vec4(sum / float(ITERATIONS + 1), 1.);
	clr *= vec4(0.5, 0.5, 0.5, 1.0);
	gl_FragColor = clr;
}
