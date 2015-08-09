#!/usr/bin/env ruby

require 'mongo'
require 'json'

def log(s)
  STDERR.puts "[LOG] #{s}"
end

def error(s)
  STDERR.puts "[ERROR] #{s}"
end

CHUNK_SIZE = 1000

Mongo::Logger.logger.level = ::Logger::INFO
client = Mongo::Client.new(['172.31.40.118'], database: 'kadingel')
#client = Mongo::Client.new(['localhost'], database: 'kadingel')

workdir = ARGV[0]
revision = ARGV[1]
comment = ARGV[2]
run_date_utc = ARGV[3]

(0..24).each do |probid|
  log "reading problem #{probid}"
  visfile = File.join(workdir, 'visdump-simple', "problem_#{probid}.json")
  outfile = File.join(workdir, 'output', "problem_#{probid}.json")
  if !File.exists?(visfile) || !File.exists?(outfile)
    error("skip #{probid} because of file absense")
    next
  end

  log "loading visfile for #{probid}"
  vis_json = JSON.parse(File.read(visfile))
  seed = vis_json['settings']['randomSeed']
  score = vis_json['diffBoards'].last['s']
  vis_json['revision'] = revision
  vis_json['problemId'] = probid
  vis_json['comment'] = comment
  vis_json['seed'] = seed
  client[:vis].insert_one(vis_json)

  log "loading outfile for #{probid}"
  output_json = JSON.parse(File.read(outfile))
  output_json.each do |out|
    if seed == out['initialSeed']
      out['score'] = score
    end
    out['revision'] = revision
    out['problemId'] = probid
    out['comment'] = comment
    out['runDateUtc'] = run_date_utc
  end
  client[:output].insert_many(output_json)
end
