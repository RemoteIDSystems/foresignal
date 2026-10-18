from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationEvidenceCorroboration")


@_attrs_define
class SituationEvidenceCorroboration:
    """116 corroboration roll-up; echoed reports never count twice

    Attributes:
        corroborated (int):
        uncorroborated (int):
        share (float | None):
        unknown (int | Unset):
        not_applicable (int | Unset):
        uncorroborated_only (bool | Unset): True when the situation rests only on uncorroborated text reports
    """

    corroborated: int
    uncorroborated: int
    share: float | None
    unknown: int | Unset = UNSET
    not_applicable: int | Unset = UNSET
    uncorroborated_only: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        corroborated = self.corroborated

        uncorroborated = self.uncorroborated

        share: float | None
        share = self.share

        unknown = self.unknown

        not_applicable = self.not_applicable

        uncorroborated_only = self.uncorroborated_only

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "corroborated": corroborated,
                "uncorroborated": uncorroborated,
                "share": share,
            }
        )
        if unknown is not UNSET:
            field_dict["unknown"] = unknown
        if not_applicable is not UNSET:
            field_dict["notApplicable"] = not_applicable
        if uncorroborated_only is not UNSET:
            field_dict["uncorroboratedOnly"] = uncorroborated_only

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        corroborated = d.pop("corroborated")

        uncorroborated = d.pop("uncorroborated")

        def _parse_share(data: object) -> float | None:
            if data is None:
                return data
            return cast(float | None, data)

        share = _parse_share(d.pop("share"))

        unknown = d.pop("unknown", UNSET)

        not_applicable = d.pop("notApplicable", UNSET)

        uncorroborated_only = d.pop("uncorroboratedOnly", UNSET)

        situation_evidence_corroboration = cls(
            corroborated=corroborated,
            uncorroborated=uncorroborated,
            share=share,
            unknown=unknown,
            not_applicable=not_applicable,
            uncorroborated_only=uncorroborated_only,
        )

        situation_evidence_corroboration.additional_properties = d
        return situation_evidence_corroboration

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
