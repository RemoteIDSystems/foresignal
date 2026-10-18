from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat


T = TypeVar("T", bound="ForecastResolution")


@_attrs_define
class ForecastResolution:
    """How a question resolved, with pinned evidence

    Attributes:
        resolved_at (datetime.datetime):
        resolved_by (str): Resolver id and version, or the principal for manual resolutions
        evidence (list[str]): Pinned records or rollup rows
        outcome (None | str | Unset): Binary `yes`/`no`, or the label of a multiple/ordered question
        value (float | None | Unset): Numeric questions
        early (bool | Unset): Resolved before `resolve_by` because the event occurred
        as_of (datetime.datetime | None | Unset): Knowledge cutoff used (`resolve_by` + release lag)
        note (None | str | Unset):
        caveats (list[Caveat] | Unset): e.g. late data still missing after 14 days
        manifest_id (None | str | Unset):
    """

    resolved_at: datetime.datetime
    resolved_by: str
    evidence: list[str]
    outcome: None | str | Unset = UNSET
    value: float | None | Unset = UNSET
    early: bool | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    note: None | str | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        resolved_at = self.resolved_at.isoformat()

        resolved_by = self.resolved_by

        evidence = self.evidence

        outcome: None | str | Unset
        if isinstance(self.outcome, Unset):
            outcome = UNSET
        else:
            outcome = self.outcome

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        early = self.early

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "resolvedAt": resolved_at,
                "resolvedBy": resolved_by,
                "evidence": evidence,
            }
        )
        if outcome is not UNSET:
            field_dict["outcome"] = outcome
        if value is not UNSET:
            field_dict["value"] = value
        if early is not UNSET:
            field_dict["early"] = early
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if note is not UNSET:
            field_dict["note"] = note
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat

        d = dict(src_dict)
        resolved_at = datetime.datetime.fromisoformat(d.pop("resolvedAt"))

        resolved_by = d.pop("resolvedBy")

        evidence = cast(list[str], d.pop("evidence"))

        def _parse_outcome(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        outcome = _parse_outcome(d.pop("outcome", UNSET))

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        early = d.pop("early", UNSET)

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        forecast_resolution = cls(
            resolved_at=resolved_at,
            resolved_by=resolved_by,
            evidence=evidence,
            outcome=outcome,
            value=value,
            early=early,
            as_of=as_of,
            note=note,
            caveats=caveats,
            manifest_id=manifest_id,
        )

        forecast_resolution.additional_properties = d
        return forecast_resolution

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
