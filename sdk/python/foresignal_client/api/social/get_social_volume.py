import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_social_volume_interval import GetSocialVolumeInterval
from ...models.get_social_volume_metric import GetSocialVolumeMetric
from ...models.problem import Problem
from ...models.social_volume import SocialVolume
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    topic: str | Unset = UNSET,
    place: str | Unset = UNSET,
    entity: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    language: str | Unset = UNSET,
    metric: GetSocialVolumeMetric | Unset = GetSocialVolumeMetric.POSTS,
    interval: GetSocialVolumeInterval | Unset = GetSocialVolumeInterval.PT1H,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["topic"] = topic

    params["place"] = place

    params["entity"] = entity

    params["platform"] = platform

    params["language"] = language

    json_metric: str | Unset = UNSET
    if not isinstance(metric, Unset):
        json_metric = metric.value

    params["metric"] = json_metric

    json_interval: str | Unset = UNSET
    if not isinstance(interval, Unset):
        json_interval = interval.value

    params["interval"] = json_interval

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/volume",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialVolume | None:
    if response.status_code == 200:
        response_200 = SocialVolume.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SocialVolume]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    place: str | Unset = UNSET,
    entity: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    language: str | Unset = UNSET,
    metric: GetSocialVolumeMetric | Unset = GetSocialVolumeMetric.POSTS,
    interval: GetSocialVolumeInterval | Unset = GetSocialVolumeInterval.PT1H,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
) -> Response[Problem | SocialVolume]:
    """Get social mention volume

     Hourly or daily volume of exactly one topic, place or entity per platform (and language) with the
    seasonal, denominator-scaled baseline, velocity ratio, robust z and complete flag. Series from
    sampled paths carry no baseline. 400 VALIDATION_FAILED unless exactly one of `topic`, `place`,
    `entity` is given.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        place (str | Unset):
        entity (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        language (str | Unset):
        metric (GetSocialVolumeMetric | Unset):  Default: GetSocialVolumeMetric.POSTS.
        interval (GetSocialVolumeInterval | Unset):  Default: GetSocialVolumeInterval.PT1H.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialVolume]
    """

    kwargs = _get_kwargs(
        topic=topic,
        place=place,
        entity=entity,
        platform=platform,
        language=language,
        metric=metric,
        interval=interval,
        since=since,
        until=until,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    place: str | Unset = UNSET,
    entity: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    language: str | Unset = UNSET,
    metric: GetSocialVolumeMetric | Unset = GetSocialVolumeMetric.POSTS,
    interval: GetSocialVolumeInterval | Unset = GetSocialVolumeInterval.PT1H,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
) -> Problem | SocialVolume | None:
    """Get social mention volume

     Hourly or daily volume of exactly one topic, place or entity per platform (and language) with the
    seasonal, denominator-scaled baseline, velocity ratio, robust z and complete flag. Series from
    sampled paths carry no baseline. 400 VALIDATION_FAILED unless exactly one of `topic`, `place`,
    `entity` is given.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        place (str | Unset):
        entity (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        language (str | Unset):
        metric (GetSocialVolumeMetric | Unset):  Default: GetSocialVolumeMetric.POSTS.
        interval (GetSocialVolumeInterval | Unset):  Default: GetSocialVolumeInterval.PT1H.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialVolume
    """

    return sync_detailed(
        client=client,
        topic=topic,
        place=place,
        entity=entity,
        platform=platform,
        language=language,
        metric=metric,
        interval=interval,
        since=since,
        until=until,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    place: str | Unset = UNSET,
    entity: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    language: str | Unset = UNSET,
    metric: GetSocialVolumeMetric | Unset = GetSocialVolumeMetric.POSTS,
    interval: GetSocialVolumeInterval | Unset = GetSocialVolumeInterval.PT1H,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
) -> Response[Problem | SocialVolume]:
    """Get social mention volume

     Hourly or daily volume of exactly one topic, place or entity per platform (and language) with the
    seasonal, denominator-scaled baseline, velocity ratio, robust z and complete flag. Series from
    sampled paths carry no baseline. 400 VALIDATION_FAILED unless exactly one of `topic`, `place`,
    `entity` is given.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        place (str | Unset):
        entity (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        language (str | Unset):
        metric (GetSocialVolumeMetric | Unset):  Default: GetSocialVolumeMetric.POSTS.
        interval (GetSocialVolumeInterval | Unset):  Default: GetSocialVolumeInterval.PT1H.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialVolume]
    """

    kwargs = _get_kwargs(
        topic=topic,
        place=place,
        entity=entity,
        platform=platform,
        language=language,
        metric=metric,
        interval=interval,
        since=since,
        until=until,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    place: str | Unset = UNSET,
    entity: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    language: str | Unset = UNSET,
    metric: GetSocialVolumeMetric | Unset = GetSocialVolumeMetric.POSTS,
    interval: GetSocialVolumeInterval | Unset = GetSocialVolumeInterval.PT1H,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
) -> Problem | SocialVolume | None:
    """Get social mention volume

     Hourly or daily volume of exactly one topic, place or entity per platform (and language) with the
    seasonal, denominator-scaled baseline, velocity ratio, robust z and complete flag. Series from
    sampled paths carry no baseline. 400 VALIDATION_FAILED unless exactly one of `topic`, `place`,
    `entity` is given.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        place (str | Unset):
        entity (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        language (str | Unset):
        metric (GetSocialVolumeMetric | Unset):  Default: GetSocialVolumeMetric.POSTS.
        interval (GetSocialVolumeInterval | Unset):  Default: GetSocialVolumeInterval.PT1H.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialVolume
    """

    return (
        await asyncio_detailed(
            client=client,
            topic=topic,
            place=place,
            entity=entity,
            platform=platform,
            language=language,
            metric=metric,
            interval=interval,
            since=since,
            until=until,
        )
    ).parsed
