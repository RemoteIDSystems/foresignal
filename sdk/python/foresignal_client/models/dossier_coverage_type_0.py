from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_coverage_type_0_status import DossierCoverageType0Status
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.coverage_stats import CoverageStats


T = TypeVar("T", bound="DossierCoverageType0")


@_attrs_define
class DossierCoverageType0:
    """Dossier coverage section

    Attributes:
        status (DossierCoverageType0Status):
        stats (CoverageStats | None | Unset):
    """

    status: DossierCoverageType0Status
    stats: CoverageStats | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.coverage_stats import CoverageStats

        status = self.status.value

        stats: dict[str, Any] | None | Unset
        if isinstance(self.stats, Unset):
            stats = UNSET
        elif isinstance(self.stats, CoverageStats):
            stats = self.stats.to_dict()
        else:
            stats = self.stats

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if stats is not UNSET:
            field_dict["stats"] = stats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coverage_stats import CoverageStats

        d = dict(src_dict)
        status = DossierCoverageType0Status(d.pop("status"))

        def _parse_stats(data: object) -> CoverageStats | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                stats_type_0 = CoverageStats.from_dict(data)

                return stats_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CoverageStats | None | Unset, data)

        stats = _parse_stats(d.pop("stats", UNSET))

        dossier_coverage_type_0 = cls(
            status=status,
            stats=stats,
        )

        dossier_coverage_type_0.additional_properties = d
        return dossier_coverage_type_0

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
