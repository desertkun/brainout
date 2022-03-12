#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

const float PI = 3.1415926535;

void main()
{
	float aperture = 178.0;
	float apertureHalf = 0.5 * aperture * (PI / 180.0);
	float maxFactor = sin(apertureHalf);

	vec2 uv = v_texCoords;

	if (uv.x > 0.5)
	{
		vec2 xy = 2.0 * uv - 1.0;
		float d = clamp(length(xy), 0.0, 2.0-maxFactor);

		{
			d = length(xy * maxFactor);
			float z = sqrt(1.0 - d * d);
			float r = atan(d, z) / PI;
			float phi = atan(xy.y, xy.x);

			uv.x = r * cos(phi) + 0.5;
			uv.y = r * sin(phi) + 0.5;
		}
	}
	else
	{
		uv.x = asin((uv.x - 0.5) * 2.0) / 3.1415 + 0.5;
		uv.y = asin((uv.y - 0.5) * 2.0) / 3.1415 + 0.5;
	}

	gl_FragColor = texture2D(u_texture, uv);
}