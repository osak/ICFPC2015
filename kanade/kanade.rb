#!/usr/bin/env ruby

require 'mongo'
require 'set'
require 'net/http'
require 'json'
require 'uri'
require 'pp'

Mongo::Logger.logger.level = ::Logger::INFO

class Kanade
  def initialize
    @client = Mongo::Client.new(['icfpc.osak.jp'], database: 'leaderboard')
    @collection = @client[:leaderboard]
    @keywords = []
    @using_prob = Set.new
    @keyword_prob = {}
    @known = Set.new
    @queue = []
    read_config
  end

  def poll
    @collection.find(_id: {'$gt': Time.now.to_i - 20*60}).each do |row|
      keyword = row['comment']
      if keyword && @keyword_prob[keyword] && row['power']
        if row['power'] >= 1
          @keywords << keyword
        end
        @known << keyword
        @keyword_prob.delete(keyword)
        @using_prob.delete(row['problem_id'])
      end
    end
    export_known
    export_keywords

    to_reject = []
    @queue.each do |keyword|
      IO.popen(['java', '-cp', '../ema/ema.jar:../ema/lib/*', 'icfpc.ema.Main', '-p', '../problems', :err => [:child, :out]], 'r+') do |io|
        io.puts(keyword)
        io.each_line do |line|
          m = line.match(/problem_(\d+).json, seed: (\d+)/)
          if m
            problem_id = m[1].to_i
            seed = m[2].to_i
            if !@using_prob.include?(problem_id) && (13..23).include?(problem_id)
              @using_prob << problem_id
              @keyword_prob[keyword] = problem_id
              post(problem_id, keyword)
              to_reject << keyword
              break
            end
          end
        end
      end
    end
    @queue.reject!{|k| to_reject.include?(k)}
  end

  def post(problem_id, keyword)
    solution = [{
      seed: 0,
      solution: keyword,
      problemId: problem_id
    }].to_json
    Net::HTTP.post_form(URI('http://icfpc.osak.jp/ogawa/solution'), {solution: solution, comment: keyword})
  end

  def export_known
    File.open('known', 'w') do |f|
      f.puts(@known.to_a)
    end
  end

  def export_keywords
    File.open('keywords', 'w') do |f|
      f.puts(@keywords.to_a)
    end
  end

  def read_config
    list = File.read('list').lines.map(&:chomp)
    @known = Set[*File.read('known').lines.map(&:chomp)]
    list.each do |keyword|
      if !@known.include?(keyword)
        puts "load word: #{keyword}"
        @queue << keyword
      end
    end
  end
end

kanade = Kanade.new
Dir.chdir(__dir__)
Signal.trap(:USR1) do
  kanade.read_config
end
Signal.trap(:USR2) do
  kanade.put_keyword
end

loop do
  kanade.poll
  sleep 30
end
