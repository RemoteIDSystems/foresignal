import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_social_comparisons_status_item import (
    ListSocialComparisonsStatusItem,
)
from ...models.problem import Problem
from ...models.social_comparison_page import SocialComparisonPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: list[ListSocialComparisonsStatusItem] | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    narrative_id: str | Unset = UNSET,
    claim_id: str | Unset = UNSET,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    params["situationId"] = situation_id

    params["narrativeId"] = narrative_id

    params["claimId"] = claim_id

    params["family"] = family

    params["region"] = region

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/comparisons",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialComparisonPage | None:
    if response.status_code == 200:
        response_200 = SocialComparisonPage.from_dict(response.json())

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
) -> Response[Problem | SocialComparisonPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialComparisonsStatusItem] | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    narrative_id: str | Unset = UNSET,
    claim_id: str | Unset = UNSET,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SocialComparisonPage]:
    """List social–physical comparisons

     Claims compared with independent physical evidence: `corroborated` (with lead times), `unobserved`
    (only when `canSeeAbsence` is ok), `contradicted`, `insufficient-coverage` or `pending`.
    `unobserved` means no corresponding change despite adequate coverage, never "false".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialComparisonsStatusItem] | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        claim_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str | Unset):
        region (str | Unset): 040 region code Example: YE.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialComparisonPage]
    """

    kwargs = _get_kwargs(
        status=status,
        situation_id=situation_id,
        narrative_id=narrative_id,
        claim_id=claim_id,
        family=family,
        region=region,
        since=since,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialComparisonsStatusItem] | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    narrative_id: str | Unset = UNSET,
    claim_id: str | Unset = UNSET,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SocialComparisonPage | None:
    """List social–physical comparisons

     Claims compared with independent physical evidence: `corroborated` (with lead times), `unobserved`
    (only when `canSeeAbsence` is ok), `contradicted`, `insufficient-coverage` or `pending`.
    `unobserved` means no corresponding change despite adequate coverage, never "false".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialComparisonsStatusItem] | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        claim_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str | Unset):
        region (str | Unset): 040 region code Example: YE.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialComparisonPage
    """

    return sync_detailed(
        client=client,
        status=status,
        situation_id=situation_id,
        narrative_id=narrative_id,
        claim_id=claim_id,
        family=family,
        region=region,
        since=since,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialComparisonsStatusItem] | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    narrative_id: str | Unset = UNSET,
    claim_id: str | Unset = UNSET,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SocialComparisonPage]:
    """List social–physical comparisons

     Claims compared with independent physical evidence: `corroborated` (with lead times), `unobserved`
    (only when `canSeeAbsence` is ok), `contradicted`, `insufficient-coverage` or `pending`.
    `unobserved` means no corresponding change despite adequate coverage, never "false".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialComparisonsStatusItem] | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        claim_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str | Unset):
        region (str | Unset): 040 region code Example: YE.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialComparisonPage]
    """

    kwargs = _get_kwargs(
        status=status,
        situation_id=situation_id,
        narrative_id=narrative_id,
        claim_id=claim_id,
        family=family,
        region=region,
        since=since,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialComparisonsStatusItem] | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    narrative_id: str | Unset = UNSET,
    claim_id: str | Unset = UNSET,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SocialComparisonPage | None:
    """List social–physical comparisons

     Claims compared with independent physical evidence: `corroborated` (with lead times), `unobserved`
    (only when `canSeeAbsence` is ok), `contradicted`, `insufficient-coverage` or `pending`.
    `unobserved` means no corresponding change despite adequate coverage, never "false".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialComparisonsStatusItem] | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        claim_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str | Unset):
        region (str | Unset): 040 region code Example: YE.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialComparisonPage
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            situation_id=situation_id,
            narrative_id=narrative_id,
            claim_id=claim_id,
            family=family,
            region=region,
            since=since,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
