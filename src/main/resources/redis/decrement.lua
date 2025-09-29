local count = redis.call('DECR', KEYS[1])

if count < 0 then
    redis.call('SET', KEYS[1], '0')
    return 0
end

return count