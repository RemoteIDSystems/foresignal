from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exchange_package_status import ExchangePackageStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.exchange_package_deliveries_item import ExchangePackageDeliveriesItem
    from ..models.package_manifest import PackageManifest
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="ExchangePackage")


@_attrs_define
class ExchangePackage:
    """A package built by this instance and its delivery state

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        manifest (PackageManifest): `manifest.json` of a signed intelligence package; `signature.jws` is a detached JWS
            over it with the instance key (109)
        status (ExchangePackageStatus):
        created_by (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        created_at (datetime.datetime):
        size_bytes (int | None | Unset):
        deliveries (list[ExchangePackageDeliveriesItem] | Unset):
        reductions (list[Caveat] | Unset): LOCAL_ONLY or private data excluded or reduced to derived statistics
        download_href (None | str | Unset):
        error (None | str | Unset):
    """

    id: str
    manifest: PackageManifest
    status: ExchangePackageStatus
    created_by: PrincipalSummary
    created_at: datetime.datetime
    size_bytes: int | None | Unset = UNSET
    deliveries: list[ExchangePackageDeliveriesItem] | Unset = UNSET
    reductions: list[Caveat] | Unset = UNSET
    download_href: None | str | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        manifest = self.manifest.to_dict()

        status = self.status.value

        created_by = self.created_by.to_dict()

        created_at = self.created_at.isoformat()

        size_bytes: int | None | Unset
        if isinstance(self.size_bytes, Unset):
            size_bytes = UNSET
        else:
            size_bytes = self.size_bytes

        deliveries: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.deliveries, Unset):
            deliveries = []
            for deliveries_item_data in self.deliveries:
                deliveries_item = deliveries_item_data.to_dict()
                deliveries.append(deliveries_item)

        reductions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.reductions, Unset):
            reductions = []
            for reductions_item_data in self.reductions:
                reductions_item = reductions_item_data.to_dict()
                reductions.append(reductions_item)

        download_href: None | str | Unset
        if isinstance(self.download_href, Unset):
            download_href = UNSET
        else:
            download_href = self.download_href

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "manifest": manifest,
                "status": status,
                "createdBy": created_by,
                "createdAt": created_at,
            }
        )
        if size_bytes is not UNSET:
            field_dict["sizeBytes"] = size_bytes
        if deliveries is not UNSET:
            field_dict["deliveries"] = deliveries
        if reductions is not UNSET:
            field_dict["reductions"] = reductions
        if download_href is not UNSET:
            field_dict["downloadHref"] = download_href
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.exchange_package_deliveries_item import (
            ExchangePackageDeliveriesItem,
        )
        from ..models.package_manifest import PackageManifest
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        id = d.pop("id")

        manifest = PackageManifest.from_dict(d.pop("manifest"))

        status = ExchangePackageStatus(d.pop("status"))

        created_by = PrincipalSummary.from_dict(d.pop("createdBy"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_size_bytes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        size_bytes = _parse_size_bytes(d.pop("sizeBytes", UNSET))

        _deliveries = d.pop("deliveries", UNSET)
        deliveries: list[ExchangePackageDeliveriesItem] | Unset = UNSET
        if _deliveries is not UNSET:
            deliveries = []
            for deliveries_item_data in _deliveries:
                deliveries_item = ExchangePackageDeliveriesItem.from_dict(
                    deliveries_item_data
                )

                deliveries.append(deliveries_item)

        _reductions = d.pop("reductions", UNSET)
        reductions: list[Caveat] | Unset = UNSET
        if _reductions is not UNSET:
            reductions = []
            for reductions_item_data in _reductions:
                reductions_item = Caveat.from_dict(reductions_item_data)

                reductions.append(reductions_item)

        def _parse_download_href(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        download_href = _parse_download_href(d.pop("downloadHref", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        exchange_package = cls(
            id=id,
            manifest=manifest,
            status=status,
            created_by=created_by,
            created_at=created_at,
            size_bytes=size_bytes,
            deliveries=deliveries,
            reductions=reductions,
            download_href=download_href,
            error=error,
        )

        exchange_package.additional_properties = d
        return exchange_package

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
