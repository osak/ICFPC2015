require 'mongo'

module Ogawa
  class Database
    def initialize
    end

    def read_post(from, to)
      client[:leaderboard].find(_id: {'$gt': from, '$lt': to}).limit(30).each.map {|post|
        Ogawa::Post.new(
          id: post['_id'],
          problem_id: post['problem_id'],
          power: post['power'],
          score: post['score'],
          tag: post['tag'],
          solution: post['solution'],
          comment: post['comment'],
        )
      }
    end

    def write_post(post)
      cur = client[:leaderboard].find(_id: post.id)
      if cur.count > 0
        cur.update_one(solution: post.solution, comment: post.comment, history: post.history)
      else
        client[:leaderboard].insert_one(_id: post.id, solution: post.solution, comment: post.comment, history: post.history)
      end
    end

    private
    def client
      @client ||= Mongo::Client.new(['localhost'], database: 'leaderboard')
    end
  end
end
