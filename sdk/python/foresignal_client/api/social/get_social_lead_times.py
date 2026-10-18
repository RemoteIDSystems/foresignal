import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_social_lead_times_found_by import GetSocialLeadTimesFoundBy
from ...models.lead_time_stats import LeadTimeStats
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    claim_type: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    region: str | Unset = UNSET,
    found_by: GetSocialLeadTimesFoundBy | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["claimType"] = claim_type

    params["platform"] = platform

    params["region"] = region

    json_found_by: str | Unset = UNSET
    if not isinstance(found_by, Unset):
        json_found_by = found_by.value

    params["foundBy"] = json_found_by

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/lead-times",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LeadTimeStats | Problem | None:
    if response.status_code == 200:
        response_200 = LeadTimeStats.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[LeadTimeStats | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    claim_type: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    region: str | Unset = UNSET,
    found_by: GetSocialLeadTimesFoundBy | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Response[LeadTimeStats | Problem]:
    """Get social lead-time statistics

     Median and interquartile lead of social first observation over physical, news and official first
    observation for corroborated comparisons, by claim type, platform, region and `foundBy`, with
    counts. Only comparisons evaluated after the training cut-off support "leads by" statements; a
    single case is an observed lead time.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        claim_type (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        region (str | Unset): 040 region code Example: YE.
        found_by (GetSocialLeadTimesFoundBy | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LeadTimeStats | Problem]
    """

    kwargs = _get_kwargs(
        claim_type=claim_type,
        platform=platform,
        region=region,
        found_by=found_by,
        since=since,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    claim_type: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    region: str | Unset = UNSET,
    found_by: GetSocialLeadTimesFoundBy | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> LeadTimeStats | Problem | None:
    """Get social lead-time statistics

     Median and interquartile lead of social first observation over physical, news and official first
    observation for corroborated comparisons, by claim type, platform, region and `foundBy`, with
    counts. Only comparisons evaluated after the training cut-off support "leads by" statements; a
    single case is an observed lead time.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        claim_type (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        region (str | Unset): 040 region code Example: YE.
        found_by (GetSocialLeadTimesFoundBy | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LeadTimeStats | Problem
    """

    return sync_detailed(
        client=client,
        claim_type=claim_type,
        platform=platform,
        region=region,
        found_by=found_by,
        since=since,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    claim_type: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    region: str | Unset = UNSET,
    found_by: GetSocialLeadTimesFoundBy | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Response[LeadTimeStats | Problem]:
    """Get social lead-time statistics

     Median and interquartile lead of social first observation over physical, news and official first
    observation for corroborated comparisons, by claim type, platform, region and `foundBy`, with
    counts. Only comparisons evaluated after the training cut-off support "leads by" statements; a
    single case is an observed lead time.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        claim_type (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        region (str | Unset): 040 region code Example: YE.
        found_by (GetSocialLeadTimesFoundBy | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LeadTimeStats | Problem]
    """

    kwargs = _get_kwargs(
        claim_type=claim_type,
        platform=platform,
        region=region,
        found_by=found_by,
        since=since,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    claim_type: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    region: str | Unset = UNSET,
    found_by: GetSocialLeadTimesFoundBy | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> LeadTimeStats | Problem | None:
    """Get social lead-time statistics

     Median and interquartile lead of social first observation over physical, news and official first
    observation for corroborated comparisons, by claim type, platform, region and `foundBy`, with
    counts. Only comparisons evaluated after the training cut-off support "leads by" statements; a
    single case is an observed lead time.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        claim_type (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        region (str | Unset): 040 region code Example: YE.
        found_by (GetSocialLeadTimesFoundBy | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LeadTimeStats | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            claim_type=claim_type,
            platform=platform,
            region=region,
            found_by=found_by,
            since=since,
        )
    ).parsed
