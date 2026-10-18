from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.import_media_ratings_json_body_rows_item_dimension_type_0 import (
    ImportMediaRatingsJsonBodyRowsItemDimensionType0,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ImportMediaRatingsJsonBodyRowsItem")


@_attrs_define
class ImportMediaRatingsJsonBodyRowsItem:
    """Operator rating row

    Attributes:
        domain (str):
        dimension (ImportMediaRatingsJsonBodyRowsItemDimensionType0 | str): Rating dimension; provider axes that are not
            left–right are recorded as `{provider}:{axis}`, never forced into lean
        value (str):
        licence (str):
        attribution (str):
        label (None | str | Unset):
    """

    domain: str
    dimension: ImportMediaRatingsJsonBodyRowsItemDimensionType0 | str
    value: str
    licence: str
    attribution: str
    label: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        domain = self.domain

        dimension: str
        if isinstance(self.dimension, ImportMediaRatingsJsonBodyRowsItemDimensionType0):
            dimension = self.dimension.value
        else:
            dimension = self.dimension

        value = self.value

        licence = self.licence

        attribution = self.attribution

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "domain": domain,
                "dimension": dimension,
                "value": value,
                "licence": licence,
                "attribution": attribution,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        domain = d.pop("domain")

        def _parse_dimension(
            data: object,
        ) -> ImportMediaRatingsJsonBodyRowsItemDimensionType0 | str:
            try:
                if not isinstance(data, str):
                    raise TypeError()
                dimension_type_0 = ImportMediaRatingsJsonBodyRowsItemDimensionType0(
                    data
                )

                return dimension_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ImportMediaRatingsJsonBodyRowsItemDimensionType0 | str, data)

        dimension = _parse_dimension(d.pop("dimension"))

        value = d.pop("value")

        licence = d.pop("licence")

        attribution = d.pop("attribution")

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        import_media_ratings_json_body_rows_item = cls(
            domain=domain,
            dimension=dimension,
            value=value,
            licence=licence,
            attribution=attribution,
            label=label,
        )

        import_media_ratings_json_body_rows_item.additional_properties = d
        return import_media_ratings_json_body_rows_item

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
