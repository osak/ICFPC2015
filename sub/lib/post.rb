require 'json'
require_relative 'record'

module Ogawa
  class Post
    include Ogawa::Record
    read_attr :id, :problem_id, :power, :score, :tag, :solution, :comment

    def initialize(*args)
      super(*args)
      @id = id
      @solution = solution
      @comment = comment
    end

    def to_json(*a)
      {_id: id, solution: solution, comment: comment}.to_json(*a)
    end
  end
end
