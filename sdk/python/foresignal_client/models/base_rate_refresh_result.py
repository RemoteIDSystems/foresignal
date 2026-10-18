from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="BaseRateRefreshResult")


@_attrs_define
class BaseRateRefreshResult:
    """Result of a base-rate-refresh job (121): region profiles and automatic rates recomputed

    Attributes:
        profiles (int): Region profiles written
        rates (int): Automatic rates written (family × region × window)
        families (list[str]):
        skipped (int | Unset): Family-region pairs with under 52 covered weeks
        removed (int | Unset): Automatic rates no longer produced and removed
        regions (int | Unset): Regions with at least one automatic rate
        computer (DetectorRef | Unset):
    """

    profiles: int
    rates: int
    families: list[str]
    skipped: int | Unset = UNSET
    removed: int | Unset = UNSET
    regions: int | Unset = UNSET
    computer: DetectorRef | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        profiles = self.profiles

        rates = self.rates

        families = self.families

        skipped = self.skipped

        removed = self.removed

        regions = self.regions

        computer: dict[str, Any] | Unset = UNSET
        if not isinstance(self.computer, Unset):
            computer = self.computer.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "profiles": profiles,
                "rates": rates,
                "families": families,
            }
        )
        if skipped is not UNSET:
            field_dict["skipped"] = skipped
        if removed is not UNSET:
            field_dict["removed"] = removed
        if regions is not UNSET:
            field_dict["regions"] = regions
        if computer is not UNSET:
            field_dict["computer"] = computer

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref import DetectorRef

        d = dict(src_dict)
        profiles = d.pop("profiles")

        rates = d.pop("rates")

        families = cast(list[str], d.pop("families"))

        skipped = d.pop("skipped", UNSET)

        removed = d.pop("removed", UNSET)

        regions = d.pop("regions", UNSET)

        _computer = d.pop("computer", UNSET)
        computer: DetectorRef | Unset
        if isinstance(_computer, Unset):
            computer = UNSET
        else:
            computer = DetectorRef.from_dict(_computer)

        base_rate_refresh_result = cls(
            profiles=profiles,
            rates=rates,
            families=families,
            skipped=skipped,
            removed=removed,
            regions=regions,
            computer=computer,
        )

        base_rate_refresh_result.additional_properties = d
        return base_rate_refresh_result

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
