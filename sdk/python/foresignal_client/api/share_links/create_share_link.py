from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.share_link_create import ShareLinkCreate
from ...models.share_link_created import ShareLinkCreated
from ...types import Response


def _get_kwargs(
    *,
    body: ShareLinkCreate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/share-links",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ShareLinkCreated | None:
    if response.status_code == 201:
        response_201 = ShareLinkCreated.from_dict(response.json())

        return response_201

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ShareLinkCreated]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ShareLinkCreate,
) -> Response[Problem | ShareLinkCreated]:
    """Create a share link

     Creates an expiring, path-bound link (default 7 d, max 30 d); the plaintext `fss_` key is returned
    once. Personal or private sources are masked by default (precision ≤ 1,000 m, observer, device and
    address-like fields removed, payload reduced unless an allowlist is given); private targets need
    admin. Records whose redistribution is not ALLOWED are removed; a target that is such a record is
    refused with `409 SHARE_LICENCE_FORBIDDEN`. Audited.

    **Scope:** `agent`.

    Args:
        body (ShareLinkCreate): Create a path-bound share link (112). Personal and private sources
            are masked by default

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ShareLinkCreated]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ShareLinkCreate,
) -> Problem | ShareLinkCreated | None:
    """Create a share link

     Creates an expiring, path-bound link (default 7 d, max 30 d); the plaintext `fss_` key is returned
    once. Personal or private sources are masked by default (precision ≤ 1,000 m, observer, device and
    address-like fields removed, payload reduced unless an allowlist is given); private targets need
    admin. Records whose redistribution is not ALLOWED are removed; a target that is such a record is
    refused with `409 SHARE_LICENCE_FORBIDDEN`. Audited.

    **Scope:** `agent`.

    Args:
        body (ShareLinkCreate): Create a path-bound share link (112). Personal and private sources
            are masked by default

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ShareLinkCreated
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ShareLinkCreate,
) -> Response[Problem | ShareLinkCreated]:
    """Create a share link

     Creates an expiring, path-bound link (default 7 d, max 30 d); the plaintext `fss_` key is returned
    once. Personal or private sources are masked by default (precision ≤ 1,000 m, observer, device and
    address-like fields removed, payload reduced unless an allowlist is given); private targets need
    admin. Records whose redistribution is not ALLOWED are removed; a target that is such a record is
    refused with `409 SHARE_LICENCE_FORBIDDEN`. Audited.

    **Scope:** `agent`.

    Args:
        body (ShareLinkCreate): Create a path-bound share link (112). Personal and private sources
            are masked by default

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ShareLinkCreated]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ShareLinkCreate,
) -> Problem | ShareLinkCreated | None:
    """Create a share link

     Creates an expiring, path-bound link (default 7 d, max 30 d); the plaintext `fss_` key is returned
    once. Personal or private sources are masked by default (precision ≤ 1,000 m, observer, device and
    address-like fields removed, payload reduced unless an allowlist is given); private targets need
    admin. Records whose redistribution is not ALLOWED are removed; a target that is such a record is
    refused with `409 SHARE_LICENCE_FORBIDDEN`. Audited.

    **Scope:** `agent`.

    Args:
        body (ShareLinkCreate): Create a path-bound share link (112). Personal and private sources
            are masked by default

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ShareLinkCreated
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
