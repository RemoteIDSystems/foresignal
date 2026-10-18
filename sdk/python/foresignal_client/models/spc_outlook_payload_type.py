from enum import StrEnum


class SpcOutlookPayloadType(StrEnum):
    CAT = "cat"
    HAIL = "hail"
    PROB = "prob"
    SIGHAIL = "sighail"
    SIGPROB = "sigprob"
    SIGTORN = "sigtorn"
    SIGWIND = "sigwind"
    TORN = "torn"
    WIND = "wind"

    def __str__(self) -> str:
        return str(self.value)
