# examples

## Overview
Lightweight playground for testing and demonstrating technologies before spinning them into separate projects. Currently focused on Pulsar messaging and event-driven framework experiments.

## Scorecard

| Dimension | Rating | Notes |
|-----------|--------|-------|
| Build system | A | Gradle 9.4.0, consistent conventions |
| Code quality | B | Exploratory but well-structured |
| Test coverage | A- | 23 test files, project is primarily test-driven |
| Documentation | B+ | Clear README with build instructions |
| Dependency freshness | A | All current |
| Modularity | B | 2 modules — minimal for a playground |
| Maintainability | A | Small scope, easy to understand |

## Structure
- 2 modules: `pulsar/messaging`, `exercises/event-framework`
- Primarily test files (23 test files, 0 production files) — test-first exploration

## Issues
- SNAPSHOT dependencies for all internal libraries
- Configuration cache disabled
- Only 2 modules — could demonstrate more patterns
- No integration tests showing Pulsar end-to-end

## Potential Improvements
1. Add more example modules showcasing different architectural patterns
2. Add Pulsar integration tests (with Testcontainers)
3. Document the event framework pattern with architecture diagrams
4. Consider using this as a living cookbook/reference
