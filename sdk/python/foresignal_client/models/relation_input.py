from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.relation_input_label import RelationInputLabel
from ..models.relation_input_type import RelationInputType
from ..types import UNSET, Unset

T = TypeVar("T", bound="RelationInput")


@_attrs_define
class RelationInput:
    """An operator or agent relation; `causes` stays ≤ `possible` until an operator confirms

    Attributes:
        from_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        to_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (RelationInputType): Client-Contract §4 relation type
        valid_from (datetime.datetime | None | Unset):
        valid_to (datetime.datetime | None | Unset):
        confidence (float | Unset):  Default: 1.0.
        label (RelationInputLabel | Unset): Relation or dependency label (104 vocabulary)
        private (bool | Unset):  Default: False.
    """

    from_ref: str
    to_ref: str
    type_: RelationInputType
    valid_from: datetime.datetime | None | Unset = UNSET
    valid_to: datetime.datetime | None | Unset = UNSET
    confidence: float | Unset = 1.0
    label: RelationInputLabel | Unset = UNSET
    private: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ref = self.from_ref

        to_ref = self.to_ref

        type_ = self.type_.value

        valid_from: None | str | Unset
        if isinstance(self.valid_from, Unset):
            valid_from = UNSET
        elif isinstance(self.valid_from, datetime.datetime):
            valid_from = self.valid_from.isoformat()
        else:
            valid_from = self.valid_from

        valid_to: None | str | Unset
        if isinstance(self.valid_to, Unset):
            valid_to = UNSET
        elif isinstance(self.valid_to, datetime.datetime):
            valid_to = self.valid_to.isoformat()
        else:
            valid_to = self.valid_to

        confidence = self.confidence

        label: str | Unset = UNSET
        if not isinstance(self.label, Unset):
            label = self.label.value

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fromRef": from_ref,
                "toRef": to_ref,
                "type": type_,
            }
        )
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if label is not UNSET:
            field_dict["label"] = label
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ref = d.pop("fromRef")

        to_ref = d.pop("toRef")

        type_ = RelationInputType(d.pop("type"))

        def _parse_valid_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_from_type_0 = datetime.datetime.fromisoformat(data)

                return valid_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_from = _parse_valid_from(d.pop("validFrom", UNSET))

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

        confidence = d.pop("confidence", UNSET)

        _label = d.pop("label", UNSET)
        label: RelationInputLabel | Unset
        if isinstance(_label, Unset):
            label = UNSET
        else:
            label = RelationInputLabel(_label)

        private = d.pop("private", UNSET)

        relation_input = cls(
            from_ref=from_ref,
            to_ref=to_ref,
            type_=type_,
            valid_from=valid_from,
            valid_to=valid_to,
            confidence=confidence,
            label=label,
            private=private,
        )

        relation_input.additional_properties = d
        return relation_input

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
