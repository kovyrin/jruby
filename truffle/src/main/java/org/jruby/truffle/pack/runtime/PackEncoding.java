package org.jruby.truffle.pack.runtime;

public enum PackEncoding {
    DEFAULT,
    ASCII_8BIT,
    US_ASCII,
    UTF_8;

    public PackEncoding unifyWith(PackEncoding other) {
        if (this == DEFAULT) {
            return other;
        }

        if (other == DEFAULT) {
            return this;
        }

        switch (this) {
            case ASCII_8BIT:
            case US_ASCII:
                switch (other) {
                    case ASCII_8BIT:
                    case US_ASCII:
                        return ASCII_8BIT;
                    case UTF_8:
                        return ASCII_8BIT;
                    default:
                        throw new UnsupportedOperationException();
                }
            case UTF_8:
                switch (other) {
                    case ASCII_8BIT:
                    case US_ASCII:
                        return ASCII_8BIT;
                    case UTF_8:
                        return UTF_8;
                    default:
                        throw new UnsupportedOperationException();
                }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
