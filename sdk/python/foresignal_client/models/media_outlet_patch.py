from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.media_outlet_patch_type import MediaOutletPatchType
from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaOutletPatch")


@_attrs_define
class MediaOutletPatch:
    """JSON merge patch for an outlet (admin)

    Attributes:
        name (str | Unset):
        type_ (MediaOutletPatchType | Unset):
        owner_org (None | str | Unset):
        parent_org (None | str | Unset):
        wikidata_qid (None | str | Unset):
        domains (list[str] | Unset): Replaces the domain list
        merge_outlet_ids (list[str] | Unset): Merge these outlets (and their domains and ratings) into this one
        aggregates (list[str] | Unset): Wire originators this outlet syndicates (empty: any wire); used to group
            reprints (133)
    """

    name: str | Unset = UNSET
    type_: MediaOutletPatchType | Unset = UNSET
    owner_org: None | str | Unset = UNSET
    parent_org: None | str | Unset = UNSET
    wikidata_qid: None | str | Unset = UNSET
    domains: list[str] | Unset = UNSET
    merge_outlet_ids: list[str] | Unset = UNSET
    aggregates: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        type_: str | Unset = UNSET
        if not isinstance(self.type_, Unset):
            type_ = self.type_.value

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

        domains: list[str] | Unset = UNSET
        if not isinstance(self.domains, Unset):
            domains = self.domains

        merge_outlet_ids: list[str] | Unset = UNSET
        if not isinstance(self.merge_outlet_ids, Unset):
            merge_outlet_ids = self.merge_outlet_ids

        aggregates: list[str] | Unset = UNSET
        if not isinstance(self.aggregates, Unset):
            aggregates = self.aggregates

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if type_ is not UNSET:
            field_dict["type"] = type_
        if owner_org is not UNSET:
            field_dict["ownerOrg"] = owner_org
        if parent_org is not UNSET:
            field_dict["parentOrg"] = parent_org
        if wikidata_qid is not UNSET:
            field_dict["wikidataQid"] = wikidata_qid
        if domains is not UNSET:
            field_dict["domains"] = domains
        if merge_outlet_ids is not UNSET:
            field_dict["mergeOutletIds"] = merge_outlet_ids
        if aggregates is not UNSET:
            field_dict["aggregates"] = aggregates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name", UNSET)

        _type_ = d.pop("type", UNSET)
        type_: MediaOutletPatchType | Unset
        if isinstance(_type_, Unset):
            type_ = UNSET
        else:
            type_ = MediaOutletPatchType(_type_)

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

        domains = cast(list[str], d.pop("domains", UNSET))

        merge_outlet_ids = cast(list[str], d.pop("mergeOutletIds", UNSET))

        aggregates = cast(list[str], d.pop("aggregates", UNSET))

        media_outlet_patch = cls(
            name=name,
            type_=type_,
            owner_org=owner_org,
            parent_org=parent_org,
            wikidata_qid=wikidata_qid,
            domains=domains,
            merge_outlet_ids=merge_outlet_ids,
            aggregates=aggregates,
        )

        media_outlet_patch.additional_properties = d
        return media_outlet_patch

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
