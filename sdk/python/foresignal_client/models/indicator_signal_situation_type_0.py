from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_signal_situation_type_0_status_type_1 import (
    IndicatorSignalSituationType0StatusType1,
)
from ..models.indicator_signal_situation_type_0_status_type_2_type_1 import (
    IndicatorSignalSituationType0StatusType2Type1,
)
from ..models.indicator_signal_situation_type_0_status_type_3_type_1 import (
    IndicatorSignalSituationType0StatusType3Type1,
)
from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="IndicatorSignalSituationType0")


@_attrs_define
class IndicatorSignalSituationType0:
    """Situation status or severity

    Attributes:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (IndicatorSignalSituationType0StatusType1 | IndicatorSignalSituationType0StatusType2Type1 |
            IndicatorSignalSituationType0StatusType3Type1 | None | Unset):
        min_severity (None | Severity | Unset):
    """

    situation_id: str
    status: (
        IndicatorSignalSituationType0StatusType1
        | IndicatorSignalSituationType0StatusType2Type1
        | IndicatorSignalSituationType0StatusType3Type1
        | None
        | Unset
    ) = UNSET
    min_severity: None | Severity | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situation_id = self.situation_id

        status: None | str | Unset
        if isinstance(self.status, Unset):
            status = UNSET
        elif (
            isinstance(self.status, IndicatorSignalSituationType0StatusType1)
            or isinstance(self.status, IndicatorSignalSituationType0StatusType2Type1)
            or isinstance(self.status, IndicatorSignalSituationType0StatusType3Type1)
        ):
            status = self.status.value
        else:
            status = self.status

        min_severity: None | str | Unset
        if isinstance(self.min_severity, Unset):
            min_severity = UNSET
        elif isinstance(self.min_severity, Severity):
            min_severity = self.min_severity.value
        else:
            min_severity = self.min_severity

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "situationId": situation_id,
            }
        )
        if status is not UNSET:
            field_dict["status"] = status
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        situation_id = d.pop("situationId")

        def _parse_status(
            data: object,
        ) -> (
            IndicatorSignalSituationType0StatusType1
            | IndicatorSignalSituationType0StatusType2Type1
            | IndicatorSignalSituationType0StatusType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_1 = IndicatorSignalSituationType0StatusType1(data)

                return status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_2_type_1 = IndicatorSignalSituationType0StatusType2Type1(
                    data
                )

                return status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_3_type_1 = IndicatorSignalSituationType0StatusType3Type1(
                    data
                )

                return status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                IndicatorSignalSituationType0StatusType1
                | IndicatorSignalSituationType0StatusType2Type1
                | IndicatorSignalSituationType0StatusType3Type1
                | None
                | Unset,
                data,
            )

        status = _parse_status(d.pop("status", UNSET))

        def _parse_min_severity(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_0 = Severity(data)

                return min_severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        min_severity = _parse_min_severity(d.pop("minSeverity", UNSET))

        indicator_signal_situation_type_0 = cls(
            situation_id=situation_id,
            status=status,
            min_severity=min_severity,
        )

        indicator_signal_situation_type_0.additional_properties = d
        return indicator_signal_situation_type_0

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
