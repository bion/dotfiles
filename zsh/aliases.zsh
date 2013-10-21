function bgem() {
  cd `bundle list $1`
}

alias pg-start="pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start"
alias pg-stop="pg_ctl -D /usr/local/var/postgres stop -s -m fast"

alias tw="open -a /Applications/TextWrangler.app/"

alias psack="ps aux | ack $1"

alias ..="cd .."

alias ls="ls -G"

alias lp="cd ~/Documents/lp/"

alias gz="cd ~/Documents/工作/"

alias br="bin/rspec"

alias vi="mvim"

alias psgrep="ps aux | grep $1"

alias 7="vim ~/.zshrc"
alias 6="vim ~/.vimrc"

alias fautotest="AUTOFEATURE=true autotest"
alias be="bundle exec"
alias begs="be guard start"

alias testdb="RAILS_ENV=test be rake db:migrate"

function cdgem {
  cd $(bundle list $1)
}

alias mpack="ack -r --exclude .git,log,tmp"

alias ber="be rake"

alias pgstart="pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start"
alias pgstop="pg_ctl -D /usr/local/var/postgres stop -s -m fast"
