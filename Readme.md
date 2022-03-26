# Brain / Out

Source code for [Brain / Out](https://brainout.org/), the game.

## How To Build From Source

First, clone this repo.

Use Gradle Wrapper: `./gradlew <command>` on mac/linux, or `gradlew.bat <command>` on windows

To obtain project, use `./gradlew idea` to generate [IntelliJ IDEA](https://www.jetbrains.com/idea/) project. Once project
   is generated, open `brainout.ipr` within IntelliJ IDEA IDE.

Then, 

1. `./gradlew desktop:dist` to build desktop client
2. `./gradlew steam:dist` to build steam client
3. `./gradlew server:dist` to build server

For either of those to work, Game Assets has to be built.

## Dependencies

On your build environment, for scripts to work, you need to install these

```
pip install git+https://github.com/desertkun/hjson-py
pip install pycrypto
```

Notes:
1. Command line git is required to install hjson
2. `pycrypto` on Windows could be replaced with `pip install pycryptodome` since original
pycrypto requires C development environment.

## How to build Game Assets

See [this readme](data/README.md) for how to change Game Assets.

To build them, call `./make_data.sh` on Mac/Linux and `make_data.bat` on Windows.

**NOTE**: the client digitally verifies the built data! So by default, the client
won't be able to start up with locally built data, since the private key is not present in this repository.
Pass `--unsafe` command line argument to the client so it would ignore non-signed packages.
`Offline Unsafe` IDEA configurations already do this.

## Offline build

By default, both the client and the server are trying to participate into the
Alpha game network. Since no keys are present in this repository, for locally run builds please pass
`--offline` command line argument both to the server and the client.
`Offline` IDEA configurations already do this.

## Run configurations

`Server Lobby` – server hosting "Main Menu". Offline version is offline, Alpha version participates
in the Alpha network.

`Server` – server hosting active game phase. Offline version is offline, Alpha version participates
in the Alpha network.

`Server Editor` – server hosting old non-steam editor. Offline version is offline, Alpha version participates
in the Alpha network. Important to note that client looks for Home User folder `brainout-maps` by default to upload 
any map, use `BRAINOUT_MAPS` environment variable to point client to `<this repo>/bin/server/maps`.

`Desktop Local Offline Unsafe` – a client that tries to connect to server on localhost. 
It uses `--connect brainout://bG9jYWxob3N0OzM2NTU1OzM2NTU2OzM2NTU3` argument
which is bas64-encoded `localhost;36555;36556;36557`

You might also see there's `Desktop Local 2 *` configurations, these are copies under different user,
e.g., you can launch those two in parallel and run two instances of the game on same PC.

`Desktop Alpha` – a client that tries to participate Alpha network.

## Testing

The project has a CI system in place: on every new commit, 
it builds a new version of client/server and deploys it automatically.
There are two environments available:

* `main`, which is built on every master commit
* `test`, which is built on every pull request. A new pull request wipes out the previous one,
  as it is intended to verify the correctness of the pull request.
* `dev`, which is build on every `dev_*` branch commit. Contributors with collaboration permissions
  could push there directly and skip PR requirements to check things quickly.

The clients of those builds are deployed on Dropbox, to get invited there,
please join the Dev Support telegram channel below and ask for invite link.

## Community

If you have development question, please join
[Dev Support](https://t.me/+f8ha1XpCJS1mZmUy) group on Telegram.

Otherwise, feel free to join the [Official Discord Community Server](https://discord.gg/eeqyjeG7P5)