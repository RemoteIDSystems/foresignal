from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_device_update_content_level import PushDeviceUpdateContentLevel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_filters import PushFilters


T = TypeVar("T", bound="PushDeviceUpdate")


@_attrs_define
class PushDeviceUpdate:
    """Editable fields of a registered device (032)

    Attributes:
        name (None | str | Unset):
        filters (PushFilters | Unset): Which notifications a push target receives (031; shared by subscriptions and
            native devices)
        content_level (PushDeviceUpdateContentLevel | Unset):
        lang (None | str | Unset):
    """

    name: None | str | Unset = UNSET
    filters: PushFilters | Unset = UNSET
    content_level: PushDeviceUpdateContentLevel | Unset = UNSET
    lang: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        content_level: str | Unset = UNSET
        if not isinstance(self.content_level, Unset):
            content_level = self.content_level.value

        lang: None | str | Unset
        if isinstance(self.lang, Unset):
            lang = UNSET
        else:
            lang = self.lang

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if filters is not UNSET:
            field_dict["filters"] = filters
        if content_level is not UNSET:
            field_dict["contentLevel"] = content_level
        if lang is not UNSET:
            field_dict["lang"] = lang

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_filters import PushFilters

        d = dict(src_dict)

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        _filters = d.pop("filters", UNSET)
        filters: PushFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = PushFilters.from_dict(_filters)

        _content_level = d.pop("contentLevel", UNSET)
        content_level: PushDeviceUpdateContentLevel | Unset
        if isinstance(_content_level, Unset):
            content_level = UNSET
        else:
            content_level = PushDeviceUpdateContentLevel(_content_level)

        def _parse_lang(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lang = _parse_lang(d.pop("lang", UNSET))

        push_device_update = cls(
            name=name,
            filters=filters,
            content_level=content_level,
            lang=lang,
        )

        push_device_update.additional_properties = d
        return push_device_update

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
