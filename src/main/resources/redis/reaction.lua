-- 인자 값 변수 할당
local reactionsKey = KEYS[1]
local likesCountKey = KEYS[2]
local dislikesCountKey = KEYS[3]

local memberId = ARGV[1]
local newReaction = ARGV[2]

-- 사용자의 현재 리액션 상태 조회
local currentReaction = redis.call('HGET', reactionsKey, memberId)

-- 1. 같은 리액션을 다시 클릭한 경우
if currentReaction == newReaction then
  redis.call('HDEL', reactionsKey, memberId)

  if newReaction == 'LIKE' then
    redis.call('DECR', likesCountKey)
  else
    redis.call('DECR', dislikesCountKey)
  end

-- 2. 다른 리액션으로 변경한 경우
elseif currentReaction then
  redis.call('HSET', reactionsKey, memberId, newReaction)

  if newReaction == 'LIKE' then
    redis.call('DECR', dislikesCountKey)
    redis.call('INCR', likesCountKey)
  else
    redis.call('DECR', likesCountKey)
    redis.call('INCR', dislikesCountKey)
  end

-- 3. 아무 리액션도 없던 경우
else
  redis.call('HSET', reactionsKey, memberId, newReaction)

  if newReaction == 'LIKE' then
    redis.call('INCR', likesCountKey)
  else
    redis.call('INCR', dislikesCountKey)
  end
end

-- 최종 좋아요/싫어요 수 조회
local finalLikes = redis.call('GET', likesCountKey) or 0
local finalDislikes = redis.call('GET', dislikesCountKey) or 0

-- 최종 카운트를 배열로 반환
return { finalLikes, finalDislikes }
