List of External Holders for the Dactyl
====

While I personally don't use external holders, they are a popular way to securely fasten a microcontroller and connector to your keyboard, especially if you don't have access to a drill to widen the holes in the model.

If you're using one of these, make sure to set the connector type to *anything* but RJ9.

| Board                    | Model Downloads                                                       | Author                       |
|--------------------------|-----------------------------------------------------------------------|------------------------------|
| Pro Micro (micro USB) | [[left (mesh fixed)][pm-leftf]] [[left][pm-left]] [[right][pm-right]] | [Blue Ye (@yejianfengblue )] |
| Pro Micro (USB-C)     | [[left][pmc-left]] [[right][pmc-right]]                               | [Blue Ye (@yejianfengblue )] |
| Elite-C                  | [[right][ec-right]]                                                   | Unknown                      |

[pm-leftf]: https://github.com/yejianfengblue/dactyl-generator-demo/blob/main/stl/promicro-holder-v3-left-mesh-fixed.stl
[pm-left]: https://github.com/yejianfengblue/dactyl-generator-demo/blob/main/stl/promicro-holder-v3-left.stl
[pm-right]: https://github.com/yejianfengblue/dactyl-generator-demo/blob/main/stl/promicro-holder-v3-right.stl
[pmc-left]: https://github.com/yejianfengblue/dactyl-generator-demo/blob/main/stl/promicro-holder-typec-untested-left.stl
[pmc-right]: https://github.com/yejianfengblue/dactyl-generator-demo/blob/main/stl/promicro-holder-typec-untested-right.stl
[Blue Ye (@yejianfengblue )]: https://github.com/yejianfengblue
[ec-right]: https://web.archive.org/web/20220607031927/https://dactyl.siskam.link/loligagger-external-holder-elite-c-v1.stl

If you're looking to use a wireless microcontroller like the nice!nano, you can use the USB-C Pro Micro holder. However the nice!nano will fit loosely since it has a mid-mount USB-C port whereas the Pro Micro holder is designed for a top-mount port. You'll also have no need for the TRRS jack as the two halves will be wirelessly connected.  There is an [external holder](https://github.com/nathanielks/nice-nano-holder) by [@nathanielks](https://github.com/nathanielks) meant for the nice!nano that houses a power switch and reset button, but it is 4mm taller than the other holders. This makes it incompatible with the generator's models.

----

If you found one that isn't listed here, please submit a pull request! I'd like to make this list as complete as possible.
