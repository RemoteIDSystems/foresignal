from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    key: str,
    *,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/k/{key}/site".format(
            key=quote(str(key), safe=""),
        ),
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

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
) -> Response[Any | Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    key: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | str]:
    """Open a shared edition site

     The hosted site of an edition (NP-005, `getEditionSite`) behind its share link: the latest
    successful run as a standalone HTML page, read-only. The share key grants this page only, never
    writes, other editions or records. A run citing private or non-redistributable sources is not
    served. `401` for an unknown key and `410 SHARE_EXPIRED` when the link is revoked, expired or
    exhausted; `404` when the link is to another target type, the edition was deleted, its site is off,
    or it has no successful run yet. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `open` (share key in the path; `/v1/k/{key}/…`).

    Args:
        key (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        key=key,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    key: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | str | None:
    """Open a shared edition site

     The hosted site of an edition (NP-005, `getEditionSite`) behind its share link: the latest
    successful run as a standalone HTML page, read-only. The share key grants this page only, never
    writes, other editions or records. A run citing private or non-redistributable sources is not
    served. `401` for an unknown key and `410 SHARE_EXPIRED` when the link is revoked, expired or
    exhausted; `404` when the link is to another target type, the edition was deleted, its site is off,
    or it has no successful run yet. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `open` (share key in the path; `/v1/k/{key}/…`).

    Args:
        key (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return sync_detailed(
        key=key,
        client=client,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    key: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | str]:
    """Open a shared edition site

     The hosted site of an edition (NP-005, `getEditionSite`) behind its share link: the latest
    successful run as a standalone HTML page, read-only. The share key grants this page only, never
    writes, other editions or records. A run citing private or non-redistributable sources is not
    served. `401` for an unknown key and `410 SHARE_EXPIRED` when the link is revoked, expired or
    exhausted; `404` when the link is to another target type, the edition was deleted, its site is off,
    or it has no successful run yet. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `open` (share key in the path; `/v1/k/{key}/…`).

    Args:
        key (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        key=key,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    key: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | str | None:
    """Open a shared edition site

     The hosted site of an edition (NP-005, `getEditionSite`) behind its share link: the latest
    successful run as a standalone HTML page, read-only. The share key grants this page only, never
    writes, other editions or records. A run citing private or non-redistributable sources is not
    served. `401` for an unknown key and `410 SHARE_EXPIRED` when the link is revoked, expired or
    exhausted; `404` when the link is to another target type, the edition was deleted, its site is off,
    or it has no successful run yet. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `open` (share key in the path; `/v1/k/{key}/…`).

    Args:
        key (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return (
        await asyncio_detailed(
            key=key,
            client=client,
            if_none_match=if_none_match,
        )
    ).parsed
