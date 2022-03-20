#!/usr/bin/env bash

shared_dir="${BASH_SOURCE%/*}/shared"

SOURCED_SHARED_DOTFILES=${SOURCED_SHARED_DOTFILES:-"no"}

if [[ "$SOURCED_SHARED_DOTFILES" != "yes" ]]; then
  source "$shared_dir/file_stuff.sh"
  source "$shared_dir/os-detection.sh"
  source "$shared_dir/aptitude.sh"
  source "$shared_dir/stdout.sh"

  SOURCED_SHARED_DOTFILES="yes"
fi
