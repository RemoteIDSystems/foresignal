from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.hypothesis_revision_page import HypothesisRevisionPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    set_id: str,
    *,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/hypotheses/sets/{set_id}/revisions".format(
            set_id=quote(str(set_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> HypothesisRevisionPage | Problem | None:
    if response.status_code == 200:
        response_200 = HypothesisRevisionPage.from_dict(response.json())

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
) -> Response[HypothesisRevisionPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    set_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[HypothesisRevisionPage | Problem]:
    """List a set's revisions

     Append-only revisions newest first, each with cause, actor, `asOfSeq`, before and after, drivers
    with their log-LR contributions, citations and the change rationale.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HypothesisRevisionPage | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    set_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> HypothesisRevisionPage | Problem | None:
    """List a set's revisions

     Append-only revisions newest first, each with cause, actor, `asOfSeq`, before and after, drivers
    with their log-LR contributions, citations and the change rationale.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HypothesisRevisionPage | Problem
    """

    return sync_detailed(
        set_id=set_id,
        client=client,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    set_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[HypothesisRevisionPage | Problem]:
    """List a set's revisions

     Append-only revisions newest first, each with cause, actor, `asOfSeq`, before and after, drivers
    with their log-LR contributions, citations and the change rationale.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HypothesisRevisionPage | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    set_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> HypothesisRevisionPage | Problem | None:
    """List a set's revisions

     Append-only revisions newest first, each with cause, actor, `asOfSeq`, before and after, drivers
    with their log-LR contributions, citations and the change rationale.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HypothesisRevisionPage | Problem
    """

    return (
        await asyncio_detailed(
            set_id=set_id,
            client=client,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
