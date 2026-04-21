Output docs/demo.gif

# ── Terminal appearance ───────────────────────────────────────────────────────
Set Width     1100
Set Height     660
Set FontSize    13
Set FontFamily "JetBrains Mono"
Set Theme      "Dracula"
Set Padding     24
Set Framerate   22

# ── Shell prompt ─────────────────────────────────────────────────────────────
Env PS1 "$ "
Env TERM "xterm-256color"

# ── Run demo ─────────────────────────────────────────────────────────────────
Sleep 300ms
Type "bash docs/demo.sh"
Sleep 200ms
Enter
# demo.sh runtime ≈ 71s (all section sleep() calls combined)
Sleep 72s
