from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EntityLink")


@_attrs_define
class EntityLink:
    """Time-bounded link from a source record to an entity, with the key that produced it

    Attributes:
        source_id (str):  Example: adsblol-mil.
        external_id (str):
        key_type (str): Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
        key_value (str):
        confidence (float):
        valid_from (datetime.datetime):
        valid_to (datetime.datetime | None | Unset): null while the link is open
        first_seq (int | Unset): seq of the first linked version
    """

    source_id: str
    external_id: str
    key_type: str
    key_value: str
    confidence: float
    valid_from: datetime.datetime
    valid_to: datetime.datetime | None | Unset = UNSET
    first_seq: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        external_id = self.external_id

        key_type = self.key_type

        key_value = self.key_value

        confidence = self.confidence

        valid_from = self.valid_from.isoformat()

        valid_to: None | str | Unset
        if isinstance(self.valid_to, Unset):
            valid_to = UNSET
        elif isinstance(self.valid_to, datetime.datetime):
            valid_to = self.valid_to.isoformat()
        else:
            valid_to = self.valid_to

        first_seq = self.first_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "externalId": external_id,
                "keyType": key_type,
                "keyValue": key_value,
                "confidence": confidence,
                "validFrom": valid_from,
            }
        )
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to
        if first_seq is not UNSET:
            field_dict["firstSeq"] = first_seq

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        key_type = d.pop("keyType")

        key_value = d.pop("keyValue")

        confidence = d.pop("confidence")

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        def _parse_valid_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_to_type_0 = datetime.datetime.fromisoformat(data)

                return valid_to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_to = _parse_valid_to(d.pop("validTo", UNSET))

        first_seq = d.pop("firstSeq", UNSET)

        entity_link = cls(
            source_id=source_id,
            external_id=external_id,
            key_type=key_type,
            key_value=key_value,
            confidence=confidence,
            valid_from=valid_from,
            valid_to=valid_to,
            first_seq=first_seq,
        )

        entity_link.additional_properties = d
        return entity_link

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
