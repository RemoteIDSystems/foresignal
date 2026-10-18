from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.infra_impact_result_dependents_item import (
        InfraImpactResultDependentsItem,
    )
    from ..models.infra_impact_result_summary import InfraImpactResultSummary
    from ..models.infra_node import InfraNode


T = TypeVar("T", bound="InfraImpactResult")


@_attrs_define
class InfraImpactResult:
    """Facilities inside the area and their dependents with weakest-link path labels, caveats and attribution. Derived from
    open data; not authoritative.

        Attributes:
            build_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            direct (list[InfraNode]):
            dependents (list[InfraImpactResultDependentsItem]):
            summary (InfraImpactResultSummary): Counts by facility type and label
            caveats (list[Caveat]): Always includes open data / heuristic links; region-level area; OSM extract date;
                disabled rules
            attribution (list[str]):
            truncated (bool):
            approximate_area (bool | Unset): Seeds came from a region polygon
    """

    build_id: str
    direct: list[InfraNode]
    dependents: list[InfraImpactResultDependentsItem]
    summary: InfraImpactResultSummary
    caveats: list[Caveat]
    attribution: list[str]
    truncated: bool
    approximate_area: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        build_id = self.build_id

        direct = []
        for direct_item_data in self.direct:
            direct_item = direct_item_data.to_dict()
            direct.append(direct_item)

        dependents = []
        for dependents_item_data in self.dependents:
            dependents_item = dependents_item_data.to_dict()
            dependents.append(dependents_item)

        summary = self.summary.to_dict()

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        attribution = self.attribution

        truncated = self.truncated

        approximate_area = self.approximate_area

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "buildId": build_id,
                "direct": direct,
                "dependents": dependents,
                "summary": summary,
                "caveats": caveats,
                "attribution": attribution,
                "truncated": truncated,
            }
        )
        if approximate_area is not UNSET:
            field_dict["approximateArea"] = approximate_area

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.infra_impact_result_dependents_item import (
            InfraImpactResultDependentsItem,
        )
        from ..models.infra_impact_result_summary import InfraImpactResultSummary
        from ..models.infra_node import InfraNode

        d = dict(src_dict)
        build_id = d.pop("buildId")

        direct = []
        _direct = d.pop("direct")
        for direct_item_data in _direct:
            direct_item = InfraNode.from_dict(direct_item_data)

            direct.append(direct_item)

        dependents = []
        _dependents = d.pop("dependents")
        for dependents_item_data in _dependents:
            dependents_item = InfraImpactResultDependentsItem.from_dict(
                dependents_item_data
            )

            dependents.append(dependents_item)

        summary = InfraImpactResultSummary.from_dict(d.pop("summary"))

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        attribution = cast(list[str], d.pop("attribution"))

        truncated = d.pop("truncated")

        approximate_area = d.pop("approximateArea", UNSET)

        infra_impact_result = cls(
            build_id=build_id,
            direct=direct,
            dependents=dependents,
            summary=summary,
            caveats=caveats,
            attribution=attribution,
            truncated=truncated,
            approximate_area=approximate_area,
        )

        infra_impact_result.additional_properties = d
        return infra_impact_result

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
