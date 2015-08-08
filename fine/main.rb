#!/usr/bin/env ruby

require 'mongo'
require 'json'

client = Mongo::Client.new(['icfpc.osak.jp'], database: 'kadingel')

workdir = ARGV[0]
revision = ARGV[1]
comment = ARGV[2]
run_date_utc = ARGV[3]

Dir.glob(File.join(workdir, 'output', '*.json')) do |filename|
  json = JSON.parse(File.read(filename))
  client[:output].insert_one(
    raw: json,
    revision: revision,
    problemId: json.first['problemId'],
    comment: comment,
    runDateUtc: run_date_utc
  )
end

Dir.glob(File.join(workdir, 'visdump', '*.json')) do |filename|
  problem_id = filename.match(/\d+/)[1].to_i
  json = JSON.parse(File.read(filename))
  client[:vis].insert_one(
    raw: json,
    revision: revision,
    problemId: problem_id
  )
end
