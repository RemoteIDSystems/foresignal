from enum import StrEnum


class ListInfraNodesTypeItem(StrEnum):
    HEALTH_HOSPITAL = "health.hospital"
    POWER_GENERATION = "power.generation"
    POWER_LINE = "power.line"
    POWER_NUCLEAR = "power.nuclear"
    POWER_SUBSTATION = "power.substation"
    SPACE_SPACEPORT = "space.spaceport"
    TELECOM_CABLE = "telecom.cable"
    TELECOM_DATACENTRE = "telecom.datacentre"
    TELECOM_EXCHANGE = "telecom.exchange"
    TELECOM_IXP = "telecom.ixp"
    TELECOM_LANDING = "telecom.landing"
    TRANSPORT_AIRPORT = "transport.airport"
    TRANSPORT_PORT = "transport.port"
    WATER_TREATMENT = "water.treatment"

    def __str__(self) -> str:
        return str(self.value)
