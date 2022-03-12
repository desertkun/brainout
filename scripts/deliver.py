
from anthill_tools.admin.dlc import deployer

deployer.deploy(
    "https://env.brainout.org",
    "brainout",
    "1.0",
    "brainout:desktop",
    "bundles.json",
    username="deliver",
    password="@bigbrother")
