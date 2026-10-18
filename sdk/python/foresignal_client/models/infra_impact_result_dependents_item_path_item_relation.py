from enum import StrEnum


class InfraImpactResultDependentsItemPathItemRelation(StrEnum):
    HOSTS = "hosts"
    INTERCONNECTS = "interconnects"
    LANDS_AT = "lands_at"
    SERVES = "serves"
    SUPPLIES_POWER = "supplies_power"
    TRANSMITS_POWER = "transmits_power"

    def __str__(self) -> str:
        return str(self.value)
