# Pre Commit Hook Plugin

IntelliJ plugin that allows you to run a hook prior commiting changes to any Version Control System. Good for Version Control Systems that doesn't allow you to run pre-commit-hook on the client side.

[See this if you're using git](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks).

## Usage

Create a file named `pre-commit-hook.sh` in your project root, exit with non-zero code to 'fail' the commit.

## Known Problems

- MacOS : IntelliJ starts a new process with non-usual `PATH` environment variable. A bypass for now is to set the `PATH` within the script.
