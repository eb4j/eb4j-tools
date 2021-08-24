# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

## [2.2.1] - 2021-08-24

### Fixed
- Fix appendix subcommand crashed, adjust native image builder configuration
- CI: Fix release automation crashed. 

## [2.2.0] - 2021-08-24

### Added
- Add appendix generation utility(#3)
- Support unicode extension(#4,#5)
- Provide Graalvm-native-image command(#7)

### Changed
- Spin out tools to its individual project
- Change license to GPL-3
- Use picocli for CLI options(#1)
- Support subcommand(#2,#6)

## [2.1.0] - 2021-03-10

### Added
- Extension: Support unicode maps definition file that bundled by EBView application.
- Extension: Support Appendix feature that EB library extend.
- Publish to jitpack.io.
- Publish to github packages repository.

### Fixed
- Able to handle monochrome image at HONMON instead of HONMONG

### Changed
- Automation of release versioning based on git tag.
- Change version scheme to <major>.<minor>.<patchlevel>.<build>
- Use Asciidoc for javadoc comment for Japanese and English.

### Deprecated
- Drop bintray repository to publish.

## [2.0.0] - 2020-10-25
### Changed
- Released the library at bintray
- Release automation
- Refactoring gradle build structures
- Fix tests(checkstyle).
- Bump to gradle@6.1.1
- Target java version to 8.
- Add github actions workflows

## 1.99.0.dev - 2016-06-01
### Added
- Import from eb4j-1.0.5

[Unreleased]: https://github.com/eb4j/eb4j/compare/v2.2.1...HEAD
[2.2.1]: https://github.com/eb4j/eb4j/compare/v2.2.0...v2.2.1
[2.2.0]: https://github.com/eb4j/eb4j/compare/v2.1.0...v2.2.0
