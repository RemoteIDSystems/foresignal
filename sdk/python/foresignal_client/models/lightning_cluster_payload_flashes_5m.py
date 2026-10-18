from enum import IntEnum


class LightningClusterPayloadFlashes5M(IntEnum):
    VALUE_3 = 3
    VALUE_5 = 5
    VALUE_10 = 10
    VALUE_20 = 20
    VALUE_50 = 50
    VALUE_100 = 100
    VALUE_200 = 200

    def __str__(self) -> str:
        return str(self.value)
