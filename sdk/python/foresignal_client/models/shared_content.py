from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.shared_content_target_type import SharedContentTargetType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.shared_content_data import SharedContentData
    from ..models.shared_content_links import SharedContentLinks


T = TypeVar("T", bound="SharedContent")


@_attrs_define
class SharedContent:
    """What a share link resolves to: the masked target with attribution (112)

    Attributes:
        target_type (SharedContentTargetType):
        expires_at (datetime.datetime):
        masked (bool):
        data (SharedContentData): The target's JSON after licence filtering, privacy masking and the field allowlist
        attributions (list[Attribution]):
        title (None | str | Unset):
        precision_m (int | None | Unset):
        links (SharedContentLinks | Unset): Sub-resources available for this target
    """

    target_type: SharedContentTargetType
    expires_at: datetime.datetime
    masked: bool
    data: SharedContentData
    attributions: list[Attribution]
    title: None | str | Unset = UNSET
    precision_m: int | None | Unset = UNSET
    links: SharedContentLinks | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        target_type = self.target_type.value

        expires_at = self.expires_at.isoformat()

        masked = self.masked

        data = self.data.to_dict()

        attributions = []
        for attributions_item_data in self.attributions:
            attributions_item = attributions_item_data.to_dict()
            attributions.append(attributions_item)

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        precision_m: int | None | Unset
        if isinstance(self.precision_m, Unset):
            precision_m = UNSET
        else:
            precision_m = self.precision_m

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "targetType": target_type,
                "expiresAt": expires_at,
                "masked": masked,
                "data": data,
                "attributions": attributions,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if precision_m is not UNSET:
            field_dict["precisionM"] = precision_m
        if links is not UNSET:
            field_dict["links"] = links

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.shared_content_data import SharedContentData
        from ..models.shared_content_links import SharedContentLinks

        d = dict(src_dict)
        target_type = SharedContentTargetType(d.pop("targetType"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        masked = d.pop("masked")

        data = SharedContentData.from_dict(d.pop("data"))

        attributions = []
        _attributions = d.pop("attributions")
        for attributions_item_data in _attributions:
            attributions_item = Attribution.from_dict(attributions_item_data)

            attributions.append(attributions_item)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_precision_m(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        precision_m = _parse_precision_m(d.pop("precisionM", UNSET))

        _links = d.pop("links", UNSET)
        links: SharedContentLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = SharedContentLinks.from_dict(_links)

        shared_content = cls(
            target_type=target_type,
            expires_at=expires_at,
            masked=masked,
            data=data,
            attributions=attributions,
            title=title,
            precision_m=precision_m,
            links=links,
        )

        shared_content.additional_properties = d
        return shared_content

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
