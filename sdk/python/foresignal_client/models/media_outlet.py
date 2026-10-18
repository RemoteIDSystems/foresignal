from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.media_outlet_type import MediaOutletType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.media_rating import MediaRating
    from ..models.outlet_consensus import OutletConsensus


T = TypeVar("T", bound="MediaOutlet")


@_attrs_define
class MediaOutlet:
    """A news outlet in the registry with ratings per provider and consensus

    Attributes:
        id (str):
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        domains (list[str]):
        type_ (MediaOutletType):
        rated (bool): False for `unrated` outlets added from unknown domains
        consensus (list[OutletConsensus]):
        created_at (datetime.datetime):
        country (None | str | Unset): ISO 3166-1 alpha-2
        languages (list[str] | Unset):
        owner_org (None | str | Unset): Organisation name only
        parent_org (None | str | Unset):
        wikidata_qid (None | str | Unset):
        originator_id (None | str | Unset): 116 originator; wires map to their own
        aggregates (list[str] | Unset): Wire originators this outlet syndicates
        ratings (list[MediaRating] | Unset): Detail view only
        updated_at (datetime.datetime | Unset):
    """

    id: str
    ref: str
    name: str
    domains: list[str]
    type_: MediaOutletType
    rated: bool
    consensus: list[OutletConsensus]
    created_at: datetime.datetime
    country: None | str | Unset = UNSET
    languages: list[str] | Unset = UNSET
    owner_org: None | str | Unset = UNSET
    parent_org: None | str | Unset = UNSET
    wikidata_qid: None | str | Unset = UNSET
    originator_id: None | str | Unset = UNSET
    aggregates: list[str] | Unset = UNSET
    ratings: list[MediaRating] | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        ref = self.ref

        name = self.name

        domains = self.domains

        type_ = self.type_.value

        rated = self.rated

        consensus = []
        for consensus_item_data in self.consensus:
            consensus_item = consensus_item_data.to_dict()
            consensus.append(consensus_item)

        created_at = self.created_at.isoformat()

        country: None | str | Unset
        if isinstance(self.country, Unset):
            country = UNSET
        else:
            country = self.country

        languages: list[str] | Unset = UNSET
        if not isinstance(self.languages, Unset):
            languages = self.languages

        owner_org: None | str | Unset
        if isinstance(self.owner_org, Unset):
            owner_org = UNSET
        else:
            owner_org = self.owner_org

        parent_org: None | str | Unset
        if isinstance(self.parent_org, Unset):
            parent_org = UNSET
        else:
            parent_org = self.parent_org

        wikidata_qid: None | str | Unset
        if isinstance(self.wikidata_qid, Unset):
            wikidata_qid = UNSET
        else:
            wikidata_qid = self.wikidata_qid

        originator_id: None | str | Unset
        if isinstance(self.originator_id, Unset):
            originator_id = UNSET
        else:
            originator_id = self.originator_id

        aggregates: list[str] | Unset = UNSET
        if not isinstance(self.aggregates, Unset):
            aggregates = self.aggregates

        ratings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.ratings, Unset):
            ratings = []
            for ratings_item_data in self.ratings:
                ratings_item = ratings_item_data.to_dict()
                ratings.append(ratings_item)

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "name": name,
                "domains": domains,
                "type": type_,
                "rated": rated,
                "consensus": consensus,
                "createdAt": created_at,
            }
        )
        if country is not UNSET:
            field_dict["country"] = country
        if languages is not UNSET:
            field_dict["languages"] = languages
        if owner_org is not UNSET:
            field_dict["ownerOrg"] = owner_org
        if parent_org is not UNSET:
            field_dict["parentOrg"] = parent_org
        if wikidata_qid is not UNSET:
            field_dict["wikidataQid"] = wikidata_qid
        if originator_id is not UNSET:
            field_dict["originatorId"] = originator_id
        if aggregates is not UNSET:
            field_dict["aggregates"] = aggregates
        if ratings is not UNSET:
            field_dict["ratings"] = ratings
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.media_rating import MediaRating
        from ..models.outlet_consensus import OutletConsensus

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        name = d.pop("name")

        domains = cast(list[str], d.pop("domains"))

        type_ = MediaOutletType(d.pop("type"))

        rated = d.pop("rated")

        consensus = []
        _consensus = d.pop("consensus")
        for consensus_item_data in _consensus:
            consensus_item = OutletConsensus.from_dict(consensus_item_data)

            consensus.append(consensus_item)

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_country(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        country = _parse_country(d.pop("country", UNSET))

        languages = cast(list[str], d.pop("languages", UNSET))

        def _parse_owner_org(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        owner_org = _parse_owner_org(d.pop("ownerOrg", UNSET))

        def _parse_parent_org(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        parent_org = _parse_parent_org(d.pop("parentOrg", UNSET))

        def _parse_wikidata_qid(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        wikidata_qid = _parse_wikidata_qid(d.pop("wikidataQid", UNSET))

        def _parse_originator_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        originator_id = _parse_originator_id(d.pop("originatorId", UNSET))

        aggregates = cast(list[str], d.pop("aggregates", UNSET))

        _ratings = d.pop("ratings", UNSET)
        ratings: list[MediaRating] | Unset = UNSET
        if _ratings is not UNSET:
            ratings = []
            for ratings_item_data in _ratings:
                ratings_item = MediaRating.from_dict(ratings_item_data)

                ratings.append(ratings_item)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        media_outlet = cls(
            id=id,
            ref=ref,
            name=name,
            domains=domains,
            type_=type_,
            rated=rated,
            consensus=consensus,
            created_at=created_at,
            country=country,
            languages=languages,
            owner_org=owner_org,
            parent_org=parent_org,
            wikidata_qid=wikidata_qid,
            originator_id=originator_id,
            aggregates=aggregates,
            ratings=ratings,
            updated_at=updated_at,
        )

        media_outlet.additional_properties = d
        return media_outlet

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
