require 'optparse'

Options = Struct.new(:input, :output)

class CLI
  REQUIRED_ARGUMENTS = %w(input output).freeze

  attr_reader :args

  def initialize
    @args = Options.new
  end

  def parse(options)
    parse!(options)
    args
  rescue OptionParser::ParseError => e
    puts e.to_s
    puts opt_parser.help
    exit 1
  end

  def parse!(options)
    opt_parser.parse!(options)
    check_arguments!(args)
  end

  private

  def check_arguments!(args)
    missing_args = []
    REQUIRED_ARGUMENTS.each { |arg| missing_args << arg if args.send(arg).nil? }
    raise OptionParser::MissingArgument.new(missing_args.join(", ")) unless missing_args.empty?
    raise OptionParser::InvalidArgument, "input must be a directory or a file" unless File.file?(args.input) || File.directory?(args.input)
  end

  def opt_parser
    @opt_parser ||= OptionParser.new do |opts|
      opts.banner = "Usage: example.rb [options]"

      opts.on("-iINPUT", "--input=INPUT", "Directory or files to transform") do |i|
        args.input = i
      end

      opts.on("-oOUTPUT_DIR", "--output=OUTPUT_DIR", "Output directory for AST files") do |o|
        args.output = o
      end

      opts.on("-h", "--help", "Prints this help") do
        puts opts
        exit
      end
    end
  end
end
