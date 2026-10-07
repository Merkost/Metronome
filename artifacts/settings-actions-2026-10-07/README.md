# Backup actions and leading colour alignment

Backup actions used full-width CircleShape outlined cards containing bare Text, leaving the labels at the curved edge with no content padding. The shared replacement groups Export backup and Import backup into two padded icon rows with short purposes, an inset divider, a full-row button target, and spring colour feedback. Native file pickers remain native; browser clipboard actions retain Copy backup and Paste backup wording. The existing enabled/busy gates, import/export I/O, messages and cancellation handling are unchanged.

Review caught disabled text retaining full contrast because bundled onSurfaceVariant and onSurface roles are identical. Disabled titles, subtitles, icons and chevrons now fade to the disabled foreground. Chevron direction mirrors in RTL. Text wraps naturally at larger sizes, while busy/disabled feedback preserves all control and following-content bounds.

The colour choices now align to the leading content edge, which is left in LTR and right in RTL, rather than centring a short swatch row. Selected palette and appearance transitions retain their existing stable typography and geometry.

## Verified results

- The initial six alignment tests failed with 26px extra offset at narrow width and 146px at wide width.
- After aligning the row, all fourteen appearance cases passed. A pixel test then reproduced the initial backup-row disabled state retaining identical Export text ink: 403.0673 before and after disabling.
- Final source: 212 native iOS simulator tests and 182 Android shared tests passed with zero failures, errors or skips. Thirty native rendering cases include six new alignment cases and four new backup-action cases.
- Android debug build, iOS arm64 source compilation and Wasm compilation passed. Diff checks passed; review evidence is in review.md.
- Backup cases check distinct labelled buttons, one callback per tap, disabled/busy blocking and recovery, visible foreground dimming, stable bounds, and complete text at narrow width with 200% text in LTR/dark RTL.
- Palette cases check exact leading-edge alignment and stable selection bounds at narrow/wide widths, LTR/RTL and 200% text.

The seven PNGs are native raster fixtures of production components: normal/dark RTL backup rows, 200% text in both directions, disabled content, and narrow/wide colour choices. Temporary capture helpers are absent from committed test source. These results do not establish a new end-to-end document-provider or browser-clipboard acceptance run; their original operation closures were preserved.
