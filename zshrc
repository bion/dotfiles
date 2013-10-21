# Screw you, autocorrect.
unsetopt correct_all

eval `direnv hook zsh`

# OS X keys
bindkey '\e[3~' delete-char
bindkey '^R' history-incremental-search-backward
bindkey "^[[H" beginning-of-line
bindkey "^[[F" end-of-line

source ~/.zsh/aliases.zsh
source ~/.zsh/exports.zsh
source ~/.zsh/console_color.zsh

# number of lines kept in history
export HISTSIZE=1000
# number of lines saved in the history after logout
export SAVEHIST=1000
# location of history
export HISTFILE=~/.zhistory
# append command to history file once executed
setopt inc_append_history

autoload -U compinit
compinit -i

autoload -U promptinit
promptinit

zstyle ':completion::complete:*' use-cache 1

autoload colors zsh/terminfo
if [[ "$terminfo[colors]" -ge 8 ]]; then
  colors
fi
setopt prompt_subst

source ~/.zsh/spectrum.zsh

export PATH="/opt/local/bin:/opt/local/sbin:$PATH"
export MANPATH="/opt/local/share/man:$MANPATH"

git_prompt_info() {
  ref=$(git symbolic-ref HEAD 2> /dev/null) || return
  echo "(${ref#refs/heads/})"
}

export PATH="/usr/local/share/npm/lib/node_modules/coffee-script/bin/:$PATH"

# AWS toolkit config
export AWS_RDS_HOME="/Users/bionjohnson/dev/RDSCli-1.14.001"
export AWS_CREDENTIAL_FILE="$AWS_RDS_HOME/credential-file-path.conf"
export JAVA_HOME="/Library/Java/Home"
export PATH="$AWS_RDS_HOME/bin:$PATH"

export PGHOST=localhost

PROMPT='%{$FG[110]%}%n@%m %{$FG[170]%}%~%{$FG[192]%}%{$reset_color%} $(git_prompt_info)%{$reset_color%} %{$FG[113]%}%# %{$reset_color%}'

# get .local/bin in there
export PATH="$HOME/.local/bin:$PATH"

# RVM support.
[[ -s "$HOME/.rvm/scripts/rvm" ]] && source "$HOME/.rvm/scripts/rvm"

PATH=$PATH:$HOME/.rvm/bin # Add RVM to PATH for scripting

export PATH="/Users/bionjohnson/bin:$PATH"

### Added by the Heroku Toolbelt
export PATH="/usr/local/heroku/bin:$PATH"

export PATH="/usr/local/bin:$PATH"

