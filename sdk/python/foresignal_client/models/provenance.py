from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.provenance_licence_basis import ProvenanceLicenceBasis
from ..models.provenance_reliability import ProvenanceReliability
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.licence_terms import LicenceTerms
    from ..models.provenance_hop import ProvenanceHop


T = TypeVar("T", bound="Provenance")


@_attrs_define
class Provenance:
    """
    Attributes:
        source_ids (list[str] | Unset):
        manifest_id (None | str | Unset):
        as_of_seq (int | None | Unset):
        licence (LicenceTerms | Unset): Structured licence terms resolved for a source or a record version (109)
        licence_basis (ProvenanceLicenceBasis | Unset): How the licence was resolved: the batch snapshot, the source's
            current descriptor (batch missing), the per-record MLAT LOCAL_ONLY downgrade, or a peer record's
            _foresignal.originLicence
        fetch_id (int | None | Unset): fetch_log id of the batch that wrote the version
        adapter_version (None | str | Unset):
        principal (None | str | Unset): Push principal; admin only
        reliability (ProvenanceReliability | Unset): Admiralty source reliability: A completely reliable, B usually
            reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
        credibility (int | Unset): Admiralty information credibility: 1 confirmed, 2 probably true, 3 possibly true, 4
            doubtful, 5 improbable, 6 cannot be judged
        grade (str | Unset): Admiralty grade = reliability + credibility, computed at read time Example: B2.
        origin (ProvenanceHop | Unset): One instance in a federation provenance chain (113)
        chain (list[ProvenanceHop] | Unset): Federation: origin first, one hop appended per instance that re-served the
            record
    """

    source_ids: list[str] | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    as_of_seq: int | None | Unset = UNSET
    licence: LicenceTerms | Unset = UNSET
    licence_basis: ProvenanceLicenceBasis | Unset = UNSET
    fetch_id: int | None | Unset = UNSET
    adapter_version: None | str | Unset = UNSET
    principal: None | str | Unset = UNSET
    reliability: ProvenanceReliability | Unset = UNSET
    credibility: int | Unset = UNSET
    grade: str | Unset = UNSET
    origin: ProvenanceHop | Unset = UNSET
    chain: list[ProvenanceHop] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_ids: list[str] | Unset = UNSET
        if not isinstance(self.source_ids, Unset):
            source_ids = self.source_ids

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        as_of_seq: int | None | Unset
        if isinstance(self.as_of_seq, Unset):
            as_of_seq = UNSET
        else:
            as_of_seq = self.as_of_seq

        licence: dict[str, Any] | Unset = UNSET
        if not isinstance(self.licence, Unset):
            licence = self.licence.to_dict()

        licence_basis: str | Unset = UNSET
        if not isinstance(self.licence_basis, Unset):
            licence_basis = self.licence_basis.value

        fetch_id: int | None | Unset
        if isinstance(self.fetch_id, Unset):
            fetch_id = UNSET
        else:
            fetch_id = self.fetch_id

        adapter_version: None | str | Unset
        if isinstance(self.adapter_version, Unset):
            adapter_version = UNSET
        else:
            adapter_version = self.adapter_version

        principal: None | str | Unset
        if isinstance(self.principal, Unset):
            principal = UNSET
        else:
            principal = self.principal

        reliability: str | Unset = UNSET
        if not isinstance(self.reliability, Unset):
            reliability = self.reliability.value

        credibility = self.credibility

        grade = self.grade

        origin: dict[str, Any] | Unset = UNSET
        if not isinstance(self.origin, Unset):
            origin = self.origin.to_dict()

        chain: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.chain, Unset):
            chain = []
            for chain_item_data in self.chain:
                chain_item = chain_item_data.to_dict()
                chain.append(chain_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if source_ids is not UNSET:
            field_dict["sourceIds"] = source_ids
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if as_of_seq is not UNSET:
            field_dict["asOfSeq"] = as_of_seq
        if licence is not UNSET:
            field_dict["licence"] = licence
        if licence_basis is not UNSET:
            field_dict["licenceBasis"] = licence_basis
        if fetch_id is not UNSET:
            field_dict["fetchId"] = fetch_id
        if adapter_version is not UNSET:
            field_dict["adapterVersion"] = adapter_version
        if principal is not UNSET:
            field_dict["principal"] = principal
        if reliability is not UNSET:
            field_dict["reliability"] = reliability
        if credibility is not UNSET:
            field_dict["credibility"] = credibility
        if grade is not UNSET:
            field_dict["grade"] = grade
        if origin is not UNSET:
            field_dict["origin"] = origin
        if chain is not UNSET:
            field_dict["chain"] = chain

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.licence_terms import LicenceTerms
        from ..models.provenance_hop import ProvenanceHop

        d = dict(src_dict)
        source_ids = cast(list[str], d.pop("sourceIds", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        def _parse_as_of_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        as_of_seq = _parse_as_of_seq(d.pop("asOfSeq", UNSET))

        _licence = d.pop("licence", UNSET)
        licence: LicenceTerms | Unset
        if isinstance(_licence, Unset):
            licence = UNSET
        else:
            licence = LicenceTerms.from_dict(_licence)

        _licence_basis = d.pop("licenceBasis", UNSET)
        licence_basis: ProvenanceLicenceBasis | Unset
        if isinstance(_licence_basis, Unset):
            licence_basis = UNSET
        else:
            licence_basis = ProvenanceLicenceBasis(_licence_basis)

        def _parse_fetch_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        fetch_id = _parse_fetch_id(d.pop("fetchId", UNSET))

        def _parse_adapter_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        adapter_version = _parse_adapter_version(d.pop("adapterVersion", UNSET))

        def _parse_principal(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal = _parse_principal(d.pop("principal", UNSET))

        _reliability = d.pop("reliability", UNSET)
        reliability: ProvenanceReliability | Unset
        if isinstance(_reliability, Unset):
            reliability = UNSET
        else:
            reliability = ProvenanceReliability(_reliability)

        credibility = d.pop("credibility", UNSET)

        grade = d.pop("grade", UNSET)

        _origin = d.pop("origin", UNSET)
        origin: ProvenanceHop | Unset
        if isinstance(_origin, Unset):
            origin = UNSET
        else:
            origin = ProvenanceHop.from_dict(_origin)

        _chain = d.pop("chain", UNSET)
        chain: list[ProvenanceHop] | Unset = UNSET
        if _chain is not UNSET:
            chain = []
            for chain_item_data in _chain:
                chain_item = ProvenanceHop.from_dict(chain_item_data)

                chain.append(chain_item)

        provenance = cls(
            source_ids=source_ids,
            manifest_id=manifest_id,
            as_of_seq=as_of_seq,
            licence=licence,
            licence_basis=licence_basis,
            fetch_id=fetch_id,
            adapter_version=adapter_version,
            principal=principal,
            reliability=reliability,
            credibility=credibility,
            grade=grade,
            origin=origin,
            chain=chain,
        )

        provenance.additional_properties = d
        return provenance

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
