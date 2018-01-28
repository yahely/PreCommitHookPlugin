# Pre Commit Hook Plugin

IntelliJ plugin that allows you to run a hook prior commiting changes to any Version Control System. Good for Version Control Systems that doesn't allow you to run pre-commit-hook on the client side.

[See this if you're using git](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks).

## Usage

Create a file named `pre-commit-hook.sh` or `pre-commit-hook.bat` for Windows, in your project root, exit with non-zero code to 'fail' the commit.

Alternatively you can set custom path to your script:
1. Open Settings -> Tools -> Pre Commit Hook
2. Put a path to your script:
   - relative path to your project root (e.g. tools/my_hook.sh)
   - _or_ absolute path to your script (C://users/me/my_hook.bat)
   - _or_ set to empty for default file (pre-commit-hook)

## Known Problems

- MacOS : IntelliJ starts a new process with non-usual `PATH` environment variable. A bypass for now is to set the `PATH` within the script.
