# EB4j-tools

EPWING/Ebook access command line utilities.

## Commands

- `eb appendix`: utility to generate appendix dictionary data from YAML definition source.
- `eb dump`: print hex data from epwing/eb dictionary/book.
- `eb info`: print epwing/eb metadata information.
- `eb map`: generate YAML definition for `appendix` from EBWin map format file.
- `eb zip`: compress/decompress epwing/eb data (.ebz). 

## Description

### Appendix

Appendix is a extension that provide extended data of EPWING/EB dictionaries/books.
EB library(C) and EB4J library recognize and load appendix data and process EPWING/EB data properly.
It defines **end-of-article** mark code(stop-code), character set (ISO-8859-1 or JIS-X-0208), and **GAIJI**.
eb4j-tools accept Unicode characters as alternation of GAIJI code where EB library only accept characters in EUC-JP character encoding.

### Info

`eb info` subcommand will show EPWING/EB book information as same as EB library's ebinfo command does.

### Zip

EB4J, EB library and some other EPWING reader applications such as GoldenDict and EBWin accept commpressed 
dictionary/book data with `eb zip` subcommand or `ebzip` command from EB library, which has extension `.ebz`.


## Build

EB4j uses Gradle for build system. You can build library and utilities
by typing command (in Mac/Linux/Unix):

```
$ ./gradlew build
```

or (in Windows):

```
C:> gradlew.bat build
```

You will find generated archive files at

```
build/distributions/eb4j-tools-<version>.tbz2
build/distributions/eb4j-tools-<version>.zip
```

## Contribution

As usual of other projects hosted on GitHub, we are welcome
forking source and send modification as a Pull Request.
It is recommended to post an issue before sending a patch,
and share your opinions and/or problems.


## Copyrights and License

EB4j-tools: EPWING/Ebook handling utilities

Copyright(C) 1997-2006 Motoyuki Kasahara

Copyright(C) 2003-2010 Hisaya FUKUMOTO

Copyright(C) 2016 Hiroshi Miura, Aaron Madlon-Kay

Copyright(C) 2020-2021 Hiroshi Miura

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
