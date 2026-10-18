from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="EmwinWarningPayloadDuplicateOfType0")


@_attrs_define
class EmwinWarningPayloadDuplicateOfType0:
    """Matching nws-alerts record while it is healthy

    Attributes:
        source (Literal['nws-alerts']):
        external_id (str):
    """

    source: Literal["nws-alerts"]
    external_id: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source

        external_id = self.external_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
                "externalId": external_id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source = cast(Literal["nws-alerts"], d.pop("source"))
        if source != "nws-alerts":
            raise ValueError(f"source must match const 'nws-alerts', got '{source}'")

        external_id = d.pop("externalId")

        emwin_warning_payload_duplicate_of_type_0 = cls(
            source=source,
            external_id=external_id,
        )

        emwin_warning_payload_duplicate_of_type_0.additional_properties = d
        return emwin_warning_payload_duplicate_of_type_0

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
