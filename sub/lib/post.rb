require 'json'

module Ogawa
  class Post
    attr_reader :solution, :comment

    def initialize(solution: nil, comment: nil)
      @solution = solution
      @comment = comment
    end

    def to_json(*a)
      {solution: solution, comment: comment}.to_json(*a)
    end
  end
end
