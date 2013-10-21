#! /bin/bash
# setup.sh

dir=~/dotfiles
files="bashrc vimrc vim zshrc"

cd $dir

for file in $files; do
  echo "Creating symlinks to $file in home directory."
  ln -s $dir/$file ~/.$file
done

chsh -s /bin/zsh $EUID
source ~/.zshrc
vi -c "BundleInstall"
