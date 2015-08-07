require 'set'

module Ogawa
  module Record
    module ClassMethods
      @@attrs = Set.new
      def read_attr(*names)
        names.each do |name|
          attr_reader name
          attrs << name
        end
      end

      def attrs
        @@attrs
      end
    end

    def self.included(base)
      base.send(:extend, ClassMethods)
    end

    def initialize(*args)
      mass_assign(*args)
    end

    def inspect
      vars = self.class.attrs.map {|attr|
        "#{attr}=#{self.instance_variable_get(:"@#{attr}")}"
      }.join(', ')
      "#<#{self.class} #{vars}>"
    end

    private
    def mass_assign(args)
      if args.is_a?(Hash)
        args.each do |k, v|
          if self.class.attrs.include?(k)
            self.instance_variable_set(:"@#{k}", v)
          end
        end
      end
    end
  end
end
