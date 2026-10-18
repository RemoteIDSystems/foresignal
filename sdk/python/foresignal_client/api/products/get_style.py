from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.style import Style
from ...types import Response


def _get_kwargs(
    style_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/styles/{style_id}".format(
            style_id=quote(str(style_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Style | None:
    if response.status_code == 200:
        response_200 = Style.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Style]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    style_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | Style]:
    """Get a style

     One style version; an unversioned name resolves to the current version. Unknown → 400 UNKNOWN_STYLE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        style_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Style]
    """

    kwargs = _get_kwargs(
        style_id=style_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    style_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | Style | None:
    """Get a style

     One style version; an unversioned name resolves to the current version. Unknown → 400 UNKNOWN_STYLE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        style_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Style
    """

    return sync_detailed(
        style_id=style_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    style_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | Style]:
    """Get a style

     One style version; an unversioned name resolves to the current version. Unknown → 400 UNKNOWN_STYLE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        style_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Style]
    """

    kwargs = _get_kwargs(
        style_id=style_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    style_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | Style | None:
    """Get a style

     One style version; an unversioned name resolves to the current version. Unknown → 400 UNKNOWN_STYLE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        style_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Style
    """

    return (
        await asyncio_detailed(
            style_id=style_id,
            client=client,
        )
    ).parsed
