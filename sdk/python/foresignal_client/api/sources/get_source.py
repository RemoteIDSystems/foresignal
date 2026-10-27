from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.source_detail import SourceDetail
from ...types import Response


def _get_kwargs(
    source_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sources/{source_id}".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SourceDetail | None:
    if response.status_code == 200:
        response_200 = SourceDetail.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
) -> Response[Problem | SourceDetail]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SourceDetail]:
    """Get a source

     Get a source

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user):** a source's *enablement and display* are now the caller's own per-user view
    (getMySources/updateMySources) over the shared collection; the instance collection set is managed
    with `collection.manage` (getCollectionSources). The source descriptor and health stay global.
    Request and response shapes are unchanged.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SourceDetail]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SourceDetail | None:
    """Get a source

     Get a source

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user):** a source's *enablement and display* are now the caller's own per-user view
    (getMySources/updateMySources) over the shared collection; the instance collection set is managed
    with `collection.manage` (getCollectionSources). The source descriptor and health stay global.
    Request and response shapes are unchanged.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SourceDetail
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SourceDetail]:
    """Get a source

     Get a source

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user):** a source's *enablement and display* are now the caller's own per-user view
    (getMySources/updateMySources) over the shared collection; the instance collection set is managed
    with `collection.manage` (getCollectionSources). The source descriptor and health stay global.
    Request and response shapes are unchanged.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SourceDetail]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SourceDetail | None:
    """Get a source

     Get a source

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user):** a source's *enablement and display* are now the caller's own per-user view
    (getMySources/updateMySources) over the shared collection; the instance collection set is managed
    with `collection.manage` (getCollectionSources). The source descriptor and health stay global.
    Request and response shapes are unchanged.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SourceDetail
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
        )
    ).parsed
