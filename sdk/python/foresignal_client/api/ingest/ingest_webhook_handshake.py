from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    format_: str,
    *,
    web_hook_request_origin: str,
    web_hook_request_rate: str | Unset = UNSET,
    web_hook_request_callback: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    headers["WebHook-Request-Origin"] = web_hook_request_origin

    if not isinstance(web_hook_request_rate, Unset):
        headers["WebHook-Request-Rate"] = web_hook_request_rate

    if not isinstance(web_hook_request_callback, Unset):
        headers["WebHook-Request-Callback"] = web_hook_request_callback

    _kwargs: dict[str, Any] = {
        "method": "options",
        "url": "/v1/ingest/{source_id}/{format_}".format(
            source_id=quote(str(source_id), safe=""),
            format_=quote(str(format_), safe=""),
        ),
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 200:
        response_200 = cast(Any, None)
        return response_200

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    web_hook_request_origin: str,
    web_hook_request_rate: str | Unset = UNSET,
    web_hook_request_callback: str | Unset = UNSET,
) -> Response[Any | Problem]:
    """CloudEvents webhook validation handshake

     CloudEvents HTTP webhook abuse protection: answers `OPTIONS` carrying `WebHook-Request-Origin` with
    200, `WebHook-Allowed-Origin` (the requesting origin) and `WebHook-Allowed-Rate` (the source's per-
    minute budget). Only the `cloudevents` format answers; others → 404.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        web_hook_request_origin (str):
        web_hook_request_rate (str | Unset):
        web_hook_request_callback (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        format_=format_,
        web_hook_request_origin=web_hook_request_origin,
        web_hook_request_rate=web_hook_request_rate,
        web_hook_request_callback=web_hook_request_callback,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    web_hook_request_origin: str,
    web_hook_request_rate: str | Unset = UNSET,
    web_hook_request_callback: str | Unset = UNSET,
) -> Any | Problem | None:
    """CloudEvents webhook validation handshake

     CloudEvents HTTP webhook abuse protection: answers `OPTIONS` carrying `WebHook-Request-Origin` with
    200, `WebHook-Allowed-Origin` (the requesting origin) and `WebHook-Allowed-Rate` (the source's per-
    minute budget). Only the `cloudevents` format answers; others → 404.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        web_hook_request_origin (str):
        web_hook_request_rate (str | Unset):
        web_hook_request_callback (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        source_id=source_id,
        format_=format_,
        client=client,
        web_hook_request_origin=web_hook_request_origin,
        web_hook_request_rate=web_hook_request_rate,
        web_hook_request_callback=web_hook_request_callback,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    web_hook_request_origin: str,
    web_hook_request_rate: str | Unset = UNSET,
    web_hook_request_callback: str | Unset = UNSET,
) -> Response[Any | Problem]:
    """CloudEvents webhook validation handshake

     CloudEvents HTTP webhook abuse protection: answers `OPTIONS` carrying `WebHook-Request-Origin` with
    200, `WebHook-Allowed-Origin` (the requesting origin) and `WebHook-Allowed-Rate` (the source's per-
    minute budget). Only the `cloudevents` format answers; others → 404.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        web_hook_request_origin (str):
        web_hook_request_rate (str | Unset):
        web_hook_request_callback (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        format_=format_,
        web_hook_request_origin=web_hook_request_origin,
        web_hook_request_rate=web_hook_request_rate,
        web_hook_request_callback=web_hook_request_callback,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    web_hook_request_origin: str,
    web_hook_request_rate: str | Unset = UNSET,
    web_hook_request_callback: str | Unset = UNSET,
) -> Any | Problem | None:
    """CloudEvents webhook validation handshake

     CloudEvents HTTP webhook abuse protection: answers `OPTIONS` carrying `WebHook-Request-Origin` with
    200, `WebHook-Allowed-Origin` (the requesting origin) and `WebHook-Allowed-Rate` (the source's per-
    minute budget). Only the `cloudevents` format answers; others → 404.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        web_hook_request_origin (str):
        web_hook_request_rate (str | Unset):
        web_hook_request_callback (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            format_=format_,
            client=client,
            web_hook_request_origin=web_hook_request_origin,
            web_hook_request_rate=web_hook_request_rate,
            web_hook_request_callback=web_hook_request_callback,
        )
    ).parsed
