from enum import StrEnum


class OwnershipChainNodePathsItemRelationTypesItem(StrEnum):
    AFFILIATED_WITH = "affiliated-with"
    BENEFICIAL_OWNER = "beneficial-owner"
    CONTROLS = "controls"
    OFFICER_OF = "officer-of"
    OWNS = "owns"

    def __str__(self) -> str:
        return str(self.value)
