from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecaster_track_record_type_0_label import (
    ForecasterTrackRecordType0Label,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecasterTrackRecordType0")


@_attrs_define
class ForecasterTrackRecordType0:
    """Headline track record

    Attributes:
        n_resolved (int):
        label (ForecasterTrackRecordType0Label):
        brier (float | None | Unset):
    """

    n_resolved: int
    label: ForecasterTrackRecordType0Label
    brier: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        n_resolved = self.n_resolved

        label = self.label.value

        brier: float | None | Unset
        if isinstance(self.brier, Unset):
            brier = UNSET
        else:
            brier = self.brier

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "nResolved": n_resolved,
                "label": label,
            }
        )
        if brier is not UNSET:
            field_dict["brier"] = brier

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        n_resolved = d.pop("nResolved")

        label = ForecasterTrackRecordType0Label(d.pop("label"))

        def _parse_brier(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        brier = _parse_brier(d.pop("brier", UNSET))

        forecaster_track_record_type_0 = cls(
            n_resolved=n_resolved,
            label=label,
            brier=brier,
        )

        forecaster_track_record_type_0.additional_properties = d
        return forecaster_track_record_type_0

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
