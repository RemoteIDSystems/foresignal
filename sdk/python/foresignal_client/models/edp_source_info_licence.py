from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_source_info_licence_redistribution import (
    EdpSourceInfoLicenceRedistribution,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpSourceInfoLicence")


@_attrs_define
class EdpSourceInfoLicence:
    """
    Attributes:
        id (None | str | Unset):
        url (None | str | Unset):
        attribution (None | str | Unset):
        redistribution (EdpSourceInfoLicenceRedistribution | Unset):  Default:
            EdpSourceInfoLicenceRedistribution.UNKNOWN.
    """

    id: None | str | Unset = UNSET
    url: None | str | Unset = UNSET
    attribution: None | str | Unset = UNSET
    redistribution: EdpSourceInfoLicenceRedistribution | Unset = (
        EdpSourceInfoLicenceRedistribution.UNKNOWN
    )
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id: None | str | Unset
        if isinstance(self.id, Unset):
            id = UNSET
        else:
            id = self.id

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        redistribution: str | Unset = UNSET
        if not isinstance(self.redistribution, Unset):
            redistribution = self.redistribution.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if id is not UNSET:
            field_dict["id"] = id
        if url is not UNSET:
            field_dict["url"] = url
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if redistribution is not UNSET:
            field_dict["redistribution"] = redistribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        id = _parse_id(d.pop("id", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        _redistribution = d.pop("redistribution", UNSET)
        redistribution: EdpSourceInfoLicenceRedistribution | Unset
        if isinstance(_redistribution, Unset):
            redistribution = UNSET
        else:
            redistribution = EdpSourceInfoLicenceRedistribution(_redistribution)

        edp_source_info_licence = cls(
            id=id,
            url=url,
            attribution=attribution,
            redistribution=redistribution,
        )

        edp_source_info_licence.additional_properties = d
        return edp_source_info_licence

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
