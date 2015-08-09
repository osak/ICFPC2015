#!/usr/bin/env ruby

require 'mongo'
require 'json'
require 'pp'

def log(s)
  STDERR.puts "[LOG] #{s}"
end

def error(s)
  STDERR.puts "[ERROR] #{s}"
end

CHUNK_SIZE = 1000

Mongo::Logger.logger.level = ::Logger::INFO
client = Mongo::Client.new(['172.31.37.112'], database: 'kadingel')
#client = Mongo::Client.new(['localhost'], database: 'kadingel')

workdir = ARGV[0]
revision = ARGV[1]
comment = ARGV[2]
run_date_utc = ARGV[3]
target_prob = ARGV[4] && ARGV[4].to_i

(0..24).each do |probid|
  if target_prob && probid != target_prob
    next
  end
  log "reading problem #{probid}"
  visfile = File.join(workdir, 'visdump-simple', "problem_#{probid}.json")
  outfile = File.join(workdir, 'output', "problem_#{probid}.json")
  if !File.exists?(visfile) || !File.exists?(outfile)
    error("skip #{probid} because of file absense")
    next
  end

  log "loading visfile for #{probid}"
  vis_json = JSON.parse(File.read(visfile))
  log "\t#{vis_json.size} solutions"
  score_dict = Hash.new
  vis_json.each do |vis|
    seed = vis['settings']['initialSeed']
    score_dict[seed] = vis['diffBoards'].last['s']
    vis['revision'] = revision
    vis['problemId'] = probid
    vis['comment'] = comment
    vis['seed'] = seed
    client[:vis].insert_one(vis)
  end

  log "loading outfile for #{probid}"
  output_json = JSON.parse(File.read(outfile))
  output_json.each do |out|
    score = score_dict[out['seed']]
    if score
      out['score'] = score
    end
    out['revision'] = revision
    out['problemId'] = probid
    out['comment'] = comment
    out['runDateUtc'] = run_date_utc
  end
  client[:output].insert_many(output_json)
end
