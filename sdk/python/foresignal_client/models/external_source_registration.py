from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_manifest import EdpManifest


T = TypeVar("T", bound="ExternalSourceRegistration")


@_attrs_define
class ExternalSourceRegistration:
    """Register by manifest or manifest URL

    Attributes:
        manifest (EdpManifest | None | Unset):
        manifest_url (None | str | Unset):
        approve (bool | Unset):  Default: False.
        allow_private_network (bool | Unset): Allow this provider's URLs (manifestUrl, pull itemsUrl, search url) to
            resolve to private, loopback, link-local or carrier-grade NAT addresses and to use plain http, for a provider on
            the operator's own network. Cloud-metadata addresses stay refused (EDP §7, invariant EXT-6). Set only at
            registration (024). Default: False.
    """

    manifest: EdpManifest | None | Unset = UNSET
    manifest_url: None | str | Unset = UNSET
    approve: bool | Unset = False
    allow_private_network: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_manifest import EdpManifest

        manifest: dict[str, Any] | None | Unset
        if isinstance(self.manifest, Unset):
            manifest = UNSET
        elif isinstance(self.manifest, EdpManifest):
            manifest = self.manifest.to_dict()
        else:
            manifest = self.manifest

        manifest_url: None | str | Unset
        if isinstance(self.manifest_url, Unset):
            manifest_url = UNSET
        else:
            manifest_url = self.manifest_url

        approve = self.approve

        allow_private_network = self.allow_private_network

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if manifest is not UNSET:
            field_dict["manifest"] = manifest
        if manifest_url is not UNSET:
            field_dict["manifestUrl"] = manifest_url
        if approve is not UNSET:
            field_dict["approve"] = approve
        if allow_private_network is not UNSET:
            field_dict["allowPrivateNetwork"] = allow_private_network

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_manifest import EdpManifest

        d = dict(src_dict)

        def _parse_manifest(data: object) -> EdpManifest | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                manifest_type_0 = EdpManifest.from_dict(data)

                return manifest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpManifest | None | Unset, data)

        manifest = _parse_manifest(d.pop("manifest", UNSET))

        def _parse_manifest_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_url = _parse_manifest_url(d.pop("manifestUrl", UNSET))

        approve = d.pop("approve", UNSET)

        allow_private_network = d.pop("allowPrivateNetwork", UNSET)

        external_source_registration = cls(
            manifest=manifest,
            manifest_url=manifest_url,
            approve=approve,
            allow_private_network=allow_private_network,
        )

        external_source_registration.additional_properties = d
        return external_source_registration

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
