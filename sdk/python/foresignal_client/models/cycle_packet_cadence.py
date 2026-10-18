from enum import StrEnum


class CyclePacketCadence(StrEnum):
    DAILY = "daily"
    MONTHLY = "monthly"
    ON_DEMAND = "on-demand"
    WEEKLY = "weekly"

    def __str__(self) -> str:
        return str(self.value)
