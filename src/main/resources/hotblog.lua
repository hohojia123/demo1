--key[1]=redis_hot_blog
--ARGV[1]=redis_hot_blog_count
--ARGV[2]=blog_id
local len=redis.call('LLEN',KEYS[1])
if tonumber(len)>=tonumber(ARGV[1]) then
    redis.call('RPOP',KEY[1])
end
redis.call('LPUSH',KEY[1],ARGV[2])
return 1