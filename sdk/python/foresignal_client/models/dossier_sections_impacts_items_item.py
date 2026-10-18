from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_impacts_items_item_interval_type_0 import (
        DossierSectionsImpactsItemsItemIntervalType0,
    )


T = TypeVar("T", bound="DossierSectionsImpactsItemsItem")


@_attrs_define
class DossierSectionsImpactsItemsItem:
    """Projected impact summary

    Attributes:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        impact_family (str):
        p_impact (float):
        labels (list[str]):
        admin1 (str | Unset):
        interval (DossierSectionsImpactsItemsItemIntervalType0 | None | Unset):
        dependency_paths (int | Unset):
        question_id (None | str | Unset):
    """

    finding_id: str
    impact_family: str
    p_impact: float
    labels: list[str]
    admin1: str | Unset = UNSET
    interval: DossierSectionsImpactsItemsItemIntervalType0 | None | Unset = UNSET
    dependency_paths: int | Unset = UNSET
    question_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_impacts_items_item_interval_type_0 import (
            DossierSectionsImpactsItemsItemIntervalType0,
        )

        finding_id = self.finding_id

        impact_family = self.impact_family

        p_impact = self.p_impact

        labels = self.labels

        admin1 = self.admin1

        interval: dict[str, Any] | None | Unset
        if isinstance(self.interval, Unset):
            interval = UNSET
        elif isinstance(self.interval, DossierSectionsImpactsItemsItemIntervalType0):
            interval = self.interval.to_dict()
        else:
            interval = self.interval

        dependency_paths = self.dependency_paths

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "findingId": finding_id,
                "impactFamily": impact_family,
                "pImpact": p_impact,
                "labels": labels,
            }
        )
        if admin1 is not UNSET:
            field_dict["admin1"] = admin1
        if interval is not UNSET:
            field_dict["interval"] = interval
        if dependency_paths is not UNSET:
            field_dict["dependencyPaths"] = dependency_paths
        if question_id is not UNSET:
            field_dict["questionId"] = question_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_impacts_items_item_interval_type_0 import (
            DossierSectionsImpactsItemsItemIntervalType0,
        )

        d = dict(src_dict)
        finding_id = d.pop("findingId")

        impact_family = d.pop("impactFamily")

        p_impact = d.pop("pImpact")

        labels = cast(list[str], d.pop("labels"))

        admin1 = d.pop("admin1", UNSET)

        def _parse_interval(
            data: object,
        ) -> DossierSectionsImpactsItemsItemIntervalType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                interval_type_0 = (
                    DossierSectionsImpactsItemsItemIntervalType0.from_dict(data)
                )

                return interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                DossierSectionsImpactsItemsItemIntervalType0 | None | Unset, data
            )

        interval = _parse_interval(d.pop("interval", UNSET))

        dependency_paths = d.pop("dependencyPaths", UNSET)

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        dossier_sections_impacts_items_item = cls(
            finding_id=finding_id,
            impact_family=impact_family,
            p_impact=p_impact,
            labels=labels,
            admin1=admin1,
            interval=interval,
            dependency_paths=dependency_paths,
            question_id=question_id,
        )

        dossier_sections_impacts_items_item.additional_properties = d
        return dossier_sections_impacts_items_item

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
