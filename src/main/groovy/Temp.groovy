def matcher = ('up and down, east and west, left and right' =~ /(\w+) and (\w+)/)
assert matcher.size() == 3
assert matcher.hasGroup()
assert matcher.groupCount() == 2

assert ['up and down', 'up', 'down'] == matcher[0]
assert ['east and west', 'east', 'west'] == matcher[1]

assert 'up and down' == matcher[0][0]
assert 'up' == matcher[0][1]
assert 'down' == matcher[0][2]
assert 'west' == matcher[1][2]
assert 'left' == matcher[2][1]
