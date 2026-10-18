from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_social_narratives_selector_statuses_item import (
    ChronicleSocialNarrativesSelectorStatusesItem,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleSocialNarrativesSelector")


@_attrs_define
class ChronicleSocialNarrativesSelector:
    """Chronicle layer `social-narratives` (137): narratives, claims and comparisons matching topics, places or situations;
    history kept after posts expire

        Attributes:
            type_ (Literal['social-narratives']):
            topic_ids (list[str] | Unset):
            place_ids (list[str] | Unset):
            region_codes (list[str] | Unset):
            situation_ids (list[str] | Unset):
            statuses (list[ChronicleSocialNarrativesSelectorStatusesItem] | Unset):
            keep_post_text (bool | Unset): Keep member post text in chronicle storage (else claim summaries and counts only)
                Default: False.
    """

    type_: Literal["social-narratives"]
    topic_ids: list[str] | Unset = UNSET
    place_ids: list[str] | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    situation_ids: list[str] | Unset = UNSET
    statuses: list[ChronicleSocialNarrativesSelectorStatusesItem] | Unset = UNSET
    keep_post_text: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        place_ids: list[str] | Unset = UNSET
        if not isinstance(self.place_ids, Unset):
            place_ids = self.place_ids

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        statuses: list[str] | Unset = UNSET
        if not isinstance(self.statuses, Unset):
            statuses = []
            for statuses_item_data in self.statuses:
                statuses_item = statuses_item_data.value
                statuses.append(statuses_item)

        keep_post_text = self.keep_post_text

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if statuses is not UNSET:
            field_dict["statuses"] = statuses
        if keep_post_text is not UNSET:
            field_dict["keepPostText"] = keep_post_text

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["social-narratives"], d.pop("type"))
        if type_ != "social-narratives":
            raise ValueError(
                f"type must match const 'social-narratives', got '{type_}'"
            )

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        place_ids = cast(list[str], d.pop("placeIds", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        _statuses = d.pop("statuses", UNSET)
        statuses: list[ChronicleSocialNarrativesSelectorStatusesItem] | Unset = UNSET
        if _statuses is not UNSET:
            statuses = []
            for statuses_item_data in _statuses:
                statuses_item = ChronicleSocialNarrativesSelectorStatusesItem(
                    statuses_item_data
                )

                statuses.append(statuses_item)

        keep_post_text = d.pop("keepPostText", UNSET)

        chronicle_social_narratives_selector = cls(
            type_=type_,
            topic_ids=topic_ids,
            place_ids=place_ids,
            region_codes=region_codes,
            situation_ids=situation_ids,
            statuses=statuses,
            keep_post_text=keep_post_text,
        )

        chronicle_social_narratives_selector.additional_properties = d
        return chronicle_social_narratives_selector

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
