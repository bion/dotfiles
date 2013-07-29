execute pathogen#infect()
call pathogen#helptags()
call pathogen#incubate()

color vividchalk

set guifont=Monaco:h14

" mad undooo & search history
set history=1000
set undolevels=1000

set autoindent
set shiftwidth=2
set softtabstop=2
set expandtab

" goddamn swapfiles
set noswapfile
set nobackup

" boooooooo vi! 
set nocompatible

syntax on

" KEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEYBOARD MAPPINGS

let mapleader=","

nnoremap <silent> <Leader>+ :exe "resize" . (winheight(0) * 3/2)<CR>
nnoremap <silent> <Leader>- :exe "resize" . (winheight(0) * 2/3)<CR>

nnoremap <leader>n :NERDTreeClose<CR>:NERDTreeToggle<CR>
nnoremap <leader>m :NERDTreeClose<CR>:NERDTreeFind<CR>
nnoremap <leader>N :NERDTreeClose<CR>

" flush commandT
nnoremap <leader>f :CommandTFlush<CR>

" toggle those <-------- things
nnoremap <leader>5 :NumbersToggle<CR>

" fast escape
inoremap jk <esc>

filetype on
filetype plugin on
filetype indent on

set hlsearch

set wildmenu

set incsearch

set colorcolumn=100

if (!&relativenumber)
	set number
end
set numberwidth=5

set ruler

set t_Co=256
set background=dark

" better autocomplete menu color
highlight Pmenu ctermbg=238 gui=bold

" highlight weird chars
set list
set listchars=tab:▸\ ,trail:ـ,extends:➧,eol:¬

set linebreak

aug filetype_ruby
  au!

  " Set tab sizes
  au FileType ruby set shiftwidth=2 softtabstop=2

" Comment out a line
  au FileType ruby :nnoremap <buffer> <localleader>c I#

  " Create a method
  au FileType ruby :iabbrev <buffer> defm def<cr>end<up>

  " Create an if block
  au FileType ruby :iabbrev <buffer> ifs if<cr>end<up>

  " Create an if/else block
  au FileType ruby :iabbrev <buffer> ife if<cr>else<cr>end<up><up>

  " Create an example group
  au FileType ruby :iabbrev <buffer> desce describe do<cr>end<up><right><right><right><right><right>

  " Create a spec
  au FileType ruby :iabbrev <buffer> ite it<cr>end<up><right>

  " These are Ruby files in disguise.
  au BufRead,BufNewFile *.ru,Gemfile,Guardfile set filetype=ruby

  " Remove trailing spaces from Ruby files. God help me if I actually
  " want that trailing space.
  au BufWritePre *.rb :%s/\s\+$//e
aug END

" get that unnamed register up in the osx clipboard
set clipboard=unnamed

