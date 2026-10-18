from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.attention_topic_social_mode import AttentionTopicSocialMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attention_topic_social_accounts_item import (
        AttentionTopicSocialAccountsItem,
    )
    from ..models.attention_topic_social_channel import AttentionTopicSocialChannel
    from ..models.attention_topic_social_terms import AttentionTopicSocialTerms


T = TypeVar("T", bound="AttentionTopicSocial")


@_attrs_define
class AttentionTopicSocial:
    """Social tracking block of an attention topic (135). A post matches when it matches a term, hashtag, account or
    channel and no exclude term (case- and diacritic-folded, word-boundary aware)

        Attributes:
            terms (AttentionTopicSocialTerms | Unset): Terms per language (BCP 47 → phrases); at most 50 in total Example:
                {'en': ['Bab el-Mandeb', 'Houthi strike'], 'ar': ['باب المندب']}.
            hashtags (list[str] | Unset):
            accounts (list[AttentionTopicSocialAccountsItem] | Unset):
            channels (list[AttentionTopicSocialChannel] | Unset):
            exclude (list[str] | Unset): Exclude terms
            platforms (list[str] | Unset): Subset of networks; empty = all enabled
            place_ids (list[str] | Unset): 036 places whose names and aliases are added as terms
            region_codes (list[str] | Unset): 040 regions whose names and aliases are added as terms
            entity_refs (list[str] | Unset): 098 entities whose names and identifiers are terms
            mode (AttentionTopicSocialMode | Unset): `standing` collects continuously; `cue-only` only on cues (137)
                Default: AttentionTopicSocialMode.STANDING.
    """

    terms: AttentionTopicSocialTerms | Unset = UNSET
    hashtags: list[str] | Unset = UNSET
    accounts: list[AttentionTopicSocialAccountsItem] | Unset = UNSET
    channels: list[AttentionTopicSocialChannel] | Unset = UNSET
    exclude: list[str] | Unset = UNSET
    platforms: list[str] | Unset = UNSET
    place_ids: list[str] | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    entity_refs: list[str] | Unset = UNSET
    mode: AttentionTopicSocialMode | Unset = AttentionTopicSocialMode.STANDING
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        terms: dict[str, Any] | Unset = UNSET
        if not isinstance(self.terms, Unset):
            terms = self.terms.to_dict()

        hashtags: list[str] | Unset = UNSET
        if not isinstance(self.hashtags, Unset):
            hashtags = self.hashtags

        accounts: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.accounts, Unset):
            accounts = []
            for accounts_item_data in self.accounts:
                accounts_item = accounts_item_data.to_dict()
                accounts.append(accounts_item)

        channels: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.channels, Unset):
            channels = []
            for channels_item_data in self.channels:
                channels_item = channels_item_data.to_dict()
                channels.append(channels_item)

        exclude: list[str] | Unset = UNSET
        if not isinstance(self.exclude, Unset):
            exclude = self.exclude

        platforms: list[str] | Unset = UNSET
        if not isinstance(self.platforms, Unset):
            platforms = self.platforms

        place_ids: list[str] | Unset = UNSET
        if not isinstance(self.place_ids, Unset):
            place_ids = self.place_ids

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        entity_refs: list[str] | Unset = UNSET
        if not isinstance(self.entity_refs, Unset):
            entity_refs = self.entity_refs

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if terms is not UNSET:
            field_dict["terms"] = terms
        if hashtags is not UNSET:
            field_dict["hashtags"] = hashtags
        if accounts is not UNSET:
            field_dict["accounts"] = accounts
        if channels is not UNSET:
            field_dict["channels"] = channels
        if exclude is not UNSET:
            field_dict["exclude"] = exclude
        if platforms is not UNSET:
            field_dict["platforms"] = platforms
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if entity_refs is not UNSET:
            field_dict["entityRefs"] = entity_refs
        if mode is not UNSET:
            field_dict["mode"] = mode

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attention_topic_social_accounts_item import (
            AttentionTopicSocialAccountsItem,
        )
        from ..models.attention_topic_social_channel import AttentionTopicSocialChannel
        from ..models.attention_topic_social_terms import AttentionTopicSocialTerms

        d = dict(src_dict)
        _terms = d.pop("terms", UNSET)
        terms: AttentionTopicSocialTerms | Unset
        if isinstance(_terms, Unset):
            terms = UNSET
        else:
            terms = AttentionTopicSocialTerms.from_dict(_terms)

        hashtags = cast(list[str], d.pop("hashtags", UNSET))

        _accounts = d.pop("accounts", UNSET)
        accounts: list[AttentionTopicSocialAccountsItem] | Unset = UNSET
        if _accounts is not UNSET:
            accounts = []
            for accounts_item_data in _accounts:
                accounts_item = AttentionTopicSocialAccountsItem.from_dict(
                    accounts_item_data
                )

                accounts.append(accounts_item)

        _channels = d.pop("channels", UNSET)
        channels: list[AttentionTopicSocialChannel] | Unset = UNSET
        if _channels is not UNSET:
            channels = []
            for channels_item_data in _channels:
                channels_item = AttentionTopicSocialChannel.from_dict(
                    channels_item_data
                )

                channels.append(channels_item)

        exclude = cast(list[str], d.pop("exclude", UNSET))

        platforms = cast(list[str], d.pop("platforms", UNSET))

        place_ids = cast(list[str], d.pop("placeIds", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        entity_refs = cast(list[str], d.pop("entityRefs", UNSET))

        _mode = d.pop("mode", UNSET)
        mode: AttentionTopicSocialMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = AttentionTopicSocialMode(_mode)

        attention_topic_social = cls(
            terms=terms,
            hashtags=hashtags,
            accounts=accounts,
            channels=channels,
            exclude=exclude,
            platforms=platforms,
            place_ids=place_ids,
            region_codes=region_codes,
            entity_refs=entity_refs,
            mode=mode,
        )

        attention_topic_social.additional_properties = d
        return attention_topic_social

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
