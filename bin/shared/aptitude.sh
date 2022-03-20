#!/usr/bin/env bash

function apt::update() {
  ! is_linux && return 1

  sudo apt-get update
}

function apt::is_installed() {
  ! is_linux && return 1

  local package=$1

  dpkg -s "$package" >/dev/null 2>&1
}

function apt::install() {
  ! is_linux && return 1

  local package=$1

  if apt::is_installed "$package"; then
    dotsay "+ $package already installed... skipping."
  else
    sudo apt-get install -y "$package"
  fi
}
