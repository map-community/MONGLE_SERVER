-- 인자 값 변수 할당
local reactionsKey = KEYS[1]
local likesCountKey = KEYS[2]
local dislikesCountKey = KEYS[3]
local rankingZSetKey = KEYS[4]

local memberId = ARGV[1]
local newReaction = ARGV[2]
local targetType = ARGV[3]
local targetId = ARGV[4]

-- 사용자의 현재 리액션 상태 조회
local currentReaction = redis.call('HGET', reactionsKey, memberId)
local likesDelta = 0
local dislikesDelta = 0

-- 1. 같은 리액션을 다시 클릭한 경우 (취소)
if currentReaction == newReaction then
  redis.call('HDEL', reactionsKey, memberId)

  if newReaction == 'LIKE' then
    redis.call('DECR', likesCountKey)
    likesDelta = -1
  else
    redis.call('DECR', dislikesCountKey)
    dislikesDelta = -1
  end

-- 2. 다른 리액션으로 변경한 경우
elseif currentReaction then
  redis.call('HSET', reactionsKey, memberId, newReaction)

  if newReaction == 'LIKE' then
    redis.call('DECR', dislikesCountKey)
    redis.call('INCR', likesCountKey)
    dislikesDelta = -1
    likesDelta = 1
  else -- 변경 후 DISLIKE
    redis.call('DECR', likesCountKey)
    redis.call('INCR', dislikesCountKey)
    likesDelta = -1
    dislikesDelta = 1
  end

-- 3. 아무 리액션도 없던 경우 (새로운 리액션)
else
  redis.call('HSET', reactionsKey, memberId, newReaction)

  if newReaction == 'LIKE' then
    redis.call('INCR', likesCountKey)
    likesDelta = 1
  else
    redis.call('INCR', dislikesCountKey)
    dislikesDelta = 1
  end
end

--댓글 랭킹 ZSET 업데이트
if targetType == 'COMMENT' then
    if likesDelta ~= 0 then
        redis.call('ZINCRBY', rankingZSetKey, likesDelta, targetId)
    end
end

-- 최종 좋아요/싫어요 수 조회
local finalLikes = tonumber(redis.call('GET', likesCountKey) or 0)
local finalDislikes = tonumber(redis.call('GET', dislikesCountKey) or 0)

-- 최종 카운트를 배열로 반환
return { finalLikes, finalDislikes }
