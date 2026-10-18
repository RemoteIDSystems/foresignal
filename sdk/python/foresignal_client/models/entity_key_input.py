from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.entity_key_input_key_type import EntityKeyInputKeyType
from ..types import UNSET, Unset

T = TypeVar("T", bound="EntityKeyInput")


@_attrs_define
class EntityKeyInput:
    """Operator assertion of a key on an entity

    Attributes:
        key_type (EntityKeyInputKeyType):
        key_value (str): Raw value; normalised and validated like observed keys (400 ENTITY_KEY_INVALID)
        confidence (float | Unset):  Default: 1.0.
        valid_from (datetime.datetime | Unset): Default now
        valid_to (datetime.datetime | None | Unset):
        force (bool | Unset): Close the key where it is open on another entity and open it here; without it that case is
            409 ENTITY_KEY_CONFLICT Default: False.
    """

    key_type: EntityKeyInputKeyType
    key_value: str
    confidence: float | Unset = 1.0
    valid_from: datetime.datetime | Unset = UNSET
    valid_to: datetime.datetime | None | Unset = UNSET
    force: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        key_type = self.key_type.value

        key_value = self.key_value

        confidence = self.confidence

        valid_from: str | Unset = UNSET
        if not isinstance(self.valid_from, Unset):
            valid_from = self.valid_from.isoformat()

        valid_to: None | str | Unset
        if isinstance(self.valid_to, Unset):
            valid_to = UNSET
        elif isinstance(self.valid_to, datetime.datetime):
            valid_to = self.valid_to.isoformat()
        else:
            valid_to = self.valid_to

        force = self.force

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "keyType": key_type,
                "keyValue": key_value,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to
        if force is not UNSET:
            field_dict["force"] = force

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        key_type = EntityKeyInputKeyType(d.pop("keyType"))

        key_value = d.pop("keyValue")

        confidence = d.pop("confidence", UNSET)

        _valid_from = d.pop("validFrom", UNSET)
        valid_from: datetime.datetime | Unset
        if isinstance(_valid_from, Unset):
            valid_from = UNSET
        else:
            valid_from = datetime.datetime.fromisoformat(_valid_from)

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

        force = d.pop("force", UNSET)

        entity_key_input = cls(
            key_type=key_type,
            key_value=key_value,
            confidence=confidence,
            valid_from=valid_from,
            valid_to=valid_to,
            force=force,
        )

        entity_key_input.additional_properties = d
        return entity_key_input

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
