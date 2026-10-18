from enum import StrEnum


class InfraRulesRulesItemId(StrEnum):
    AIRPORT_PORT_SERVES = "airport-port-serves"
    CABLE_LANDING = "cable-landing"
    IXP_FACILITY = "ixp-facility"
    LINE_SUBSTATION = "line-substation"
    PLANT_SUBSTATION = "plant-substation"
    SUBSTATION_CRITICAL_LOAD = "substation-critical-load"

    def __str__(self) -> str:
        return str(self.value)
