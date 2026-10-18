from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.briefing_run_page import BriefingRunPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    briefing_id: UUID,
    *,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/briefings/{briefing_id}/runs".format(
            briefing_id=quote(str(briefing_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BriefingRunPage | Problem | None:
    if response.status_code == 200:
        response_200 = BriefingRunPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[BriefingRunPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> Response[BriefingRunPage | Problem]:
    """List runs of a briefing

     List runs of a briefing, newest first. The runs of a deleted briefing stay listed until retention
    removes them.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BriefingRunPage | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> BriefingRunPage | Problem | None:
    """List runs of a briefing

     List runs of a briefing, newest first. The runs of a deleted briefing stay listed until retention
    removes them.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BriefingRunPage | Problem
    """

    return sync_detailed(
        briefing_id=briefing_id,
        client=client,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> Response[BriefingRunPage | Problem]:
    """List runs of a briefing

     List runs of a briefing, newest first. The runs of a deleted briefing stay listed until retention
    removes them.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BriefingRunPage | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> BriefingRunPage | Problem | None:
    """List runs of a briefing

     List runs of a briefing, newest first. The runs of a deleted briefing stay listed until retention
    removes them.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BriefingRunPage | Problem
    """

    return (
        await asyncio_detailed(
            briefing_id=briefing_id,
            client=client,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
