from enum import StrEnum


class UpdateTokenBodyLicencePolicyType0Item(StrEnum):
    NO_NC = "no-nc"
    NO_SA = "no-sa"
    REDISTRIBUTABLE = "redistributable"

    def __str__(self) -> str:
        return str(self.value)
