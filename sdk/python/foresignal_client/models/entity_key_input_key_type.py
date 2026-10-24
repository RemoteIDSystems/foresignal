from enum import StrEnum


class EntityKeyInputKeyType(StrEnum):
    AIRFRAME = "airframe"
    CALLSIGN = "callsign"
    COMPANY_NUMBER = "company-number"
    COSPAR = "cospar"
    ICAO24 = "icao24"
    IMO = "imo"
    LEI = "lei"
    MMSI = "mmsi"
    NORAD = "norad"
    OFFICER_ID = "officer-id"
    PERSON_KEY = "person-key"
    REGISTRATION = "registration"
    TAX_ID = "tax-id"

    def __str__(self) -> str:
        return str(self.value)
