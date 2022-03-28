;;; $DOOMDIR/config.el -*- lexical-binding: t; -*-

;; Place your private configuration here! Remember, you do not need to run 'doom
;; sync' after modifying this file!


;; Some functionality uses this to identify you, e.g. GPG configuration, email
;; clients, file templates and snippets.
(setq user-full-name "Bion"
      user-mail-address "bionjohnson@gmail.com")

;; Doom exposes five (optional) variables for controlling fonts in Doom. Here
;; are the three important ones:
;;
;; + `doom-font'
;; + `doom-variable-pitch-font'
;; + `doom-big-font' -- used for `doom-big-font-mode'; use this for
;;   presentations or streaming.
;;
;; They all accept either a font-spec, font string ("Input Mono-12"), or xlfd
;; font string. You generally only need these two:
;; (setq doom-font (font-spec :family "monospace" :size 12 :weight 'semi-light)
;;       doom-variable-pitch-font (font-spec :family "sans" :size 13))

;; There are two ways to load a theme. Both assume the theme is installed and
;; available. You can either set `doom-theme' or manually load a theme with the
;; `load-theme' function. This is the default:
(setq doom-theme 'doom-one)

;; If you use `org' and don't want your org files in the default location below,
;; change `org-directory'. It must be set before org loads!
(setq org-directory "~/org/")

;; This determines the style of line numbers in effect. If set to `nil', line
;; numbers are disabled. For relative line numbers, set this to `relative'.
(setq display-line-numbers-type t)

(define-key key-translation-map "\C-j" "\C-x")

(defun next-in-frame-window ()
  (interactive)
  (select-window (next-window)))

(defun previous-in-frame-window ()
  (interactive)
  (select-window (previous-window)))

(global-set-key (kbd "M-l") 'next-in-frame-window)
(global-set-key (kbd "M-h") 'previous-in-frame-window)
(global-set-key (kbd "M-o") 'indent-and-open-newline)
(global-set-key (kbd "M-x") 'smex)
(global-set-key (kbd "M-z") 'zap-up-to-char)
(global-set-key (kbd "M-X") 'smex-major-mode-commands)

(global-set-key (kbd "C-S-p") 'move-dup-move-lines-up)
(global-set-key (kbd "M-P") 'move-dup-duplicate-up)
(global-set-key (kbd "M-N") 'move-dup-duplicate-down)
(global-set-key (kbd "C-S-n") 'move-dup-move-lines-down)
(global-set-key (kbd "M-|") 'toggle-window-split)
(global-set-key (kbd "C-S-j") (lambda () (interactive) (join-line -1)))
(global-set-key (kbd "M-/") 'hippie-expand)
(global-set-key (kbd "C-c g l") 'git-link)

(add-to-list 'auto-mode-alist '("\\.scd\\'" . sclang-mode))
(add-to-list 'auto-mode-alist '("\\.sc\\'" . sclang-mode))

(setq-default indent-tabs-mode nil)

(defun endless/visit-pull-request-url ()
  "Visit the current branch's PR on Github."
  (interactive)
  (browse-url (format "https://github.com/%s/pull/new/%s"
                      (replace-regexp-in-string "\\`.+github\\.com:\\(.+\\)\\.git\\'" "\\1"
                                                (magit-get "remote"
                                                           (magit-get-push-remote)
                                                           "url"))
                      (magit-get-current-branch))))

(after! dired
  (map! "M-p" 'dired-sclang-preview-soundfile))

(defun dired-sclang-preview-soundfile ()
  (interactive)
  (sclang-preview-soundfile (dired-get-filename)))

(defun sclang-preview-soundfile (path)
  (let ((command (concat "~sampleBuffer.value(\"" path "\")")))
    (sclang-eval-string command)))

;; here are some additional functions/macros that could help you configure Doom:
;;
;; - `load!' for loading external *.el files relative to this one
;; - `use-package!' for configuring packages
;; - `after!' for running code after a package has loaded
;; - `add-load-path!' for adding directories to the `load-path', relative to
;;   this file. Emacs searches the `load-path' when you load packages with
;;   `require' or `use-package'.
;; - `map!' for binding new keys
;;
;; To get information about any of these functions/macros, move the cursor over
;; the highlighted symbol at press 'K' (non-evil users must press 'C-c c k').
;; This will open documentation for it, including demos of how they are used.
;;
;; You can also try 'gd' (or 'C-c c d') to jump to their definition and see how
;; they are implemented.
