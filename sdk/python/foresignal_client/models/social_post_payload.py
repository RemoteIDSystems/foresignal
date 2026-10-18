from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_post_payload_collection import SocialPostPayloadCollection
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_author import SocialAuthor
    from ..models.social_engagement import SocialEngagement
    from ..models.social_post_geo import SocialPostGeo
    from ..models.social_post_link import SocialPostLink
    from ..models.social_post_media import SocialPostMedia


T = TypeVar("T", bound="SocialPostPayload")


@_attrs_define
class SocialPostPayload:
    """Payload of a `social-post` record (`externalId` = `{platform}:{postId}`, analytic class `social`, 30 d default
    retention per platform) (135)

        Attributes:
            platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
                platform an EDP provider (024, 138) declares Example: bluesky.
            post_id (str): Platform post id (Bluesky AT-URI rkey path, Mastodon `{instance}/{id}`, …)
            author (SocialAuthor): Post author as the platform returns it; accounts are reporting sources, never profiled
                (135)
            author_key (str): HMAC-SHA256 of `{platform}:{accountId}` with the instance social salt; used for distinct-
                author counts and coordination statistics
            created_at (datetime.datetime): Platform creation time
            observed_at (datetime.datetime): When Foresignal saw the post
            collection (SocialPostPayloadCollection): How the post was collected; `cue` posts (137) are a targeted sample
                and never feed baselines
            deleted (bool): Tombstoned by deletion sync: text and media links removed, aggregates kept
            url (None | str | Unset):
            text (None | str | Unset): Null once deleted (tombstoned)
            language (None | str | Unset): BCP 47; platform-supplied, else detected (Lingua, restricted to
                `social.languages`) Example: en.
            reply_to (None | SocialPostLink | Unset):
            quote_of (None | SocialPostLink | Unset):
            repost_of (None | SocialPostLink | Unset):
            links (list[str] | Unset): Expanded URLs
            hashtags (list[str] | Unset):
            mentions (list[str] | Unset): Mentioned handles
            media (list[SocialPostMedia] | Unset):
            engagement (None | SocialEngagement | Unset):
            geo (None | SocialPostGeo | Unset):
            channel (None | str | Unset): Subreddit, Mastodon instance or YouTube channel
            topic_ids (list[str] | Unset): 117 attention topics the post matched
            cue_id (None | str | Unset): 137 cue that found the post (`collection = cue`)
    """

    platform: str
    post_id: str
    author: SocialAuthor
    author_key: str
    created_at: datetime.datetime
    observed_at: datetime.datetime
    collection: SocialPostPayloadCollection
    deleted: bool
    url: None | str | Unset = UNSET
    text: None | str | Unset = UNSET
    language: None | str | Unset = UNSET
    reply_to: None | SocialPostLink | Unset = UNSET
    quote_of: None | SocialPostLink | Unset = UNSET
    repost_of: None | SocialPostLink | Unset = UNSET
    links: list[str] | Unset = UNSET
    hashtags: list[str] | Unset = UNSET
    mentions: list[str] | Unset = UNSET
    media: list[SocialPostMedia] | Unset = UNSET
    engagement: None | SocialEngagement | Unset = UNSET
    geo: None | SocialPostGeo | Unset = UNSET
    channel: None | str | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    cue_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.social_engagement import SocialEngagement
        from ..models.social_post_geo import SocialPostGeo
        from ..models.social_post_link import SocialPostLink

        platform = self.platform

        post_id = self.post_id

        author = self.author.to_dict()

        author_key = self.author_key

        created_at = self.created_at.isoformat()

        observed_at = self.observed_at.isoformat()

        collection = self.collection.value

        deleted = self.deleted

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        text: None | str | Unset
        if isinstance(self.text, Unset):
            text = UNSET
        else:
            text = self.text

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        reply_to: dict[str, Any] | None | Unset
        if isinstance(self.reply_to, Unset):
            reply_to = UNSET
        elif isinstance(self.reply_to, SocialPostLink):
            reply_to = self.reply_to.to_dict()
        else:
            reply_to = self.reply_to

        quote_of: dict[str, Any] | None | Unset
        if isinstance(self.quote_of, Unset):
            quote_of = UNSET
        elif isinstance(self.quote_of, SocialPostLink):
            quote_of = self.quote_of.to_dict()
        else:
            quote_of = self.quote_of

        repost_of: dict[str, Any] | None | Unset
        if isinstance(self.repost_of, Unset):
            repost_of = UNSET
        elif isinstance(self.repost_of, SocialPostLink):
            repost_of = self.repost_of.to_dict()
        else:
            repost_of = self.repost_of

        links: list[str] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links

        hashtags: list[str] | Unset = UNSET
        if not isinstance(self.hashtags, Unset):
            hashtags = self.hashtags

        mentions: list[str] | Unset = UNSET
        if not isinstance(self.mentions, Unset):
            mentions = self.mentions

        media: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.media, Unset):
            media = []
            for media_item_data in self.media:
                media_item = media_item_data.to_dict()
                media.append(media_item)

        engagement: dict[str, Any] | None | Unset
        if isinstance(self.engagement, Unset):
            engagement = UNSET
        elif isinstance(self.engagement, SocialEngagement):
            engagement = self.engagement.to_dict()
        else:
            engagement = self.engagement

        geo: dict[str, Any] | None | Unset
        if isinstance(self.geo, Unset):
            geo = UNSET
        elif isinstance(self.geo, SocialPostGeo):
            geo = self.geo.to_dict()
        else:
            geo = self.geo

        channel: None | str | Unset
        if isinstance(self.channel, Unset):
            channel = UNSET
        else:
            channel = self.channel

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        cue_id: None | str | Unset
        if isinstance(self.cue_id, Unset):
            cue_id = UNSET
        else:
            cue_id = self.cue_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "postId": post_id,
                "author": author,
                "authorKey": author_key,
                "createdAt": created_at,
                "observedAt": observed_at,
                "collection": collection,
                "deleted": deleted,
            }
        )
        if url is not UNSET:
            field_dict["url"] = url
        if text is not UNSET:
            field_dict["text"] = text
        if language is not UNSET:
            field_dict["language"] = language
        if reply_to is not UNSET:
            field_dict["replyTo"] = reply_to
        if quote_of is not UNSET:
            field_dict["quoteOf"] = quote_of
        if repost_of is not UNSET:
            field_dict["repostOf"] = repost_of
        if links is not UNSET:
            field_dict["links"] = links
        if hashtags is not UNSET:
            field_dict["hashtags"] = hashtags
        if mentions is not UNSET:
            field_dict["mentions"] = mentions
        if media is not UNSET:
            field_dict["media"] = media
        if engagement is not UNSET:
            field_dict["engagement"] = engagement
        if geo is not UNSET:
            field_dict["geo"] = geo
        if channel is not UNSET:
            field_dict["channel"] = channel
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if cue_id is not UNSET:
            field_dict["cueId"] = cue_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_author import SocialAuthor
        from ..models.social_engagement import SocialEngagement
        from ..models.social_post_geo import SocialPostGeo
        from ..models.social_post_link import SocialPostLink
        from ..models.social_post_media import SocialPostMedia

        d = dict(src_dict)
        platform = d.pop("platform")

        post_id = d.pop("postId")

        author = SocialAuthor.from_dict(d.pop("author"))

        author_key = d.pop("authorKey")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        collection = SocialPostPayloadCollection(d.pop("collection"))

        deleted = d.pop("deleted")

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_text(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        text = _parse_text(d.pop("text", UNSET))

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        def _parse_reply_to(data: object) -> None | SocialPostLink | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                reply_to_type_0 = SocialPostLink.from_dict(data)

                return reply_to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialPostLink | Unset, data)

        reply_to = _parse_reply_to(d.pop("replyTo", UNSET))

        def _parse_quote_of(data: object) -> None | SocialPostLink | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quote_of_type_0 = SocialPostLink.from_dict(data)

                return quote_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialPostLink | Unset, data)

        quote_of = _parse_quote_of(d.pop("quoteOf", UNSET))

        def _parse_repost_of(data: object) -> None | SocialPostLink | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                repost_of_type_0 = SocialPostLink.from_dict(data)

                return repost_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialPostLink | Unset, data)

        repost_of = _parse_repost_of(d.pop("repostOf", UNSET))

        links = cast(list[str], d.pop("links", UNSET))

        hashtags = cast(list[str], d.pop("hashtags", UNSET))

        mentions = cast(list[str], d.pop("mentions", UNSET))

        _media = d.pop("media", UNSET)
        media: list[SocialPostMedia] | Unset = UNSET
        if _media is not UNSET:
            media = []
            for media_item_data in _media:
                media_item = SocialPostMedia.from_dict(media_item_data)

                media.append(media_item)

        def _parse_engagement(data: object) -> None | SocialEngagement | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                engagement_type_0 = SocialEngagement.from_dict(data)

                return engagement_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialEngagement | Unset, data)

        engagement = _parse_engagement(d.pop("engagement", UNSET))

        def _parse_geo(data: object) -> None | SocialPostGeo | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geo_type_0 = SocialPostGeo.from_dict(data)

                return geo_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialPostGeo | Unset, data)

        geo = _parse_geo(d.pop("geo", UNSET))

        def _parse_channel(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        channel = _parse_channel(d.pop("channel", UNSET))

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        def _parse_cue_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cue_id = _parse_cue_id(d.pop("cueId", UNSET))

        social_post_payload = cls(
            platform=platform,
            post_id=post_id,
            author=author,
            author_key=author_key,
            created_at=created_at,
            observed_at=observed_at,
            collection=collection,
            deleted=deleted,
            url=url,
            text=text,
            language=language,
            reply_to=reply_to,
            quote_of=quote_of,
            repost_of=repost_of,
            links=links,
            hashtags=hashtags,
            mentions=mentions,
            media=media,
            engagement=engagement,
            geo=geo,
            channel=channel,
            topic_ids=topic_ids,
            cue_id=cue_id,
        )

        social_post_payload.additional_properties = d
        return social_post_payload

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
