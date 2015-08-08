#!/usr/bin/env ruby

require 'mongo'
require 'json'

def log(s)
  puts "[LOG] #{s}"
end

def error(s)
  puts "[ERROR] #{s}"
end

CHUNK_SIZE = 1000

Mongo::Logger.logger.level = ::Logger::INFO
client = Mongo::Client.new(['172.31.34.41'], database: 'kadingel')

workdir = ARGV[0]
revision = ARGV[1]
comment = ARGV[2]
run_date_utc = ARGV[3]

(0..24).each do |probid|
  log "reading problem #{probid}"
  visfile = File.join(workdir, 'visdump', "problem_#{probid}.json")
  outfile = File.join(workdir, 'output', "problem_#{probid}.json")
  if !File.exists?(visfile) || !File.exists?(outfile)
    error("skip #{probid} because of file absense")
    next
  end

  log "loading visfile for #{probid}"
  vis_json = JSON.parse(File.read(visfile))
  seed = vis_json['settings']['initialSeed']
  score_dict = Hash.new
  vis_json['boards'].each_slice(CHUNK_SIZE).with_index do |boards, i|
    turn = CHUNK_SIZE * i
    docs = boards.each_with_index.map {|board, j|
      {
        board: board,
        revision: revision,
        problemId: probid,
        seed: seed,
        turn: turn + j,
      }
    }
    client[:vis].insert_many(docs)
    score_dict[seed] = {
      score: boards.last['score'],
      moveScore: boards.last['moveScore'],
      powerScore: boards.last['powerScore'],
      clearedRows: boards.last['clearedRows']
    }
  end

  log "loading outfile for #{probid}"
  output_json = JSON.parse(File.read(outfile))
  output_json.each do |out|
    score_spec = score_dict[out['seed'].to_i]
    if score_spec
      score_spec.each do |k, v|
        out[k.to_s] = v
      end
    end
    out['revision'] = revision
    out['problemId'] = probid
    out['comment'] = comment
    out['runDateUtc'] = run_date_utc
  end
  client[:output].insert_many(output_json)
end
