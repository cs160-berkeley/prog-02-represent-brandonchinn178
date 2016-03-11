import json

with open('election-county-2012.json', 'r') as f:
    results = json.load(f)

final = {}
for result in results:
    county = result['county-name'] + ' County'
    key = '%s, %s' % (county, result['state-postal'])
    final[key] = {
        'obama': result['obama-percentage'],
        'romney': result['romney-percentage']
    }

with open('App/mobile/src/main/res/raw/election_results_2012.json', 'w') as f:
    json.dump(final, f)

print 'done.'
