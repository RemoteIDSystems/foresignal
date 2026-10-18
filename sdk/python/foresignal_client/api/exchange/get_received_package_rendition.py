from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_received_package_rendition_format import (
    GetReceivedPackageRenditionFormat,
)
from ...models.problem import Problem
from ...types import UNSET, Response


def _get_kwargs(
    received_id: str,
    *,
    format_: GetReceivedPackageRenditionFormat,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_format_ = format_.value
    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/exchange/received/{received_id}/rendition".format(
            received_id=quote(str(received_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

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
) -> Response[Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    received_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReceivedPackageRenditionFormat,
) -> Response[Problem | str]:
    """Received report rendition

     The received report's stored rendition (HTML, Markdown) or JSON bundle, always marked with its
    origin.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetReceivedPackageRenditionFormat):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        received_id=received_id,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    received_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReceivedPackageRenditionFormat,
) -> Problem | str | None:
    """Received report rendition

     The received report's stored rendition (HTML, Markdown) or JSON bundle, always marked with its
    origin.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetReceivedPackageRenditionFormat):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return sync_detailed(
        received_id=received_id,
        client=client,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    received_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReceivedPackageRenditionFormat,
) -> Response[Problem | str]:
    """Received report rendition

     The received report's stored rendition (HTML, Markdown) or JSON bundle, always marked with its
    origin.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetReceivedPackageRenditionFormat):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        received_id=received_id,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    received_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReceivedPackageRenditionFormat,
) -> Problem | str | None:
    """Received report rendition

     The received report's stored rendition (HTML, Markdown) or JSON bundle, always marked with its
    origin.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetReceivedPackageRenditionFormat):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return (
        await asyncio_detailed(
            received_id=received_id,
            client=client,
            format_=format_,
        )
    ).parsed
