This directory contains adaptations of ibnuda's dactyl keyboard generator, the first such web-based Dactyl configurator out there as far as I know.

## General changes made to the config structure:
- Options such as whether to generate plates and left/right side are kept in the configs
- Use camelcase for better protobuf and JS interop
- Use integers for angles, but space them out equally over the range of values! (i.e. radians = x/constant, not radians = pi/x where x is an integer)! This allows for greater precision and fewer bytes used.

## Angle conversion
Angles in the range [-π, π] are stored as integers from -8100 to 8100. For comparison, 2<pow>13</pow> = 8192. I choose 8100 because it's a multiple of 180, which means 1 degree can be converted to this angle format without any loss of accuracy.

These ranges are used because Protobuf can encode a 14 bit `sint32` to two bytes which is pretty compact! This gives a resolution of 0.022 degrees, which is barely noticeable to the human eye.
