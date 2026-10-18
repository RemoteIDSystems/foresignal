from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tag_namespace import TagNamespace
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="Tag")


@_attrs_define
class Tag:
    """A personal or shared tag that can be assigned to any object reference

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        namespace (TagNamespace):
        owner (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        visibility (Visibility):
        created_at (datetime.datetime):
        color (None | str | Unset):
        description (None | str | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        object_count (int | Unset): Objects carrying this tag that the caller can see
        updated_at (datetime.datetime | Unset):
    """

    id: str
    name: str
    namespace: TagNamespace
    owner: PrincipalSummary
    visibility: Visibility
    created_at: datetime.datetime
    color: None | str | Unset = UNSET
    description: None | str | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    object_count: int | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        namespace = self.namespace.value

        owner = self.owner.to_dict()

        visibility = self.visibility.value

        created_at = self.created_at.isoformat()

        color: None | str | Unset
        if isinstance(self.color, Unset):
            color = UNSET
        else:
            color = self.color

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        object_count = self.object_count

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "namespace": namespace,
                "owner": owner,
                "visibility": visibility,
                "createdAt": created_at,
            }
        )
        if color is not UNSET:
            field_dict["color"] = color
        if description is not UNSET:
            field_dict["description"] = description
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if object_count is not UNSET:
            field_dict["objectCount"] = object_count
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        namespace = TagNamespace(d.pop("namespace"))

        owner = PrincipalSummary.from_dict(d.pop("owner"))

        visibility = Visibility(d.pop("visibility"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_color(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        color = _parse_color(d.pop("color", UNSET))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        object_count = d.pop("objectCount", UNSET)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        tag = cls(
            id=id,
            name=name,
            namespace=namespace,
            owner=owner,
            visibility=visibility,
            created_at=created_at,
            color=color,
            description=description,
            tlp=tlp,
            object_count=object_count,
            updated_at=updated_at,
        )

        tag.additional_properties = d
        return tag

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
