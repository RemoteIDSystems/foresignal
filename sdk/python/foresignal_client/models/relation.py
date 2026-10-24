from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.relation_label import RelationLabel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.relation_provenance import RelationProvenance


T = TypeVar("T", bound="Relation")


@_attrs_define
class Relation:
    """Time-aware, provenance-aware edge between two objects (Client-Contract §4)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        from_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        to_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (str): Client-Contract §4 relation type. Expected to grow (`x-extensible-enum`): a client must accept a
            value not listed here.
        valid_from (datetime.datetime):
        confidence (float):
        label (RelationLabel): Relation or dependency label (104 vocabulary)
        provenance (RelationProvenance): Who asserted a relation: a component and manifest, or an operator
        projected (bool): Materialised by `RelationProjector` (not editable)
        created_at (datetime.datetime):
        valid_to (datetime.datetime | None | Unset):
        private (bool | Unset):
        retired_at (datetime.datetime | None | Unset):
    """

    id: str
    from_ref: str
    to_ref: str
    type_: str
    valid_from: datetime.datetime
    confidence: float
    label: RelationLabel
    provenance: RelationProvenance
    projected: bool
    created_at: datetime.datetime
    valid_to: datetime.datetime | None | Unset = UNSET
    private: bool | Unset = UNSET
    retired_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        from_ref = self.from_ref

        to_ref = self.to_ref

        type_ = self.type_

        valid_from = self.valid_from.isoformat()

        confidence = self.confidence

        label = self.label.value

        provenance = self.provenance.to_dict()

        projected = self.projected

        created_at = self.created_at.isoformat()

        valid_to: None | str | Unset
        if isinstance(self.valid_to, Unset):
            valid_to = UNSET
        elif isinstance(self.valid_to, datetime.datetime):
            valid_to = self.valid_to.isoformat()
        else:
            valid_to = self.valid_to

        private = self.private

        retired_at: None | str | Unset
        if isinstance(self.retired_at, Unset):
            retired_at = UNSET
        elif isinstance(self.retired_at, datetime.datetime):
            retired_at = self.retired_at.isoformat()
        else:
            retired_at = self.retired_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "fromRef": from_ref,
                "toRef": to_ref,
                "type": type_,
                "validFrom": valid_from,
                "confidence": confidence,
                "label": label,
                "provenance": provenance,
                "projected": projected,
                "createdAt": created_at,
            }
        )
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to
        if private is not UNSET:
            field_dict["private"] = private
        if retired_at is not UNSET:
            field_dict["retiredAt"] = retired_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.relation_provenance import RelationProvenance

        d = dict(src_dict)
        id = d.pop("id")

        from_ref = d.pop("fromRef")

        to_ref = d.pop("toRef")

        type_ = d.pop("type")

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        confidence = d.pop("confidence")

        label = RelationLabel(d.pop("label"))

        provenance = RelationProvenance.from_dict(d.pop("provenance"))

        projected = d.pop("projected")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

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

        private = d.pop("private", UNSET)

        def _parse_retired_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                retired_at_type_0 = datetime.datetime.fromisoformat(data)

                return retired_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        retired_at = _parse_retired_at(d.pop("retiredAt", UNSET))

        relation = cls(
            id=id,
            from_ref=from_ref,
            to_ref=to_ref,
            type_=type_,
            valid_from=valid_from,
            confidence=confidence,
            label=label,
            provenance=provenance,
            projected=projected,
            created_at=created_at,
            valid_to=valid_to,
            private=private,
            retired_at=retired_at,
        )

        relation.additional_properties = d
        return relation

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
