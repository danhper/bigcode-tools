require 'parser/current'


class ASTGeneratorRunner
  attr_reader :options
  attr_reader :files

  def initialize(options)
    @options = options
    @files = load_files
  end

  private

  def load_files
    if File.file?(options.input)
      [options.input]
    else
      Dir[File.join(options.input, "**/*.rb")]
    end
  end
end


class ASTGenerator
  attr_reader :filepath

  def initialize(filepath)
    @filepath = filepath
    @parsed_file = Parser::CurrentRuby.parse(File.read(@filepath))
  end
end
