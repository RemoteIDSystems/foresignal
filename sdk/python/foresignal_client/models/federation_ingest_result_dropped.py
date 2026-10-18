from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FederationIngestResultDropped")


@_attrs_define
class FederationIngestResultDropped:
    """Items dropped by reason

    Attributes:
        loop (int | Unset):
        hops (int | Unset):
        dedup (int | Unset):
        licence (int | Unset):
        private (int | Unset):
        exercise (int | Unset):
        expired (int | Unset):
        selection (int | Unset): Outside the selection, malformed, or of a kind the target source does not declare
    """

    loop: int | Unset = UNSET
    hops: int | Unset = UNSET
    dedup: int | Unset = UNSET
    licence: int | Unset = UNSET
    private: int | Unset = UNSET
    exercise: int | Unset = UNSET
    expired: int | Unset = UNSET
    selection: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        loop = self.loop

        hops = self.hops

        dedup = self.dedup

        licence = self.licence

        private = self.private

        exercise = self.exercise

        expired = self.expired

        selection = self.selection

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if loop is not UNSET:
            field_dict["loop"] = loop
        if hops is not UNSET:
            field_dict["hops"] = hops
        if dedup is not UNSET:
            field_dict["dedup"] = dedup
        if licence is not UNSET:
            field_dict["licence"] = licence
        if private is not UNSET:
            field_dict["private"] = private
        if exercise is not UNSET:
            field_dict["exercise"] = exercise
        if expired is not UNSET:
            field_dict["expired"] = expired
        if selection is not UNSET:
            field_dict["selection"] = selection

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        loop = d.pop("loop", UNSET)

        hops = d.pop("hops", UNSET)

        dedup = d.pop("dedup", UNSET)

        licence = d.pop("licence", UNSET)

        private = d.pop("private", UNSET)

        exercise = d.pop("exercise", UNSET)

        expired = d.pop("expired", UNSET)

        selection = d.pop("selection", UNSET)

        federation_ingest_result_dropped = cls(
            loop=loop,
            hops=hops,
            dedup=dedup,
            licence=licence,
            private=private,
            exercise=exercise,
            expired=expired,
            selection=selection,
        )

        federation_ingest_result_dropped.additional_properties = d
        return federation_ingest_result_dropped

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
