require 'mongo'

module Ogawa
  class Database
    def initialize
    end

    def read_post
      client[:post].find.each.to_a
    end

    private
    def client
      @client ||= Mongo::Client.new(['localhost'], database: 'ogawa')
    end
  end
end
