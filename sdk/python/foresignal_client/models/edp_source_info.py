from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_source_info_access import EdpSourceInfoAccess
    from ..models.edp_source_info_licence import EdpSourceInfoLicence
    from ..models.edp_source_info_originator import EdpSourceInfoOriginator
    from ..models.edp_source_info_publisher import EdpSourceInfoPublisher
    from ..models.edp_source_info_terms import EdpSourceInfoTerms


T = TypeVar("T", bound="EdpSourceInfo")


@_attrs_define
class EdpSourceInfo:
    """Identity, publisher, licence, access and originator of a provider

    Attributes:
        id (str):
        name (str):
        publisher (EdpSourceInfoPublisher):
        originator (EdpSourceInfoOriginator):
        description (None | str | Unset):
        version (None | str | Unset):
        homepage (None | str | Unset):
        licence (EdpSourceInfoLicence | Unset):
        terms (EdpSourceInfoTerms | Unset):
        access (EdpSourceInfoAccess | Unset):
        private (bool | Unset):  Default: False.
    """

    id: str
    name: str
    publisher: EdpSourceInfoPublisher
    originator: EdpSourceInfoOriginator
    description: None | str | Unset = UNSET
    version: None | str | Unset = UNSET
    homepage: None | str | Unset = UNSET
    licence: EdpSourceInfoLicence | Unset = UNSET
    terms: EdpSourceInfoTerms | Unset = UNSET
    access: EdpSourceInfoAccess | Unset = UNSET
    private: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        publisher = self.publisher.to_dict()

        originator = self.originator.to_dict()

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        version: None | str | Unset
        if isinstance(self.version, Unset):
            version = UNSET
        else:
            version = self.version

        homepage: None | str | Unset
        if isinstance(self.homepage, Unset):
            homepage = UNSET
        else:
            homepage = self.homepage

        licence: dict[str, Any] | Unset = UNSET
        if not isinstance(self.licence, Unset):
            licence = self.licence.to_dict()

        terms: dict[str, Any] | Unset = UNSET
        if not isinstance(self.terms, Unset):
            terms = self.terms.to_dict()

        access: dict[str, Any] | Unset = UNSET
        if not isinstance(self.access, Unset):
            access = self.access.to_dict()

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "publisher": publisher,
                "originator": originator,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if version is not UNSET:
            field_dict["version"] = version
        if homepage is not UNSET:
            field_dict["homepage"] = homepage
        if licence is not UNSET:
            field_dict["licence"] = licence
        if terms is not UNSET:
            field_dict["terms"] = terms
        if access is not UNSET:
            field_dict["access"] = access
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_source_info_access import EdpSourceInfoAccess
        from ..models.edp_source_info_licence import EdpSourceInfoLicence
        from ..models.edp_source_info_originator import EdpSourceInfoOriginator
        from ..models.edp_source_info_publisher import EdpSourceInfoPublisher
        from ..models.edp_source_info_terms import EdpSourceInfoTerms

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        publisher = EdpSourceInfoPublisher.from_dict(d.pop("publisher"))

        originator = EdpSourceInfoOriginator.from_dict(d.pop("originator"))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        version = _parse_version(d.pop("version", UNSET))

        def _parse_homepage(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        homepage = _parse_homepage(d.pop("homepage", UNSET))

        _licence = d.pop("licence", UNSET)
        licence: EdpSourceInfoLicence | Unset
        if isinstance(_licence, Unset):
            licence = UNSET
        else:
            licence = EdpSourceInfoLicence.from_dict(_licence)

        _terms = d.pop("terms", UNSET)
        terms: EdpSourceInfoTerms | Unset
        if isinstance(_terms, Unset):
            terms = UNSET
        else:
            terms = EdpSourceInfoTerms.from_dict(_terms)

        _access = d.pop("access", UNSET)
        access: EdpSourceInfoAccess | Unset
        if isinstance(_access, Unset):
            access = UNSET
        else:
            access = EdpSourceInfoAccess.from_dict(_access)

        private = d.pop("private", UNSET)

        edp_source_info = cls(
            id=id,
            name=name,
            publisher=publisher,
            originator=originator,
            description=description,
            version=version,
            homepage=homepage,
            licence=licence,
            terms=terms,
            access=access,
            private=private,
        )

        edp_source_info.additional_properties = d
        return edp_source_info

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
