from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.federation_change_page import FederationChangePage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    after: int | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["after"] = after

    params["limit"] = limit

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/federation/changes",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FederationChangePage | Problem | None:
    if response.status_code == 200:
        response_200 = FederationChangePage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FederationChangePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> Response[FederationChangePage | Problem]:
    """Federation change feed

     Full version items with licence and provenance after `after`, in seq order. Per record: FORBIDDEN
    and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private
    sources and exercise records (unless the token sets includeExercise) are excluded. Restriction
    columns of the token still apply. Below the purge horizon → `410 RESYNC_REQUIRED`.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationChangePage | Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        limit=limit,
        source=source,
        kind=kind,
        bbox=bbox,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> FederationChangePage | Problem | None:
    """Federation change feed

     Full version items with licence and provenance after `after`, in seq order. Per record: FORBIDDEN
    and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private
    sources and exercise records (unless the token sets includeExercise) are excluded. Restriction
    columns of the token still apply. Below the purge horizon → `410 RESYNC_REQUIRED`.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationChangePage | Problem
    """

    return sync_detailed(
        client=client,
        after=after,
        limit=limit,
        source=source,
        kind=kind,
        bbox=bbox,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> Response[FederationChangePage | Problem]:
    """Federation change feed

     Full version items with licence and provenance after `after`, in seq order. Per record: FORBIDDEN
    and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private
    sources and exercise records (unless the token sets includeExercise) are excluded. Restriction
    columns of the token still apply. Below the purge horizon → `410 RESYNC_REQUIRED`.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationChangePage | Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        limit=limit,
        source=source,
        kind=kind,
        bbox=bbox,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> FederationChangePage | Problem | None:
    """Federation change feed

     Full version items with licence and provenance after `after`, in seq order. Per record: FORBIDDEN
    and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private
    sources and exercise records (unless the token sets includeExercise) are excluded. Restriction
    columns of the token still apply. Below the purge horizon → `410 RESYNC_REQUIRED`.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationChangePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            after=after,
            limit=limit,
            source=source,
            kind=kind,
            bbox=bbox,
        )
    ).parsed
