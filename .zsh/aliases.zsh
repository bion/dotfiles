function bgem() {
  cd `bundle list $1`
}

alias pg-start="pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start"
alias pg-stop="pg_ctl -D /usr/local/var/postgres stop -s -m fast"

alias tw="open -a /Applications/TextWrangler.app/"

alias ..="cd .."

alias ls="ls -G"

alias cf="cd ~/Documents/CodeFellows/"

alias lp="cd ~/Documents/lp/"

alias gz="cd ~/Documents/工作/"

alias ticketee="cd ~/Documents/CodeFellows/ticketee/"

alias br="bin/rspec"

alias vi="mvim"

alias psgrep="ps aux | grep $1"

alias 7="vim ~/.zshrc"

alias fautotest="AUTOFEATURE=true autotest"
alias be="bundle exec"
alias begs="be guard start"

alias testdb="RAILS_ENV=test be rake db:migrate"

function cdgem {
  cd $(bundle list $1)
}

alias mpgrep="grep -r --exclude .git,log,tmp"

alias ber="be rake"

