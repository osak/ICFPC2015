#!/usr/bin/env ruby

require 'sinatra'
require 'net/http'
require 'slim'
require 'pp'
require 'fileutils'
require 'time'
require_relative 'env'
require_relative 'lib/post'
require_relative 'lib/db'

Mongo::Logger.logger.level = ::Logger::INFO
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
    timestamp = Time.now.to_i
    json_org = params['solution']
    json_post = JSON.parse(params['solution'])
    json_post[0]['tag'] = timestamp.to_s

    req = Net::HTTP::Post.new('/teams/59/solutions')
    req['Content-Type'] = 'application/json'
    req.basic_auth '', API_TOKEN
    req.body = json_post.to_json
    pp req.body
    res = http.request(req)

    if res.header.is_a?(Net::HTTPCreated)
      db.write_post(Ogawa::Post.new(
        id: timestamp,
        solution: json_org,
        comment: params['comment'],
        history: params['history']
      ))
      session[:posted] = timestamp
      redirect to(Ogawa::ROOT)
    else
      raise Exception, res
    end
  end
end

get '/history' do
  from = params['from'] && Time.strptime(params['from'], '%Y-%m-%d %H:%M:%S')
  to = params['to'] && Time.strptime(params['to'], '%Y-%m-%d %H:%M:%S')
  pp from
  pp to
  @posts = db.read_post(from.to_i, to.to_i)
  slim :list
end
