This directory contains adaptations of ibnuda's dactyl keyboard generator, the first such web-based Dactyl configurator out there as far as I know.

General changes made to the config structure:
- Options such as whether to generate plates and left/right side are kept in the configs
- Use camelcase for better protobuf and JS interop
- Do not use integers (i.e. radians = pi/x where x is an integer) for angles! This allows for greater precision and flexibility in the frontend
