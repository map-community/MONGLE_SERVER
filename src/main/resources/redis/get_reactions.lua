local memberId = ARGV[1]
local results = {}

for i, reactionsKey in ipairs(KEYS) do
    local reaction = redis.call('HGET', reactionsKey, memberId)
    table.insert(results, reaction)
end

return results