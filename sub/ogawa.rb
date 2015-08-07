#!/usr/bin/env ruby

require 'sinatra'
require 'net/http'
require 'slim'
require 'pp'
require 'fileutils'
require_relative 'env'
require_relative 'lib/post'
require_relative 'lib/db'

API_TOKEN = 'yvNVFcvQWZGrDZKWRuA786nhrj3BA35kHbJIDsukAb0='.freeze
URL = 'https://davar.icfpcontest.org/teams/59/solutions'.freeze
STORAGE_PATH = 'storage'.freeze

enable :sessions

if !File.exists?(STORAGE_PATH)
  FileUtils.mkdir(STORAGE_PATH)
end

def db
  @database ||= Ogawa::Database.new
end

get '/' do
  res = slim :index
  session[:posted] = nil
  res
end

post '/solution' do
  Net::HTTP.new('davar.icfpcontest.org', 443).tap{|h| h.use_ssl=true}.start do |http|
    req = Net::HTTP::Post.new('/teams/59/solutions')
    req['Content-Type'] = 'application/json'
    req.basic_auth '', API_TOKEN
    req.body = params['solution']
    res = http.request(req)
    if res.header.is_a?(Net::HTTPCreated)
      now = Time.now.strftime('%Y-%m-%d-%H%M%S')
      filename = "#{now}.json"
      File.open(File.join(STORAGE_PATH, filename), 'w') do |f|
        f.puts(Ogawa::Post.new(solution: params['solution'], comment: params['comment']).to_json)
      end
      session[:posted] = filename
      redirect to(Ogawa::ROOT)
    else
      raise Exception, res
    end
  end
end

get '/history' do
  @posts = db.read_post
  slim :list
end
