@echo off
git init -b main
git add -A
git commit -m "FlipClock"
gh repo create flipclock --public --source=. --push
echo.
echo Build starts now (about 3 min). Then run:  gh release view latest --web
pause
