import json

LEVELS = 69
SCORE = 200
INCR_ADD = 100

o = []
score = SCORE
score_add = INCR_ADD

for i in xrange(1, LEVELS):
	o.append({"score": score, "name": "LEVEL_" + str(i), "icon": "icon-level-" + str(i-1)})
	score += score_add
	score_add += INCR_ADD

print json.dumps(o)