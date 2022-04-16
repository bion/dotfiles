#! /usr/bin/env bash

# Links files in a source dir into a target dir, destroys the latter first.
# Args:
#   1: source dir
#   2: target dir
function file_stuff::link_files_to_dir() {
  local source_dir="$(realpath "$1")"
  local target_dir="$(realpath "$2")"

  rm -rf "$target_dir"
  mkdir -p "$target_dir"

  for f in "${source_dir}"/*; do
    name="$(basename "$f")"
    dotsay + linking "$(basename $f)" to "${target_dir}/${name}"
    ln -s "$f" "${target_dir}/${name}"
  done
}
