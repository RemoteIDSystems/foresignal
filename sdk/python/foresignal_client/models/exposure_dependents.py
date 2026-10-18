from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exposure_dependents_status import ExposureDependentsStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.exposure_dependents_by_label import ExposureDependentsByLabel
    from ..models.exposure_dependents_by_type import ExposureDependentsByType


T = TypeVar("T", bound="ExposureDependents")


@_attrs_define
class ExposureDependents:
    """Infrastructure dependents summary (104)

    Attributes:
        status (ExposureDependentsStatus):
        attribution (list[Attribution] | Unset):
        build_id (None | str | Unset):
        by_label (ExposureDependentsByLabel | Unset): Dependent counts by path label
        by_type (ExposureDependentsByType | Unset):
    """

    status: ExposureDependentsStatus
    attribution: list[Attribution] | Unset = UNSET
    build_id: None | str | Unset = UNSET
    by_label: ExposureDependentsByLabel | Unset = UNSET
    by_type: ExposureDependentsByType | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        build_id: None | str | Unset
        if isinstance(self.build_id, Unset):
            build_id = UNSET
        else:
            build_id = self.build_id

        by_label: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_label, Unset):
            by_label = self.by_label.to_dict()

        by_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_type, Unset):
            by_type = self.by_type.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if build_id is not UNSET:
            field_dict["buildId"] = build_id
        if by_label is not UNSET:
            field_dict["byLabel"] = by_label
        if by_type is not UNSET:
            field_dict["byType"] = by_type

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.exposure_dependents_by_label import ExposureDependentsByLabel
        from ..models.exposure_dependents_by_type import ExposureDependentsByType

        d = dict(src_dict)
        status = ExposureDependentsStatus(d.pop("status"))

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        def _parse_build_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        build_id = _parse_build_id(d.pop("buildId", UNSET))

        _by_label = d.pop("byLabel", UNSET)
        by_label: ExposureDependentsByLabel | Unset
        if isinstance(_by_label, Unset):
            by_label = UNSET
        else:
            by_label = ExposureDependentsByLabel.from_dict(_by_label)

        _by_type = d.pop("byType", UNSET)
        by_type: ExposureDependentsByType | Unset
        if isinstance(_by_type, Unset):
            by_type = UNSET
        else:
            by_type = ExposureDependentsByType.from_dict(_by_type)

        exposure_dependents = cls(
            status=status,
            attribution=attribution,
            build_id=build_id,
            by_label=by_label,
            by_type=by_type,
        )

        exposure_dependents.additional_properties = d
        return exposure_dependents

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
