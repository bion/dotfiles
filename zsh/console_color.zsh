set_background() {
  osascript -e "tell application \"iTerm\"
    set current_terminal to (current terminal)
    tell current_terminal
      set current_session to (current session)
      tell current_session
        set background color to \"$1\"
      end tell
    end tell
  end tell"
}

heroku_console() {
    ( set_background red )
    ( heroku run console $* )
}

alias hc='heroku_console --app mediapiston-cedar'
