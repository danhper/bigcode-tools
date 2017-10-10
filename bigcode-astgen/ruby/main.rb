require_relative 'cli'
require_relative 'generator'

options = CLI.new.parse(ARGV)
runner = ASTGeneratorRunner.new(options)
puts runner.files
