from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_related_page import ChronicleRelatedPage
from ...models.list_related_chronicles_min_strength import (
    ListRelatedChroniclesMinStrength,
)
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    *,
    min_strength: ListRelatedChroniclesMinStrength | Unset = UNSET,
    include_dismissed: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_min_strength: str | Unset = UNSET
    if not isinstance(min_strength, Unset):
        json_min_strength = min_strength.value

    params["minStrength"] = json_min_strength

    params["includeDismissed"] = include_dismissed

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/related".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleRelatedPage | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleRelatedPage.from_dict(response.json())

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
) -> Response[ChronicleRelatedPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    min_strength: ListRelatedChroniclesMinStrength | Unset = UNSET,
    include_dismissed: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChronicleRelatedPage | Problem]:
    """Related chronicles

     Links of this chronicle at the disclosure level the caller is allowed: full (both visible: scores,
    shared canonical events and entities, computed exactly from both key sets) or opaque (strength band,
    dimensions, time overlap; title and owner only if revealed). A principal is shown at most 20 new
    opaque links per day; links shown once stay visible.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        min_strength (ListRelatedChroniclesMinStrength | Unset): From estimated Jaccard: low < 0.1
            ≤ medium < 0.3 ≤ high
        include_dismissed (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleRelatedPage | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        min_strength=min_strength,
        include_dismissed=include_dismissed,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    min_strength: ListRelatedChroniclesMinStrength | Unset = UNSET,
    include_dismissed: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChronicleRelatedPage | Problem | None:
    """Related chronicles

     Links of this chronicle at the disclosure level the caller is allowed: full (both visible: scores,
    shared canonical events and entities, computed exactly from both key sets) or opaque (strength band,
    dimensions, time overlap; title and owner only if revealed). A principal is shown at most 20 new
    opaque links per day; links shown once stay visible.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        min_strength (ListRelatedChroniclesMinStrength | Unset): From estimated Jaccard: low < 0.1
            ≤ medium < 0.3 ≤ high
        include_dismissed (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleRelatedPage | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        min_strength=min_strength,
        include_dismissed=include_dismissed,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    min_strength: ListRelatedChroniclesMinStrength | Unset = UNSET,
    include_dismissed: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChronicleRelatedPage | Problem]:
    """Related chronicles

     Links of this chronicle at the disclosure level the caller is allowed: full (both visible: scores,
    shared canonical events and entities, computed exactly from both key sets) or opaque (strength band,
    dimensions, time overlap; title and owner only if revealed). A principal is shown at most 20 new
    opaque links per day; links shown once stay visible.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        min_strength (ListRelatedChroniclesMinStrength | Unset): From estimated Jaccard: low < 0.1
            ≤ medium < 0.3 ≤ high
        include_dismissed (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleRelatedPage | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        min_strength=min_strength,
        include_dismissed=include_dismissed,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    min_strength: ListRelatedChroniclesMinStrength | Unset = UNSET,
    include_dismissed: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChronicleRelatedPage | Problem | None:
    """Related chronicles

     Links of this chronicle at the disclosure level the caller is allowed: full (both visible: scores,
    shared canonical events and entities, computed exactly from both key sets) or opaque (strength band,
    dimensions, time overlap; title and owner only if revealed). A principal is shown at most 20 new
    opaque links per day; links shown once stay visible.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        min_strength (ListRelatedChroniclesMinStrength | Unset): From estimated Jaccard: low < 0.1
            ≤ medium < 0.3 ≤ high
        include_dismissed (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleRelatedPage | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            min_strength=min_strength,
            include_dismissed=include_dismissed,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
