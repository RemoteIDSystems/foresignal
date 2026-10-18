from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.gotify_message import GotifyMessage
from ...models.gotify_message_input import GotifyMessageInput
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: GotifyMessageInput | Unset = UNSET,
    token: str | Unset = UNSET,
    x_gotify_key: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(x_gotify_key, Unset):
        headers["X-Gotify-Key"] = x_gotify_key

    params: dict[str, Any] = {}

    params["token"] = token

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/compat/gotify/message",
        "params": params,
    }

    if isinstance(body, GotifyMessageInput):
        _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/json"
    if isinstance(body, GotifyMessageInput):
        _kwargs["data"] = body.to_dict()
        headers["Content-Type"] = "application/x-www-form-urlencoded"
    if isinstance(body, GotifyMessageInput):
        _kwargs["files"] = body.to_multipart()

        headers["Content-Type"] = "multipart/form-data; boundary=+++"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> GotifyMessage | Problem | None:
    if response.status_code == 200:
        response_200 = GotifyMessage.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[GotifyMessage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: GotifyMessageInput | Unset = UNSET,
    token: str | Unset = UNSET,
    x_gotify_key: str | Unset = UNSET,
) -> Response[GotifyMessage | Problem]:
    """Gotify-compatible message publish

     Gotify-compatible publish (Proxmox and other tools). The ingest key in `X-Gotify-Key` or `?token=`
    identifies the source. Kind = the source's single kind (else `notification`); externalId from
    `extras["foresignal::record"].externalId`, else a ULID. Outside `/v1`; AccessPolicy rule
    `/compat/gotify/**`.

    **Scope:** ingest key (or an `admin` token).

    Args:
        token (str | Unset):
        x_gotify_key (str | Unset):
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GotifyMessage | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        token=token,
        x_gotify_key=x_gotify_key,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: GotifyMessageInput | Unset = UNSET,
    token: str | Unset = UNSET,
    x_gotify_key: str | Unset = UNSET,
) -> GotifyMessage | Problem | None:
    """Gotify-compatible message publish

     Gotify-compatible publish (Proxmox and other tools). The ingest key in `X-Gotify-Key` or `?token=`
    identifies the source. Kind = the source's single kind (else `notification`); externalId from
    `extras["foresignal::record"].externalId`, else a ULID. Outside `/v1`; AccessPolicy rule
    `/compat/gotify/**`.

    **Scope:** ingest key (or an `admin` token).

    Args:
        token (str | Unset):
        x_gotify_key (str | Unset):
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GotifyMessage | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
        token=token,
        x_gotify_key=x_gotify_key,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: GotifyMessageInput | Unset = UNSET,
    token: str | Unset = UNSET,
    x_gotify_key: str | Unset = UNSET,
) -> Response[GotifyMessage | Problem]:
    """Gotify-compatible message publish

     Gotify-compatible publish (Proxmox and other tools). The ingest key in `X-Gotify-Key` or `?token=`
    identifies the source. Kind = the source's single kind (else `notification`); externalId from
    `extras["foresignal::record"].externalId`, else a ULID. Outside `/v1`; AccessPolicy rule
    `/compat/gotify/**`.

    **Scope:** ingest key (or an `admin` token).

    Args:
        token (str | Unset):
        x_gotify_key (str | Unset):
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GotifyMessage | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        token=token,
        x_gotify_key=x_gotify_key,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: GotifyMessageInput | Unset = UNSET,
    token: str | Unset = UNSET,
    x_gotify_key: str | Unset = UNSET,
) -> GotifyMessage | Problem | None:
    """Gotify-compatible message publish

     Gotify-compatible publish (Proxmox and other tools). The ingest key in `X-Gotify-Key` or `?token=`
    identifies the source. Kind = the source's single kind (else `notification`); externalId from
    `extras["foresignal::record"].externalId`, else a ULID. Outside `/v1`; AccessPolicy rule
    `/compat/gotify/**`.

    **Scope:** ingest key (or an `admin` token).

    Args:
        token (str | Unset):
        x_gotify_key (str | Unset):
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)
        body (GotifyMessageInput): Gotify message body (JSON or form) accepted by `POST
            /compat/gotify/message` (024)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GotifyMessage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            token=token,
            x_gotify_key=x_gotify_key,
        )
    ).parsed
