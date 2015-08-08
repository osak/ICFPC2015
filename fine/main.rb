#!/usr/bin/env ruby

require 'sinatra'
require 'fileutils'
require 'mongo'
require 'slim'
require 'pp'

Dir.chdir(File.dirname(__FILE__))
if !File.exists?('uploads')
  FileUtils.mkdir('uploads')
end

def db
  @client ||= Mongo::Client.new(['localhost'], database: 'kadingel')
  @collection ||= @client[:submit]
end

get '/' do
  slim :index
end

post '/upload' do
  timestamp = Time.now
  timestr = timestamp.strftime('%Y-%m-%d_%H%M%S')
  pp params
  filename_org = params['file'][:filename]
  file = params['file'][:tempfile]
  tag = params['tag']
  filename = File.join('uploads', "#{timestr}-#{filename_org}")
  FileUtils.mv(file, filename)
  db.insert_one(_id: timestamp.to_i, filename: filename, tag: tag)
  
  redirect to('/')
end
