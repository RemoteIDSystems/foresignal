from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.package_manifest_type import PackageManifestType
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.package_manifest_authors_item import PackageManifestAuthorsItem
    from ..models.package_manifest_content_hashes import PackageManifestContentHashes
    from ..models.package_manifest_sender import PackageManifestSender
    from ..models.package_recipient import PackageRecipient
    from ..models.provenance_chain_entry import ProvenanceChainEntry


T = TypeVar("T", bound="PackageManifest")


@_attrs_define
class PackageManifest:
    """`manifest.json` of a signed intelligence package; `signature.jws` is a detached JWS over it with the instance key
    (109)

        Attributes:
            package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            type_ (PackageManifestType):
            object_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
                `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            sender (PackageManifestSender): Sending instance
            authors (list[PackageManifestAuthorsItem]):
            tlp (Tlp): Traffic Light Protocol 2.0 label
            created_at (datetime.datetime):
            recipients (list[PackageRecipient] | Literal['*']): Named recipients, or `*` for GREEN/CLEAR
            content_hashes (PackageManifestContentHashes): Path under `content/` → sha256
            message (None | str | Unset):
            provenance_chain (list[ProvenanceChainEntry] | Unset):
            title (None | str | Unset): The packaged object's title
    """

    package_id: str
    type_: PackageManifestType
    object_ref: str
    sender: PackageManifestSender
    authors: list[PackageManifestAuthorsItem]
    tlp: Tlp
    created_at: datetime.datetime
    recipients: list[PackageRecipient] | Literal["*"]
    content_hashes: PackageManifestContentHashes
    message: None | str | Unset = UNSET
    provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
    title: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        package_id = self.package_id

        type_ = self.type_.value

        object_ref = self.object_ref

        sender = self.sender.to_dict()

        authors = []
        for authors_item_data in self.authors:
            authors_item = authors_item_data.to_dict()
            authors.append(authors_item)

        tlp = self.tlp.value

        created_at = self.created_at.isoformat()

        recipients: list[dict[str, Any]] | Literal["*"]
        if isinstance(self.recipients, list):
            recipients = []
            for recipients_type_0_item_data in self.recipients:
                recipients_type_0_item = recipients_type_0_item_data.to_dict()
                recipients.append(recipients_type_0_item)

        else:
            recipients = self.recipients

        content_hashes = self.content_hashes.to_dict()

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        provenance_chain: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.provenance_chain, Unset):
            provenance_chain = []
            for provenance_chain_item_data in self.provenance_chain:
                provenance_chain_item = provenance_chain_item_data.to_dict()
                provenance_chain.append(provenance_chain_item)

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "packageId": package_id,
                "type": type_,
                "objectRef": object_ref,
                "sender": sender,
                "authors": authors,
                "tlp": tlp,
                "createdAt": created_at,
                "recipients": recipients,
                "contentHashes": content_hashes,
            }
        )
        if message is not UNSET:
            field_dict["message"] = message
        if provenance_chain is not UNSET:
            field_dict["provenanceChain"] = provenance_chain
        if title is not UNSET:
            field_dict["title"] = title

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.package_manifest_authors_item import PackageManifestAuthorsItem
        from ..models.package_manifest_content_hashes import (
            PackageManifestContentHashes,
        )
        from ..models.package_manifest_sender import PackageManifestSender
        from ..models.package_recipient import PackageRecipient
        from ..models.provenance_chain_entry import ProvenanceChainEntry

        d = dict(src_dict)
        package_id = d.pop("packageId")

        type_ = PackageManifestType(d.pop("type"))

        object_ref = d.pop("objectRef")

        sender = PackageManifestSender.from_dict(d.pop("sender"))

        authors = []
        _authors = d.pop("authors")
        for authors_item_data in _authors:
            authors_item = PackageManifestAuthorsItem.from_dict(authors_item_data)

            authors.append(authors_item)

        tlp = Tlp(d.pop("tlp"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_recipients(data: object) -> list[PackageRecipient] | Literal["*"]:
            try:
                if not isinstance(data, list):
                    raise TypeError()
                recipients_type_0 = []
                _recipients_type_0 = data
                for recipients_type_0_item_data in _recipients_type_0:
                    recipients_type_0_item = PackageRecipient.from_dict(
                        recipients_type_0_item_data
                    )

                    recipients_type_0.append(recipients_type_0_item)

                return recipients_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            recipients_type_1 = cast(Literal["*"], data)
            if recipients_type_1 != "*":
                raise ValueError(
                    f"recipients_type_1 must match const '*', got '{recipients_type_1}'"
                )
            return recipients_type_1

        recipients = _parse_recipients(d.pop("recipients"))

        content_hashes = PackageManifestContentHashes.from_dict(d.pop("contentHashes"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        _provenance_chain = d.pop("provenanceChain", UNSET)
        provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
        if _provenance_chain is not UNSET:
            provenance_chain = []
            for provenance_chain_item_data in _provenance_chain:
                provenance_chain_item = ProvenanceChainEntry.from_dict(
                    provenance_chain_item_data
                )

                provenance_chain.append(provenance_chain_item)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        package_manifest = cls(
            package_id=package_id,
            type_=type_,
            object_ref=object_ref,
            sender=sender,
            authors=authors,
            tlp=tlp,
            created_at=created_at,
            recipients=recipients,
            content_hashes=content_hashes,
            message=message,
            provenance_chain=provenance_chain,
            title=title,
        )

        package_manifest.additional_properties = d
        return package_manifest

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
